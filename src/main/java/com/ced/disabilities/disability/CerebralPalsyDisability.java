package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/** Random stumbles shove you sideways. */
public class CerebralPalsyDisability extends Disability {
	private static final Random RANDOM = new Random();

	public CerebralPalsyDisability() {
		super("cerebral_palsy", "Cerebral Palsy", "You randomly stumble");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(400)) != 0) return;
		if (Boolean.getBoolean("disabilitystack.selftest")) com.ced.disabilities.DisabilityMod.LOGGER.info("[selftest] cerebral_palsy fired motion={}", player.getDeltaMovement());
		player.addDeltaMovement(new Vec3((RANDOM.nextDouble() - 0.5) * 0.8, 0, (RANDOM.nextDouble() - 0.5) * 0.8));
		player.hurtMarked = true;
	}
}