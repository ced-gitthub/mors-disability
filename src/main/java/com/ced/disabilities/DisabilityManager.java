package com.ced.disabilities;

import com.ced.disabilities.disability.Disabilities;
import com.ced.disabilities.disability.Disability;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Per-player state lives in Fabric data attachments: OWNED is saved and synced to the client, TIMERS is saved only. */
public final class DisabilityManager {
	public static final AttachmentType<List<String>> OWNED = AttachmentRegistry.<List<String>>builder()
			.persistent(Codec.STRING.listOf())
			.copyOnDeath()
			.initializer(List::of)
			.syncWith(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "owned"));

	/** Ticks left on each of this player's timers, keyed by severity ("all" when one shared timer is used). */
	public static final AttachmentType<java.util.Map<String, Integer>> TIMERS = AttachmentRegistry.<java.util.Map<String, Integer>>builder()
			.persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
			.copyOnDeath()
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "timers"));

	/** Ticks until the next disability as last sent to the client, -1 when the timer is off. */
	public static final AttachmentType<Integer> COUNTDOWN = AttachmentRegistry.<Integer>builder()
			.initializer(() -> -1)
			.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "countdown"));

	private DisabilityManager() {
	}

	public static void init() {
		// forces class load so the attachments register
	}

	public static List<String> owned(ServerPlayer player) {
		return player.getAttachedOrElse(OWNED, List.of());
	}

	public static boolean give(ServerPlayer player, Disability d) {
		List<String> owned = owned(player);
		if (owned.contains(d.id)) return false;
		List<String> next = new ArrayList<>(owned);
		next.add(d.id);
		player.setAttached(OWNED, List.copyOf(next));
		d.onAdd(player);
		if (!next.contains("anosognosia")) player.sendSystemMessage(Component.literal("You gained a disability: " + d.displayName + " (" + d.description + ")"));
		return true;
	}

	public static boolean remove(ServerPlayer player, Disability d) {
		List<String> owned = owned(player);
		if (!owned.contains(d.id)) return false;
		List<String> next = new ArrayList<>(owned);
		next.remove(d.id);
		player.setAttached(OWNED, List.copyOf(next));
		d.onRemove(player);
		return true;
	}

	public static void reset(ServerPlayer player) {
		for (String id : new ArrayList<>(owned(player))) {
			Disability d = Disabilities.get(id);
			if (d != null) remove(player, d);
		}
		player.setAttached(OWNED, List.of());
		resetTimers(player);
	}

	/** Adds one random new disability on top of what the player already has. */
	public static Disability giveRandom(ServerPlayer player) {
		Disability d = Disabilities.pickRandom(owned(player));
		if (d != null) give(player, d);
		return d;
	}

	/** Timer keys with their length in ticks; "all" is the single timer used when no per-severity timers are configured. */
	private static java.util.Map<String, Integer> activeTimers() {
		DisabilityConfig cfg = DisabilityConfig.get();
		java.util.Map<String, Integer> out = new java.util.LinkedHashMap<>();
		if (cfg.severityMinutes.isEmpty()) {
			out.put("all", cfg.intervalTicks());
		} else {
			for (String s : Disabilities.SEVERITIES) {
				out.put(s, DisabilityConfig.toTicks(cfg.severityMinutes.getOrDefault(s, cfg.intervalMinutes)));
			}
		}
		return out;
	}

	/** Restarts every timer of this player at its full length. */
	public static void resetTimers(ServerPlayer player) {
		java.util.Map<String, Integer> fresh = new java.util.HashMap<>();
		activeTimers().forEach((k, v) -> fresh.put(k, Math.max(1, v)));
		player.setAttached(TIMERS, fresh);
	}

	/** Gives one random disability from a timer's pool; in replace mode that pool's old disabilities go first. */
	private static void fire(ServerPlayer player, String key) {
		String severity = key.equals("all") ? null : key;
		if ("replace".equals(DisabilityConfig.get().mode)) {
			for (String id : new ArrayList<>(owned(player))) {
				Disability old = Disabilities.get(id);
				if (old != null && (severity == null || severity.equals(Disabilities.severity(id)))) remove(player, old);
			}
		}
		Disability d = Disabilities.pickRandom(owned(player), severity);
		if (d != null) give(player, d);
	}

	public static void tick(ServerPlayer player) {
		for (String id : player.isAlive() ? owned(player) : List.<String>of()) {
			Disability d = Disabilities.get(id);
			if (d != null) d.tick(player);
		}

		java.util.Map<String, Integer> timers = new java.util.HashMap<>(player.getAttachedOrElse(TIMERS, java.util.Map.of()));
		int shortest = -1, shortestLen = 0;
		for (var e : activeTimers().entrySet()) {
			int interval = e.getValue();
			if (interval <= 0) continue;
			int left = Math.min(timers.getOrDefault(e.getKey(), interval), interval) - 1;
			if (left <= 0) {
				fire(player, e.getKey());
				left = interval;
			}
			timers.put(e.getKey(), left);
			if (shortest < 0 || left < shortest) { shortest = left; shortestLen = interval; }
		}
		player.setAttached(TIMERS, timers);
		if (shortest < 0) {
			if (player.getAttachedOrElse(COUNTDOWN, -1) != -1) player.setAttached(COUNTDOWN, -1);
			return;
		}
		// Synced about once a second; the client counts down locally in between.
		int shown = player.getAttachedOrElse(COUNTDOWN, -1);
		if (shortest == shortestLen || shortest % 20 == 0 || Math.abs(shown - shortest) > 40) player.setAttached(COUNTDOWN, shortest);
	}
}
