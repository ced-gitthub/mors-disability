// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisabilityMod implements ModInitializer {
	public static final String MOD_ID = "mors-disability";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModCheck.init();
		DisabilityConfig.load();
		DisabilityManager.init();
		com.ced.disabilities.disability.Disabilities.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			DisabilityCommand.register(dispatcher);
			com.ced.disabilities.disability.MuteDisability.guardCommands(dispatcher);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				DisabilityManager.tick(player);
			}
		});
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			for (var d : com.ced.disabilities.disability.Disabilities.all()) d.onLeave(handler.getPlayer());
		});
		LOGGER.info("Disability Stack loaded, interval = {} min", DisabilityConfig.get().intervalMinutes);
	}
}