package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityConfig;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Disabilities {
	private static final Map<String, Disability> ALL = new LinkedHashMap<>();
	private static final Random RANDOM = new Random();

	static {
		register(new BlindDisability());
		register(new AttributeDisability("brittle_bones", "Brittle Bones", "Double fall damage",
				Attributes.FALL_DAMAGE_MULTIPLIER, 1.0, Operation.ADD_MULTIPLIED_BASE));
		register(new AttributeDisability("short", "Short", "Half a block shorter",
				Attributes.SCALE, -0.28, Operation.ADD_MULTIPLIED_BASE));
		register(new CancerDisability());
		register(new InsomniaDisability());
		register(new CompositeDisability("arthritis", "Arthritis", "Mining and attacking take longer",
				new AttributeDisability("arthritis_attack", "", "", Attributes.ATTACK_SPEED, -0.5, Operation.ADD_MULTIPLIED_BASE),
				new AttributeDisability("arthritis_mining", "", "", Attributes.BLOCK_BREAK_SPEED, -0.5, Operation.ADD_MULTIPLIED_BASE)));
		register(new KleptomaniaDisability());
		register(new LactoseDisability());
		register(new AsthmaDisability());
		register(new PollenDisability());
		register(new MuteDisability());
		register(new AmputatedOffhandDisability());
		register(new CompositeDisability("amputated_leg", "Amputated Leg", "You move and jump poorly",
				new AttributeDisability("amputated_leg_speed", "", "", Attributes.MOVEMENT_SPEED, -0.4, Operation.ADD_MULTIPLIED_BASE),
				new AttributeDisability("amputated_leg_jump", "", "", Attributes.JUMP_STRENGTH, -0.3, Operation.ADD_MULTIPLIED_BASE)));
		register(new NarcolepsyDisability());
		register(new TouretteDisability());
		register(new CerebralPalsyDisability());
		register(new DiabetesDisability());
		register(new AdhdDisability());
		register(new DeafDisability());
		register(new ParkinsonDisability());
		register(new ColorBlindDisability());
		register(new SchizophreniaDisability());
		register(new SeizuresDisability());
		register(new OcdDisability());
		register(new DyslexiaDisability());
		register(new DementiaDisability());
		register(new VertigoDisability());
		register(new ProsopagnosiaDisability());
		register(new CompositeDisability("scoliosis", "Scoliosis", "A bent spine slows you and makes landings hurt",
				new AttributeDisability("scoliosis_speed", "", "", Attributes.MOVEMENT_SPEED, -0.15, Operation.ADD_MULTIPLIED_BASE),
				new AttributeDisability("scoliosis_fall", "", "", Attributes.SAFE_FALL_DISTANCE, -2.0, Operation.ADD_VALUE)));
		register(new AnosognosiaDisability());
	}

	/** Registers the Fabric events some disabilities rely on. Call once at startup. */
	public static void init() {
		InsomniaDisability.registerEvents();
		MuteDisability.registerEvents();
		AmputatedOffhandDisability.registerEvents();
		DyslexiaDisability.registerEvents();
	}

	private Disabilities() {
	}

	private static void register(Disability d) {
		ALL.put(d.id, d);
	}

	public static Disability get(String id) {
		return ALL.get(id);
	}

	public static Iterable<Disability> all() {
		return Collections.unmodifiableCollection(ALL.values());
	}

	public static List<String> ids() {
		return new ArrayList<>(ALL.keySet());
	}

	public static final List<String> SEVERITIES = List.of("mild", "moderate", "severe");

	/** How much each disability disrupts play; each severity can have its own timer in the config. */
	private static final Map<String, String> SEVERITY = new java.util.HashMap<>();

	static {
		for (String id : new String[]{"short", "kleptomania", "lactose_intolerance", "pollen_allergy", "insomnia", "adhd",
				"ocd", "color_blind", "dyslexia", "diabetes", "anosognosia", "prosopagnosia"}) SEVERITY.put(id, "mild");
		for (String id : new String[]{"brittle_bones", "arthritis", "asthma", "mute", "tourette", "cerebral_palsy", "parkinsons",
				"vertigo", "scoliosis", "dementia", "schizophrenia", "deaf"}) SEVERITY.put(id, "moderate");
		for (String id : new String[]{"blind", "cancer", "amputated_leg", "amputated_offhand", "narcolepsy", "seizures"}) SEVERITY.put(id, "severe");
	}

	public static String severity(String id) {
		return SEVERITY.getOrDefault(id, "moderate");
	}

	/** A random enabled disability the player does not own yet, or null if none is left. */
	public static Disability pickRandom(List<String> owned) {
		return pickRandom(owned, null);
	}

	/** Same, limited to one severity; a null severity means any. */
	public static Disability pickRandom(List<String> owned, String severity) {
		List<Disability> candidates = new ArrayList<>();
		for (Disability d : ALL.values()) {
			if (severity != null && !severity.equals(severity(d.id))) continue;
			if (!owned.contains(d.id) && !DisabilityConfig.get().disabledDisabilities.contains(d.id)) {
				candidates.add(d);
			}
		}
		return candidates.isEmpty() ? null : candidates.get(RANDOM.nextInt(candidates.size()));
	}
}