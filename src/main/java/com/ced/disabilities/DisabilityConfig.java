package com.ced.disabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** config/mors-disability.json. Edit the file and run /disability reload, or restart. */
public class DisabilityConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mors-disability.json");

	/** Minutes between new disabilities for each player. 0 or less turns the timer off. */
	public double intervalMinutes = 10.0;
	/**
	 * Optional per-severity timers ("mild", "moderate", "severe"), in minutes, 0 = off for that severity.
	 * Defaults: mild 10, moderate 5, severe 3. Leave empty to use one timer (intervalMinutes) that hands out any disability.
	 */
	public java.util.Map<String, Double> severityMinutes = new java.util.LinkedHashMap<>(java.util.Map.of("mild", 10.0, "moderate", 5.0, "severe", 3.0));
	/** "stack" adds the new disability on top; "replace" first removes the player's disabilities of the same timer. */
	public String mode = "replace";
	/** When a timer fires: chance of handing out 2 disabilities at once (0-1), and of 3 at once. Otherwise 1. */
	public double chanceTwo = 0.25;
	public double chanceThree = 0.07;
	/** Ids of disabilities that must never be handed out by the timer or /disability next. */
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
