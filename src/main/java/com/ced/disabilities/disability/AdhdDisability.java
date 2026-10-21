package com.ced.disabilities.disability;

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

/** The selected hotbar slot changes on its own. */
public class AdhdDisability extends Disability {
	private static final Random RANDOM = new Random();

	public AdhdDisability() {
		super("adhd", "ADHD", "Your attention keeps wandering");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(300) != 0) return;
		int slot = RANDOM.nextInt(9);
		player.getInventory().setSelectedSlot(slot);
		player.connection.send(new ClientboundSetHeldSlotPacket(slot));
	}
}