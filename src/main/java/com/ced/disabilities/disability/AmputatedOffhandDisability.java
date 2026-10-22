// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class AmputatedOffhandDisability extends Disability {
	public AmputatedOffhandDisability() {
		super("amputated_offhand", "Amputated Offhand", "Loss of a hand or arm: the offhand is gone");
	}

	@Override
	public void tick(net.minecraft.server.level.ServerPlayer player) {
		net.minecraft.world.item.ItemStack off = player.getOffhandItem();
		if (off.isEmpty()) return;
		player.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
		if (!player.getInventory().add(off)) player.drop(off, false);
	}

	private static boolean owns(Player p) {
		return DisabilityManager.ownedOf(p).contains("amputated_offhand");
	}

	public static void registerEvents() {
		UseItemCallback.EVENT.register((player, level, hand) ->
				owns(player) && hand == InteractionHand.OFF_HAND
						? InteractionResultHolder.fail(player.getItemInHand(hand)) : InteractionResultHolder.pass(player.getItemInHand(hand)));
		UseBlockCallback.EVENT.register((player, level, hand, hit) ->
				owns(player) && hand == InteractionHand.OFF_HAND
						? InteractionResult.FAIL : InteractionResult.PASS);
	}
}