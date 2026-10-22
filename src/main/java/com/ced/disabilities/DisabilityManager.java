// Copyright (c) 2026 Ced. MIT License.
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

public final class DisabilityManager {
	public static final AttachmentType<List<String>> OWNED = AttachmentRegistry.<List<String>>builder()
			.persistent(Codec.STRING.listOf())
			.copyOnDeath()
			.initializer(List::of)
			.syncWith(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "owned"));

	public static final AttachmentType<java.util.Map<String, Integer>> TIMERS = AttachmentRegistry.<java.util.Map<String, Integer>>builder()
			.persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
			.copyOnDeath()
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "timers"));

	public static final AttachmentType<Integer> COUNTDOWN = AttachmentRegistry.<Integer>builder()
			.initializer(() -> -1)
			.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "countdown"));

	public static final AttachmentType<java.util.Map<String, Integer>> TIMER_VIEW = AttachmentRegistry.<java.util.Map<String, Integer>>builder()
			.initializer(java.util.Map::of)
			.syncWith(ByteBufCodecs.map(java.util.HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "timer_view"));

	private DisabilityManager() {
	}

	public static void init() {

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
		int len = lengthFor(d.id);
		if (len > 0) setTimer(player, d.id, len);
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
		setTimer(player, d.id, null);
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

	public static Disability giveRandom(ServerPlayer player) {
		Disability d = Disabilities.pickRandom(owned(player));
		if (d != null) give(player, d);
		return d;
	}

	private static int lengthFor(String id) {
		DisabilityConfig cfg = DisabilityConfig.get();
		if (cfg.severityMinutes.isEmpty()) return cfg.intervalTicks();
		return DisabilityConfig.toTicks(cfg.severityMinutes.getOrDefault(Disabilities.severity(id), cfg.intervalMinutes));
	}

	private static final String NEXT = "_next";

	private static int cfg0() { return DisabilityConfig.get().bootstrapTicks(); }

	private static void setTimer(ServerPlayer player, String id, Integer ticks) {
		java.util.Map<String, Integer> m = new java.util.HashMap<>(player.getAttachedOrElse(TIMERS, java.util.Map.of()));
		if (ticks == null) m.remove(id); else m.put(id, ticks);
		player.setAttached(TIMERS, m);
	}

	public static void resetTimers(ServerPlayer player) {
		java.util.Map<String, Integer> fresh = new java.util.HashMap<>();
		for (String id : owned(player)) {
			int len = lengthFor(id);
			if (len > 0) fresh.put(id, len);
		}
		player.setAttached(TIMERS, fresh);
	}

	private static void expireAll(ServerPlayer player, List<String> ids) {
		DisabilityConfig cfg = DisabilityConfig.get();
		List<String> exclude = new ArrayList<>(owned(player));
		boolean stack = "stack".equals(cfg.mode);
		for (String id : ids) {
			Disability old = Disabilities.get(id);
			if (old != null && !stack) remove(player, old);
		}
		if (stack) resetTimers(player);
		double roll = java.util.concurrent.ThreadLocalRandom.current().nextDouble();
		int count = roll < cfg.chanceThree ? 3 : roll < cfg.chanceThree + cfg.chanceTwo ? 2 : 1;
		for (int i = 0; i < count; i++) {
			Disability d = Disabilities.pickRandom(exclude, null);
			if (d == null) break;
			exclude.add(d.id);
			give(player, d);
		}
	}

	public static void tick(ServerPlayer player) {
		for (String id : player.isAlive() ? owned(player) : List.<String>of()) {
			Disability d = Disabilities.get(id);
			if (d != null) d.tick(player);
		}

		java.util.Map<String, Integer> old = player.getAttachedOrElse(TIMERS, java.util.Map.of());
		java.util.Map<String, Integer> timers = new java.util.HashMap<>();
		List<String> expired = new ArrayList<>();
		int timed = 0;
		for (String id : owned(player)) {
			int len = lengthFor(id);
			if (len <= 0) continue;
			timed++;
			int left = Math.max(0, Math.min(old.getOrDefault(id, len), len) - 1);
			timers.put(id, left);
			if (left == 0) expired.add(id);
		}
		boolean bootstrap = false;
		if (timed == 0 && owned(player).isEmpty() && player.isAlive() && cfg0() > 0) {
			int left = Math.max(0, Math.min(old.getOrDefault(NEXT, cfg0()), cfg0()) - 1);
			timers.put(NEXT, left);
			if (left == 0) { timers.remove(NEXT); bootstrap = true; }
		}
		if (!timers.equals(old)) player.setAttached(TIMERS, timers);
		if (bootstrap) expireAll(player, List.of());
		boolean allDone = bootstrap || (timed > 0 && expired.size() == timed);
		if (allDone && !bootstrap) expireAll(player, expired);

		if (player.tickCount % 20 == 0 || allDone) {
			player.setAttached(TIMER_VIEW, new java.util.HashMap<>(player.getAttachedOrElse(TIMERS, java.util.Map.of())));
		}
	}
}
