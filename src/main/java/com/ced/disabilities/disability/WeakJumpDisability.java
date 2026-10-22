// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeakJumpDisability extends Disability {
	private final double factor;
	private final Map<UUID, Boolean> grounded = new ConcurrentHashMap<>();

	public WeakJumpDisability(String id, double factor) {
		super(id, "", "");
		this.factor = factor;
	}

	@Override
	public void tick(ServerPlayer player) {
		boolean was = grounded.getOrDefault(player.getUUID(), true);
		boolean now = player.onGround();
		grounded.put(player.getUUID(), now);
		Vec3 v = player.getDeltaMovement();
		if (was && !now && v.y > 0.2 && v.y < 0.6) {
			player.setDeltaMovement(v.x, v.y * factor, v.z);
			player.hurtMarked = true;
		}
	}

	@Override
	public void onRemove(ServerPlayer player) {
		grounded.remove(player.getUUID());
	}

	@Override
	public void onLeave(ServerPlayer player) {
		grounded.remove(player.getUUID());
	}
}
