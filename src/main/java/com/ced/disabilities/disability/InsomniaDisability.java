// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class InsomniaDisability extends Disability {
	public InsomniaDisability() {
		super("insomnia", "Insomnia", "Chronic inability to sleep");
	}

	public static void registerEvents() {
		EntitySleepEvents.ALLOW_SLEEPING.register((player, pos) -> {
			if (player instanceof ServerPlayer sp
					&& DisabilityManager.ownedOf(sp).contains("insomnia")) {
				sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("You can't fall asleep."), true);
				return Player.BedSleepingProblem.OTHER_PROBLEM;
			}
			return null;
		});
	}
}
