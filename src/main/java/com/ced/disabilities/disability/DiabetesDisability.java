// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class DiabetesDisability extends Disability {
	private static final int LOW_FOOD = 6;

	public DiabetesDisability() {
		super("diabetes", "Diabetes", "Unstable blood sugar: you get hungry much faster, and a low food bar weakens and slows you");
	}

	@Override
	public void tick(ServerPlayer player) {
		player.causeFoodExhaustion(0.03f);
		if (player.getFoodData().getFoodLevel() <= LOW_FOOD && player.tickCount % 100 == 0) {
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 0, true, false, true));
		}
	}
}
