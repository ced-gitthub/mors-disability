// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ModCheck {
	public record Hello() implements CustomPacketPayload {
		public static final Type<Hello> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "hello"));
		public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, Hello> CODEC = StreamCodec.unit(new Hello());

		@Override
		public Type<? extends CustomPacketPayload> type() { return TYPE; }
	}

	private ModCheck() {}

	public static void init() {
		PayloadTypeRegistry.clientboundConfiguration().register(Hello.TYPE, Hello.CODEC);
		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (!ServerConfigurationNetworking.canSend(handler, Hello.TYPE)) {
				handler.disconnect(Component.literal("This server requires the Mors Disability mod (mors-disability). Install it and rejoin."));
			}
		});
	}
}
