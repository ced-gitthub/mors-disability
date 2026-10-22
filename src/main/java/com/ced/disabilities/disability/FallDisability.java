// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FallDisability extends Disability {
	private final float multiplier;
	private final float extraBlocks;
	private final Map<UUID, Float> peak = new ConcurrentHashMap<>();

	public FallDisability(String id, String name, String description, float multiplier, float extraBlocks) {
		super(id, name, description);
		this.multiplier = multiplier;
		this.extraBlocks = extraBlocks;
	}

	@Override
	public void tick(ServerPlayer player) {
		float fall = player.fallDistance;
		if (!player.onGround()) {
			if (fall > 0) peak.merge(player.getUUID(), fall, Math::max);
			return;
		}
		Float f = peak.remove(player.getUUID());
		if (f == null || player.isCreative() || player.isSpectator()) return;
		float vanilla = Math.max(0f, f - 3f);
		float wanted = Math.max(0f, f + extraBlocks - 3f) * multiplier;
		float extra = wanted - vanilla;
		if (extra >= 1f && vanilla + extra > 0 && f >= 3f - extraBlocks) player.hurt(player.damageSources().fall(), extra);
	}

	@Override
	public void onRemove(ServerPlayer player) {
		peak.remove(player.getUUID());
	}

	@Override
	public void onLeave(ServerPlayer player) {
		peak.remove(player.getUUID());
	}
}
