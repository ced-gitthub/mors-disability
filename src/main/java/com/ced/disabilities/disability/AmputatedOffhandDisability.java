package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** The offhand can neither use items nor interact with blocks. Handled by events. */
public class AmputatedOffhandDisability extends Disability {
	public AmputatedOffhandDisability() {
		super("amputated_offhand", "Amputated Offhand", "You cannot use your offhand");
	}

	@Override
	public void tick(net.minecraft.server.level.ServerPlayer player) {
		net.minecraft.world.item.ItemStack off = player.getOffhandItem();
		if (off.isEmpty()) return;
		player.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
		if (!player.getInventory().add(off)) player.drop(off, false);
	}

	private static boolean owns(Player p) {
		return p.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("amputated_offhand");
	}

	public static void registerEvents() {
		UseItemCallback.EVENT.register((player, level, hand) ->
				owns(player) && hand == InteractionHand.OFF_HAND
						? InteractionResult.FAIL : InteractionResult.PASS);
		UseBlockCallback.EVENT.register((player, level, hand, hit) ->
				owns(player) && hand == InteractionHand.OFF_HAND
						? InteractionResult.FAIL : InteractionResult.PASS);
	}
}