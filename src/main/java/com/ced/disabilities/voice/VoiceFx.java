// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.voice;

import com.ced.disabilities.DisabilityMod;
import com.ced.disabilities.DisabilityManager;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/** Voice chat effects. Only ever touched when Simple Voice Chat is installed. */
public final class VoiceFx {
	static volatile VoicechatApi api;
	static volatile VoicechatServerApi server;

	private static final Random RANDOM = new Random();
	private static final int FRAME = 960;
	private static final float RATE = 48000f;
	private static final boolean SELFTEST = Boolean.getBoolean("disabilitystack.selftest");

	private VoiceFx() {
	}

	public static boolean active() {
		return server != null;
	}

	public static boolean has(VoicechatConnection conn, String id) {
		if (conn == null || conn.getPlayer() == null) return false;
		return conn.getPlayer().getPlayer() instanceof ServerPlayer sp && DisabilityManager.owned(sp).contains(id);
	}

	private static boolean connected(ServerPlayer p) {
		VoicechatServerApi s = server;
		if (s == null) return false;
		VoicechatConnection c = s.getConnectionOf(p.getUUID());
		if (SELFTEST) DisabilityMod.LOGGER.info("[selftest] voice connection={} connected={} disabled={}", c != null,
				c != null && c.isConnected(), c != null && c.isDisabled());
		return c != null && c.isConnected() && !c.isDisabled();
	}

	/** Schizophrenia: a breathy voice nobody else can hear, a few blocks away, only for this player. */
	public static void whisper(ServerPlayer p) {
		VoicechatServerApi s = server;
		if (s == null || !connected(p) || DisabilityManager.owned(p).contains("deaf")) return;
		double ang = RANDOM.nextDouble() * Math.PI * 2;
		double dist = 3 + RANDOM.nextDouble() * 3;
		LocationalAudioChannel ch = s.createLocationalAudioChannel(UUID.randomUUID(), s.fromServerLevel((ServerLevel) p.level()),
				s.createPosition(p.getX() + Math.cos(ang) * dist, p.getY() + 1.5, p.getZ() + Math.sin(ang) * dist));
		if (ch == null) return;
		UUID target = p.getUUID();
		ch.setFilter(sp -> sp.getUuid().equals(target));
		ch.setDistance(12f);
		play(s, ch, new Voice(false, 1.0 + RANDOM.nextDouble() * 1.8, 0.55f), "whisper");
	}

	/** Tourette's: a short vocal tic from the player's own position, heard by people nearby. */
	public static void tic(ServerPlayer p) {
		VoicechatServerApi s = server;
		if (s == null || !connected(p) || DisabilityManager.owned(p).contains("mute")) return;
		EntityAudioChannel ch = s.createEntityAudioChannel(UUID.randomUUID(), s.fromEntity(p));
		if (ch == null) return;
		play(s, ch, new Voice(true, 0.25 + RANDOM.nextDouble() * 0.2, 0.9f), "tic");
	}

	private static void play(VoicechatServerApi s, AudioChannel ch, Voice voice, String what) {
		OpusEncoder enc = s.createEncoder();
		AudioPlayer player = s.createAudioPlayer(ch, enc, voice);
		player.setOnStopped(() -> {
			enc.close();
			if (SELFTEST) DisabilityMod.LOGGER.info("[selftest] voice {} finished frames={}", what, voice.frames);
		});
		player.startPlaying();
		if (SELFTEST) DisabilityMod.LOGGER.info("[selftest] voice {} started", what);
	}

	/** Speech-like noise: a voiced or breathy source pushed through two formant filters that hop between vowels. */
	private static final class Voice implements Supplier<short[]> {
		private static final double[][] VOWELS = {{730, 1090}, {270, 2290}, {300, 870}, {530, 1840}, {570, 840}, {660, 1720}};
		private final boolean voiced;
		private final int totalFrames;
		private final float gain;
		private final double f0;
		int frames;
		private int segLeft;
		private int segLen;
		private double phase;
		private final Biquad a = new Biquad();
		private final Biquad b = new Biquad();

		Voice(boolean voiced, double seconds, float gain) {
			this.voiced = voiced;
			this.totalFrames = Math.max(2, (int) (seconds * 50));
			this.gain = gain;
			this.f0 = voiced ? 90 + RANDOM.nextInt(80) : 0;
		}

		@Override
		public short[] get() {
			if (frames >= totalFrames) return null;
			short[] out = new short[FRAME];
			for (int i = 0; i < FRAME; i++) {
				if (segLeft <= 0) {
					double[] v = VOWELS[RANDOM.nextInt(VOWELS.length)];
					a.band(v[0], 6);
					b.band(v[1], 9);
					segLen = segLeft = (int) (RATE * (voiced ? 0.12 : 0.14 + RANDOM.nextDouble() * 0.1));
				}
				double src;
				if (voiced) {
					phase += f0 / RATE;
					if (phase >= 1) phase -= 1;
					src = phase * 2 - 1;
				} else {
					src = RANDOM.nextDouble() * 2 - 1;
				}
				double syl = Math.sin(Math.PI * (1.0 - (double) segLeft / segLen));
				double env = syl * syl * Math.min(1.0, Math.min(frames + 1, totalFrames - frames) / 3.0);
				out[i] = (short) Math.max(-32000, Math.min(32000, (a.run(src) + b.run(src) * 0.6) * env * gain * 9000));
				segLeft--;
			}
			frames++;
			return out;
		}
	}

	private static final class Biquad {
		private double b0, b2, a1, a2, x1, x2, y1, y2;

		void band(double freq, double q) {
			double w = 2 * Math.PI * freq / RATE;
			double alpha = Math.sin(w) / (2 * q);
			double a0 = 1 + alpha;
			b0 = alpha / a0;
			b2 = -alpha / a0;
			a1 = -2 * Math.cos(w) / a0;
			a2 = (1 - alpha) / a0;
		}

		double run(double x) {
			double y = b0 * x + b2 * x2 - a1 * y1 - a2 * y2;
			x2 = x1;
			x1 = x;
			y2 = y1;
			y1 = y;
			return y;
		}
	}
}
