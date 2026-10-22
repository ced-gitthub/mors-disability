// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ModCheck {
	public static final ResourceLocation HELLO = new ResourceLocation(DisabilityMod.MOD_ID, "hello");

	private ModCheck() {}

	public static void init() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
				sender.sendPacket(HELLO, PacketByteBufs.create()));
		ServerLoginNetworking.registerGlobalReceiver(HELLO, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood) {
				handler.disconnect(Component.literal("This server requires the Mors Disability mod (mors-disability). Install it and rejoin."));
			}
		});
	}
}
