// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LactoseDisability extends Disability {
	private static final int DEATH_TICKS = 100;
	private final Map<UUID, Integer> dying = new HashMap<>();

	public LactoseDisability() {
		super("lactose_intolerance", "Lactose Intolerance", "Cannot digest milk: drinking it makes you violently ill (5s slow death, only a golden apple cures it)");
	}

	private static boolean finishing(ServerPlayer player, boolean golden) {
		if (!player.isUsingItem() || player.getUseItemRemainingTicks() > 1) return false;
		ItemStack use = player.getUseItem();
		return golden ? use.is(Items.GOLDEN_APPLE) || use.is(Items.ENCHANTED_GOLDEN_APPLE) : use.is(Items.MILK_BUCKET);
	}

	@Override
	public void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		Integer left = dying.get(id);
		if (left == null) {
			if (finishing(player, false)) dying.put(id, DEATH_TICKS);
			return;
		}
		if (finishing(player, true)) {
			dying.remove(id);
			return;
		}
		if (left % 20 == 0) {
			int secondsLeft = left / 20;
			if (secondsLeft <= 1) {
				player.kill();
			} else {
				player.hurt(player.damageSources().magic(), player.getHealth() / secondsLeft);
			}
		}
		if (left <= 1 || !player.isAlive()) dying.remove(id); else dying.put(id, left - 1);
	}

	@Override
	public void onLeave(ServerPlayer player) {
		dying.remove(player.getUUID());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		onLeave(player);
	}
}
