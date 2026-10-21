package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;

/** A disability made of several smaller ones whose lifecycle hooks are forwarded. */
public class CompositeDisability extends Disability {
	private final Disability[] parts;

	public CompositeDisability(String id, String name, String description, Disability... parts) {
		super(id, name, description);
		this.parts = parts;
	}

	@Override
	public void tick(ServerPlayer player) {
		for (Disability p : parts) p.tick(player);
	}

	@Override
	public void onAdd(ServerPlayer player) {
		for (Disability p : parts) p.onAdd(player);
	}

	@Override
	public void onRemove(ServerPlayer player) {
		for (Disability p : parts) p.onRemove(player);
	}
}