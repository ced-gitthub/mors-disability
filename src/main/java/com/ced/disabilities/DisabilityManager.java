// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import com.ced.disabilities.disability.Disabilities;
import com.ced.disabilities.disability.Disability;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.List;

public final class DisabilityManager {
	public static final ResourceLocation SYNC = new ResourceLocation(DisabilityMod.MOD_ID, "sync");

	private DisabilityManager() {
	}

	public static void init() {

	}

	private static PlayerState.Entry entry(ServerPlayer player) {
		return PlayerState.get(player.server).entry(player.getUUID());
	}

	public static List<String> owned(ServerPlayer player) {
		return entry(player).owned;
	}

	public static List<String> ownedOf(Entity e) {
		if (e instanceof ServerPlayer sp) return owned(sp);
		if (e != null && e.level().isClientSide && e.getUUID().equals(ClientCache.self)) return ClientCache.owned;
		return List.of();
	}

	private static void setOwned(ServerPlayer player, List<String> next) {
		entry(player).owned = next;
		PlayerState.get(player.server).touch();
		sync(player);
	}

	public static void sync(ServerPlayer player) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		List<String> owned = owned(player);
		buf.writeVarInt(owned.size());
		for (String s : owned) buf.writeUtf(s);
		java.util.Map<String, Integer> view = entry(player).timers;
		buf.writeVarInt(view.size());
		view.forEach((k, v) -> { buf.writeUtf(k); buf.writeVarInt(v); });
		if (ServerPlayNetworking.canSend(player, SYNC)) ServerPlayNetworking.send(player, SYNC, buf);
	}

	public static boolean give(ServerPlayer player, Disability d) {
		List<String> owned = owned(player);
		if (owned.contains(d.id)) return false;
		List<String> next = new ArrayList<>(owned);
		next.add(d.id);
		setOwned(player, List.copyOf(next));
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
		setOwned(player, List.copyOf(next));
		d.onRemove(player);
		setTimer(player, d.id, null);
		return true;
	}

	public static void reset(ServerPlayer player) {
		for (String id : new ArrayList<>(owned(player))) {
			Disability d = Disabilities.get(id);
			if (d != null) remove(player, d);
		}
		setOwned(player, List.of());
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

	private static int cfg0() { return DisabilityConfig.get().intervalTicks(); }

	private static void setTimer(ServerPlayer player, String id, Integer ticks) {
		java.util.Map<String, Integer> m = new java.util.HashMap<>(entry(player).timers);
		if (ticks == null) m.remove(id); else m.put(id, ticks);
		entry(player).timers = m;
		PlayerState.get(player.server).touch();
	}

	public static void resetTimers(ServerPlayer player) {
		java.util.Map<String, Integer> fresh = new java.util.HashMap<>();
		for (String id : owned(player)) {
			int len = lengthFor(id);
			if (len > 0) fresh.put(id, len);
		}
		entry(player).timers = fresh;
		PlayerState.get(player.server).touch();
		sync(player);
	}

	private static void expireAll(ServerPlayer player, List<String> ids) {
		DisabilityConfig cfg = DisabilityConfig.get();
		List<String> exclude = new ArrayList<>(owned(player));
		for (String id : ids) {
			Disability old = Disabilities.get(id);
			if (old != null) remove(player, old);
		}
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

		java.util.Map<String, Integer> old = entry(player).timers;
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
		if (!timers.equals(old)) { entry(player).timers = timers; PlayerState.get(player.server).touch(); }
		if (bootstrap) expireAll(player, List.of());
		boolean allDone = bootstrap || (timed > 0 && expired.size() == timed);
		if (allDone && !bootstrap) expireAll(player, expired);

		if (player.tickCount % 20 == 0 || allDone) {
			sync(player);
		}
	}
}
