// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class KleptomaniaDisability extends Disability {
	public KleptomaniaDisability() {
		super("kleptomania", "Kleptomania", "Compulsive need to hold on: you cannot drop items");
	}

	@Override
	public void tick(ServerPlayer player) {
		AABB box = player.getBoundingBox().inflate(4.0);
		for (ItemEntity entity : player.level().getEntitiesOfClass(ItemEntity.class, box)) {
			if (entity.getOwner() != player || entity.getAge() > 5) continue;
			ItemStack stack = entity.getItem().copy();
			if (player.getInventory().add(stack)) {
				entity.discard();
			} else if (stack.getCount() != entity.getItem().getCount()) {
				entity.setItem(stack);
			}
		}
	}
}
