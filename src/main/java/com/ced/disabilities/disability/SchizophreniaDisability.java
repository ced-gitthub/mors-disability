// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

public class SchizophreniaDisability extends Disability {
	private static final Random RANDOM = new Random();
	private static final SoundEvent[] SOUNDS = {SoundEvents.CREEPER_PRIMED, SoundEvents.ZOMBIE_AMBIENT,
			SoundEvents.ENDERMAN_STARE, SoundEvents.SKELETON_AMBIENT, SoundEvents.GHAST_SCREAM, SoundEvents.WOODEN_DOOR_OPEN};

	public SchizophreniaDisability() {
		super("schizophrenia", "Schizophrenia", "Hallucinations: you hear and see things that are not there");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(600)) != 0) return;
		SoundEvent s = SOUNDS[RANDOM.nextInt(SOUNDS.length)];
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
				net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s), SoundSource.HOSTILE,
				player.getX() + RANDOM.nextInt(7) - 3, player.getY(), player.getZ() + RANDOM.nextInt(7) - 3,
				1.0f, 0.8f + RANDOM.nextFloat() * 0.4f, RANDOM.nextLong()));
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] schizophrenia fired {}", s.location());
	}
}
