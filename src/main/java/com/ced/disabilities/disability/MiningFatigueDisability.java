// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class MiningFatigueDisability extends Disability {
	public MiningFatigueDisability(String id) {
		super(id, "", "");
	}

	@Override
	public void tick(ServerPlayer player) {
		MobEffectInstance cur = player.getEffect(MobEffects.DIG_SLOWDOWN);
		if (cur == null || cur.getDuration() < 100) {
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 220, 0, true, false, false));
		}
	}

	@Override
	public void onRemove(ServerPlayer player) {
		player.removeEffect(MobEffects.DIG_SLOWDOWN);
	}
}
