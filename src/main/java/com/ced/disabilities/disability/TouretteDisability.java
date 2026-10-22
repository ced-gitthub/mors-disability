// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Random;

public class TouretteDisability extends Disability {
	private static final Random RANDOM = new Random();
	private static final List<SoundEvent> NOISES = List.of(SoundEvents.ZOMBIE_AMBIENT, SoundEvents.VILLAGER_NO);

	public TouretteDisability() {
		super("tourette", "Tourette's", "Involuntary tics: random noises and twitches");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(900)) != 0) return;
		if (Boolean.getBoolean("disabilitystack.selftest")) com.ced.disabilities.DisabilityMod.LOGGER.info("[selftest] tourette fired motion={}", player.getDeltaMovement());
		SoundEvent noise = NOISES.get(RANDOM.nextInt(NOISES.size()));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), noise, SoundSource.PLAYERS, 1.0f, 1.0f);
		if (player.onGround()) {
			player.addDeltaMovement(new net.minecraft.world.phys.Vec3(0, 0.3, 0));
			player.syncVelocity = true;
		}
	}
}