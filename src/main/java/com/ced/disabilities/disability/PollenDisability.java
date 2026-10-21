package com.ced.disabilities.disability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Standing near flowers poisons you. */
public class PollenDisability extends Disability {
	private static final int RADIUS = 3;

	public PollenDisability() {
		super("pollen_allergy", "Pollen Allergy", "Flowers nearby poison you");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (player.tickCount % 20 != 0) return;
		BlockPos center = player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS),
				center.offset(RADIUS, RADIUS, RADIUS))) {
			if (player.level().getBlockState(pos).is(BlockTags.FLOWERS)) {
				player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true, true));
				return;
			}
		}
	}
}
