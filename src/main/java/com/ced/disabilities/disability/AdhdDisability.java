// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class AdhdDisability extends Disability {
	private static final Random RANDOM = new Random();

	public AdhdDisability() {
		super("adhd", "ADHD", "Impulsivity and restlessness: random jumps, item switches, drops and head twitches");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(160)) != 0) return;
		switch (RANDOM.nextInt(4)) {
			case 0 -> {
				if (player.onGround()) player.jumpFromGround();
			}
			case 1 -> {
				int slot = RANDOM.nextInt(9);
				player.getInventory().selected = slot;
				player.connection.send(new ClientboundSetCarriedItemPacket(slot));
			}
			case 2 -> player.drop(false);
			default -> player.connection.teleport(player.getX(), player.getY(), player.getZ(),
					player.getYRot() + RANDOM.nextInt(121) - 60, Math.max(-60f, Math.min(60f, player.getXRot() + RANDOM.nextInt(41) - 20)));
		}
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] adhd fired");
	}
}
