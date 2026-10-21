package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Random;

/** Sudden memory lapses: the world darkens and you lose track of what you were holding. */
public class DementiaDisability extends Disability {
	private static final Random RANDOM = new Random();

	public DementiaDisability() {
		super("dementia", "Dementia", "You keep forgetting what you were doing");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(4800)) != 0) return;
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0, true, false, false));
		int slot = RANDOM.nextInt(9);
		player.getInventory().setSelectedSlot(slot);
		player.connection.send(new ClientboundSetHeldSlotPacket(slot));
		player.sendSystemMessage(Component.literal("Where am I...?"), true);
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia fired");
	}
}
