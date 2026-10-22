// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Random;

public class NarcolepsyDisability extends Disability {
	private static final Random RANDOM = new Random();

	public NarcolepsyDisability() {
		super("narcolepsy", "Narcolepsy", "Sudden sleep attacks: you randomly collapse asleep");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(4800)) != 0) return;
		if (Boolean.getBoolean("disabilitystack.selftest")) com.ced.disabilities.DisabilityMod.LOGGER.info("[selftest] narcolepsy fired motion={}", player.getDeltaMovement());
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 6, false, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, true));
		player.sendSystemMessage(Component.literal("You fell asleep..."), true);
	}
}