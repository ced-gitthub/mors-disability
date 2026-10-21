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

	/** Called every server tick while the player owns this disability. */
	public void tick(ServerPlayer player) {
	}

	/** Called once when the disability is added. */
	public void onAdd(ServerPlayer player) {
	}

	/** Called once when the disability is removed; undo anything tick() applied. */
	public void onRemove(ServerPlayer player) {
	}

	/** One-in-"n" tick odds; the dev self-test shrinks them so random effects can be observed quickly. */
	protected static int odds(int n) {
		return Boolean.getBoolean("disabilitystack.selftest") ? Math.min(n, 40) : n;
	}

	/** Called when the player leaves the server; drop any per-player bookkeeping. */
	public void onLeave(ServerPlayer player) {
	}
}