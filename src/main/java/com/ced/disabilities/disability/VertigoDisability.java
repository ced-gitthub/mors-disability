package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Standing with a long drop beneath you makes you dizzy and sluggish. */
public class VertigoDisability extends Disability {
	public VertigoDisability() {
		super("vertigo", "Vertigo", "Heights make you dizzy");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (player.tickCount % 20 != 0 || !player.onGround()) return;
		BlockPos feet = player.blockPosition();
		for (int i = 2; i <= 6; i++) {
			if (!player.level().getBlockState(feet.below(i)).isAir()) return;
		}
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, false));
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] vertigo fired");
	}

	@Override
	public void onRemove(ServerPlayer player) {
		player.removeEffect(MobEffects.NAUSEA);
		player.removeEffect(MobEffects.SLOWNESS);
	}
}
