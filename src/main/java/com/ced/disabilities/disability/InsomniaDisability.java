package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Beds refuse to let you sleep. The check lives in an event, so tick() does nothing. */
public class InsomniaDisability extends Disability {
	public InsomniaDisability() {
		super("insomnia", "Insomnia", "Chronic inability to sleep");
	}

	public static void registerEvents() {
		EntitySleepEvents.ALLOW_SLEEPING.register((player, pos) -> {
			if (player instanceof ServerPlayer sp
					&& sp.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("insomnia")) {
				sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("You can't fall asleep."), true);
				return Player.BedSleepingProblem.OTHER_PROBLEM;
			}
			return null;
		});
	}
}
