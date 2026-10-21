package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

/** Finishing a bucket of milk is fatal. */
public class LactoseDisability extends Disability {
	public LactoseDisability() {
		super("lactose_intolerance", "Lactose Intolerance", "Drinking milk kills you");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (player.isUsingItem() && player.getUseItem().is(Items.MILK_BUCKET)
				&& player.getUseItemRemainingTicks() <= 1) {
			player.kill(player.level());
		}
	}
}
