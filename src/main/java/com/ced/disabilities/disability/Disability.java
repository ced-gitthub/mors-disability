// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;

public abstract class Disability {
	public final String id;
	public final String displayName;
	public final String description;

	protected Disability(String id, String displayName, String description) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
	}

	public void tick(ServerPlayer player) {
	}

	public void onAdd(ServerPlayer player) {
	}

	public void onRemove(ServerPlayer player) {
	}

	protected static int odds(int n) {
		return Boolean.getBoolean("disabilitystack.selftest") ? Math.min(n, 40) : n;
	}

	public void onLeave(ServerPlayer player) {
	}
}