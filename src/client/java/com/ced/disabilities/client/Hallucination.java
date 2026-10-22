// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.client;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.Map;

public final class Hallucination {
	private static final List<EntityType<?>> GROUND = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER,
			EntityType.VILLAGER, EntityType.SPIDER, EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.WITCH,
			EntityType.ENDERMAN, EntityType.PILLAGER, EntityType.ZOMBIFIED_PIGLIN);
	private static final List<EntityType<?>> SEA = List.of(EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.COD,
			EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.DOLPHIN, EntityType.GUARDIAN);
	private static final List<EntityType<?>> AIR = List.of(EntityType.BAT, EntityType.PARROT, EntityType.BEE,
			EntityType.PHANTOM, EntityType.VEX, EntityType.ALLAY, EntityType.GHAST, EntityType.BLAZE);
	private static final Random RANDOM = new Random();

	private static final int SWAP_PERCENT = 20;

	private record Fake(EntityType<?> type, LivingEntity entity) {}

	private static final Map<Integer, Fake> FAKES = new HashMap<>();

	private static final Set<Integer> UNCHANGED = new HashSet<>();

	private Hallucination() {}

	public static boolean active(Minecraft mc) {
		return mc.player != null && mc.player.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("schizophrenia");
	}

	private static boolean isFake(Entity e) {
		for (Fake f : FAKES.values()) if (f.entity == e) return true;
		return false;
	}

	private static List<EntityType<?>> poolFor(Entity real) {
		if (SEA.contains(real.getType())) return SEA;
		if (AIR.contains(real.getType())) return AIR;
		return GROUND;
	}

	public static Entity replacement(Entity real) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || real == mc.player || real.isInvisible() || !(real instanceof LivingEntity living) || real instanceof ArmorStand || isFake(real)) return null;
		if (!(real instanceof Player) && !(real instanceof Mob)) return null;
		if (!active(mc)) return null;
		int id = real.getId();
		if (UNCHANGED.contains(id)) return null;
		Fake f = FAKES.get(id);
		if (f == null) {
			List<EntityType<?>> pool = poolFor(real);
			EntityType<?> t = pool.get(RANDOM.nextInt(pool.size()));
			if (RANDOM.nextInt(100) >= SWAP_PERCENT || t == real.getType() || !(t.create(mc.level, EntitySpawnReason.LOAD) instanceof LivingEntity le)) {
				UNCHANGED.add(id);
				return null;
			}
			le.setId(-1_000_000 - id);
			f = new Fake(t, le);
			FAKES.put(id, f);
			if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] hallucinate {} -> {}", real.getType().toShortString(), t.toShortString());
		}
		sync(living, f.entity);
		return f.entity;
	}

	private static void sync(LivingEntity real, LivingEntity fake) {
		fake.setPos(real.getX(), real.getY(), real.getZ());
		fake.xo = real.xo; fake.yo = real.yo; fake.zo = real.zo;
		fake.xOld = real.xOld; fake.yOld = real.yOld; fake.zOld = real.zOld;
		fake.setYRot(real.getYRot()); fake.yRotO = real.yRotO;
		fake.setXRot(real.getXRot()); fake.xRotO = real.xRotO;
		fake.yBodyRot = real.yBodyRot; fake.yBodyRotO = real.yBodyRotO;
		fake.yHeadRot = real.yHeadRot; fake.yHeadRotO = real.yHeadRotO;
		fake.tickCount = real.tickCount;
		fake.hurtTime = real.hurtTime;
		fake.swingTime = real.swingTime;
		fake.attackAnim = real.attackAnim;
		fake.walkAnimation.update(Math.min(1.0f, (float) Math.hypot(real.getX() - real.xo, real.getZ() - real.zo) * 4.0f), 0.4f, 1.0f);
	}

	public static void tick(Minecraft mc) {
		if (FAKES.isEmpty() && UNCHANGED.isEmpty()) return;
		if (mc.level == null || !active(mc)) { FAKES.clear(); UNCHANGED.clear(); return; }
		FAKES.keySet().removeIf(id -> mc.level.getEntity(id) == null);
		UNCHANGED.removeIf(id -> mc.level.getEntity(id) == null);
	}
}
