// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.voice;

import com.ced.disabilities.DisabilityMod;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PacketEvent;
import de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

public class VoicechatIntegration implements VoicechatPlugin {
	@Override
	public String getPluginId() {
		return DisabilityMod.MOD_ID;
	}

	@Override
	public void initialize(VoicechatApi api) {
		VoiceFx.api = api;
		DisabilityMod.LOGGER.info("Simple Voice Chat found: mute, deaf, schizophrenia and Tourette's now affect voice chat");
	}

	@Override
	public void registerEvents(EventRegistration reg) {
		reg.registerEvent(VoicechatServerStartedEvent.class, e -> VoiceFx.server = e.getVoicechat());
		reg.registerEvent(VoicechatServerStoppedEvent.class, e -> VoiceFx.server = null);
		reg.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
		reg.registerEvent(EntitySoundPacketEvent.class, this::onSound);
		reg.registerEvent(LocationalSoundPacketEvent.class, this::onSound);
		reg.registerEvent(StaticSoundPacketEvent.class, this::onSound);
	}

	void onMicrophone(MicrophonePacketEvent e) {
		if (VoiceFx.has(e.getSenderConnection(), "mute")) e.cancel();
	}

	void onSound(PacketEvent<?> e) {
		if (VoiceFx.has(e.getReceiverConnection(), "deaf")) e.cancel();
	}

	/** Dev-only: drives the handlers with fake events for a real player and logs what they cancel. */
	public static void selfTest(net.minecraft.server.level.ServerPlayer sp) {
		VoicechatServerApi s = VoiceFx.server;
		VoicechatConnection conn = (VoicechatConnection) java.lang.reflect.Proxy.newProxyInstance(getClass0(), new Class<?>[]{VoicechatConnection.class}, (p, m, a) -> {
			if (m.getName().equals("getPlayer")) {
				return java.lang.reflect.Proxy.newProxyInstance(getClass0(), new Class<?>[]{de.maxhenkel.voicechat.api.ServerPlayer.class}, (p2, m2, a2) ->
						m2.getName().equals("getPlayer") ? sp : null);
			}
			return null;
		});
		boolean[] cancelled = new boolean[2];
		MicrophonePacketEvent mic = (MicrophonePacketEvent) java.lang.reflect.Proxy.newProxyInstance(getClass0(), new Class<?>[]{MicrophonePacketEvent.class}, (p, m, a) -> {
			if (m.getName().equals("getSenderConnection")) return conn;
			if (m.getName().equals("cancel")) { cancelled[0] = true; return true; }
			return null;
		});
		new VoicechatIntegration().onMicrophone(mic);
		EntitySoundPacketEvent snd = (EntitySoundPacketEvent) java.lang.reflect.Proxy.newProxyInstance(getClass0(), new Class<?>[]{EntitySoundPacketEvent.class}, (p, m, a) -> {
			if (m.getName().equals("getReceiverConnection")) return conn;
			if (m.getName().equals("cancel")) { cancelled[1] = true; return true; }
			return null;
		});
		new VoicechatIntegration().onSound(snd);
		DisabilityMod.LOGGER.info("[selftest] voicechat server={} micCancelled={} soundCancelled={}", s != null, cancelled[0], cancelled[1]);
	}

	private static ClassLoader getClass0() {
		return VoicechatIntegration.class.getClassLoader();
	}
}
