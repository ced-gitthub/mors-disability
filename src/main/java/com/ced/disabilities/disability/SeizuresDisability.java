// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Random;

public class SeizuresDisability extends Disability {
	private static final Random RANDOM = new Random();

	public SeizuresDisability() {
		super("seizures", "Seizures", "Sudden seizures: convulsions, nausea and near-immobility");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(6000)) != 0) return;
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 0, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 5, true, false, false));
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] seizures fired");
	}

	@Override
	public void onRemove(ServerPlayer player) {
		player.removeEffect(MobEffects.CONFUSION);
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
	}
}
