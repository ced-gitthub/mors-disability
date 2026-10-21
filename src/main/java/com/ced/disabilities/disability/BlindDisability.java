package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class BlindDisability extends Disability {
	public BlindDisability() {
		super("blind", "Blind", "Total loss of vision");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (player.tickCount % 20 == 0) {
			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0, true, false, false));
		}
	}

	@Override
	public void onRemove(ServerPlayer player) {
		player.removeEffect(MobEffects.BLINDNESS);
	}
}