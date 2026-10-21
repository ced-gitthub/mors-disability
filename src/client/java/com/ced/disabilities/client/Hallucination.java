package com.ced.disabilities.client;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Schizophrenia: nearby mobs and players are drawn as other ground mobs (villager <-> zombie, players too). */
public final class Hallucination {
	private static final List<EntityType<?>> POOL = List.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.CREEPER,
			EntityTypes.VILLAGER, EntityTypes.SPIDER, EntityTypes.PIG, EntityTypes.COW, EntityTypes.SHEEP, EntityTypes.WITCH,
			EntityTypes.ENDERMAN, EntityTypes.PILLAGER, EntityTypes.ZOMBIFIED_PIGLIN);

	private record Fake(EntityType<?> type, LivingEntity entity) {}

	private static final Map<Integer, Fake> FAKES = new HashMap<>();

	private Hallucination() {}

	public static boolean active(Minecraft mc) {
		return mc.player != null && mc.player.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("schizophrenia");
	}

	private static boolean isFake(Entity e) {
		for (Fake f : FAKES.values()) if (f.entity == e) return true;
		return false;
	}

	private static EntityType<?> target(Entity real, long epoch) {
		int h = Integer.rotateLeft(real.getId() * 0x9E3779B1 + (int) epoch * 0x85EBCA6B, 13) * 0xC2B2AE35;
		h ^= h >>> 15;
		if (Math.floorMod(h, 4) == 0) return null;
		EntityType<?> t = POOL.get(Math.floorMod(h >>> 3, POOL.size()));
		return t == real.getType() ? null : t;
	}

	/** The stand-in to draw instead of {@code real}, or null to draw it normally. */
	public static Entity replacement(Entity real) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || real == mc.player || !(real instanceof LivingEntity living) || real instanceof ArmorStand || isFake(real)) return null;
		if (!(real instanceof Player) && !(real instanceof Mob)) return null;
		if (!active(mc)) return null;
		EntityType<?> t = target(real, (mc.level.getGameTime() + real.getId() * 97L) / 200);
		if (t == null) return null;
		Fake f = FAKES.get(real.getId());
		if (f == null || f.type != t) {
			if (!(t.create(mc.level, EntitySpawnReason.LOAD) instanceof LivingEntity le)) return null;
			le.setId(-1_000_000 - real.getId());
				f = new Fake(t, le);
			FAKES.put(real.getId(), f);
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

	/** Drops stand-ins for entities that are gone, or all of them when the effect is not active. */
	public static void tick(Minecraft mc) {
		if (FAKES.isEmpty()) return;
		if (mc.level == null || !active(mc)) { FAKES.clear(); return; }
		FAKES.keySet().removeIf(id -> mc.level.getEntity(id) == null);
	}
}
