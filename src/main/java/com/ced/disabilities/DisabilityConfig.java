// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DisabilityConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mors-disability.json");

	public double intervalMinutes = 10.0;

	public java.util.Map<String, Double> severityMinutes = new java.util.LinkedHashMap<>(java.util.Map.of("mild", 10.0, "moderate", 5.0, "severe", 3.0));

	public String mode = "replace";

	public double chanceTwo = 0.25;
	public double chanceThree = 0.07;

	public List<String> disabledDisabilities = new ArrayList<>();

	private static DisabilityConfig instance = new DisabilityConfig();

	public static DisabilityConfig get() {
		return instance;
	}

	public static int toTicks(double minutes) {
		return minutes <= 0 ? 0 : (int) Math.min(20 * 60 * 60 * 24 * 30L, Math.max(20, Math.round(minutes * 60 * 20)));
	}

	public int intervalTicks() {
		return intervalMinutes <= 0 ? 0 : (int) Math.min(20 * 60 * 60 * 24 * 30L, Math.max(20, Math.round(intervalMinutes * 60 * 20)));
	}

	public int bootstrapTicks() {
		int t = intervalTicks();
		if (t > 0) return t;
		for (double m : severityMinutes.values()) {
			int v = toTicks(m);
			if (v > 0 && (t == 0 || v < t)) t = v;
		}
		return t;
	}

	public static void load() {
		boolean writeBack = true;
		try {
			if (Files.exists(FILE)) {
				DisabilityConfig loaded = GSON.fromJson(Files.readString(FILE), DisabilityConfig.class);
				if (loaded != null) {
					if (loaded.disabledDisabilities == null) loaded.disabledDisabilities = new ArrayList<>();
					if (loaded.severityMinutes == null) loaded.severityMinutes = new java.util.LinkedHashMap<>();
					if (!"stack".equals(loaded.mode)) loaded.mode = "replace";
						loaded.chanceTwo = Math.max(0, Math.min(1, loaded.chanceTwo));
						loaded.chanceThree = Math.max(0, Math.min(1, loaded.chanceThree));
					instance = loaded;
				}
			}
		} catch (IOException | RuntimeException e) {
			DisabilityMod.LOGGER.error("Could not read {}, keeping current values and not overwriting it", FILE, e);
			writeBack = false;
		}
		if (writeBack) save();
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (IOException e) {
			DisabilityMod.LOGGER.error("Could not write {}", FILE, e);
		}
	}
}
