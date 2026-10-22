// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerState extends SavedData {
	private static final String ID = "mors_disability";

	public static final class Entry {
		public List<String> owned = List.of();
		public Map<String, Integer> timers = new HashMap<>();
	}

	private final Map<UUID, Entry> entries = new HashMap<>();

	public static PlayerState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(PlayerState::load, PlayerState::new, ID);
	}

	public Entry entry(UUID id) {
		return entries.computeIfAbsent(id, k -> new Entry());
	}

	public void touch() {
		setDirty();
	}

	public static PlayerState load(CompoundTag tag) {
		PlayerState s = new PlayerState();
		CompoundTag players = tag.getCompound("players");
		for (String key : players.getAllKeys()) {
			CompoundTag p = players.getCompound(key);
			Entry e = new Entry();
			List<String> owned = new ArrayList<>();
			for (Tag t : p.getList("owned", Tag.TAG_STRING)) owned.add(t.getAsString());
			e.owned = List.copyOf(owned);
			CompoundTag timers = p.getCompound("timers");
			for (String k : timers.getAllKeys()) e.timers.put(k, timers.getInt(k));
			try {
				s.entries.put(UUID.fromString(key), e);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return s;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		CompoundTag players = new CompoundTag();
		for (Map.Entry<UUID, Entry> en : entries.entrySet()) {
			CompoundTag p = new CompoundTag();
			ListTag owned = new ListTag();
			for (String s : en.getValue().owned) owned.add(StringTag.valueOf(s));
			p.put("owned", owned);
			CompoundTag timers = new CompoundTag();
			en.getValue().timers.forEach(timers::putInt);
			p.put("timers", timers);
			players.put(en.getKey().toString(), p);
		}
		tag.put("players", players);
		return tag;
	}
}
