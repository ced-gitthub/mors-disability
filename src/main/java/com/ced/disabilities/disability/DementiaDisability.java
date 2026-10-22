// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DementiaDisability extends Disability {
	private static final Random RANDOM = new Random();
	private static final int AWAY_BLOCKS = 12;
	private static final int BACK_BLOCKS = 6;
	private static final int MAX_TRACKED = 8;
	private static boolean registered;

	private final Map<UUID, Map<String, Memory>> memories = new HashMap<>();

	private final Map<UUID, BlockPos> pending = new HashMap<>();

	private static final int LAPSE_TICKS = 100;

	private static final class Lapse {
		final net.minecraft.world.phys.Vec3 origin;
		int ticks;
		Lapse(net.minecraft.world.phys.Vec3 origin, int ticks) { this.origin = origin; this.ticks = ticks; }
	}

	private final Map<UUID, Lapse> lapses = new HashMap<>();

	private record Memory(ResourceLocation dim, BlockPos pos, boolean[] away) {}

	public DementiaDisability() {
		super("dementia", "Dementia", "Memory loss and confusion: forgotten containers, and 'Where am I?' blackouts");
		if (!registered) {
			registered = true;
			UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
				if (player instanceof ServerPlayer sp && DisabilityManager.owned(sp).contains("dementia")) {
					BlockPos pos = hit.getBlockPos();
					if (level.getBlockEntity(pos) instanceof Container) pending.put(sp.getUUID(), pos.immutable());
				}
				return InteractionResult.PASS;
			});
		}
	}

	private void remember(ServerPlayer player, BlockPos pos) {
		Map<String, Memory> map = memories.computeIfAbsent(player.getUUID(), k -> new LinkedHashMap<>());
		ResourceLocation dim = player.level().dimension().location();
		String key = dim + "@" + pos.asLong();
		map.remove(key);
		map.put(key, new Memory(dim, pos.immutable(), new boolean[1]));
		while (map.size() > MAX_TRACKED) map.remove(map.keySet().iterator().next());
	}

	@Override
	public void tick(ServerPlayer player) {
		BlockPos clicked = pending.remove(player.getUUID());
		if (clicked != null && player.containerMenu != player.inventoryMenu) remember(player, clicked);
		forget(player);
		Lapse lapse = lapses.get(player.getUUID());
		if (lapse != null) {
			if (--lapse.ticks > 0) return;
			lapses.remove(player.getUUID());
			displace(player, lapse.origin);
			return;
		}
		if (RANDOM.nextInt(odds(4800)) != 0) return;
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, LAPSE_TICKS + 20, 0, true, false, false));
		lapses.put(player.getUUID(), new Lapse(player.position(), LAPSE_TICKS));
		int slot = RANDOM.nextInt(9);
		player.getInventory().selected = slot;
		player.connection.send(new ClientboundSetCarriedItemPacket(slot));
		player.sendSystemMessage(Component.literal("Where am I...?"), true);
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia fired");
	}

	private void displace(ServerPlayer player, net.minecraft.world.phys.Vec3 origin) {
		var level = player.level();
		for (int i = 0; i < 12; i++) {
			double a = RANDOM.nextDouble() * Math.PI * 2, r = 1 + RANDOM.nextDouble();
			double x = origin.x + Math.cos(a) * r, z = origin.z + Math.sin(a) * r;
			var box = player.getBoundingBox().move(x - player.getX(), origin.y - player.getY(), z - player.getZ());
			if (!level.noCollision(player, box) || level.noCollision(player, box.move(0, -1, 0))) continue;
			player.teleportTo(x, origin.y, z);
			if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia displaced {} from origin", String.format("%.2f", Math.hypot(x - origin.x, z - origin.z)));
			return;
		}
	}

	private void forget(ServerPlayer player) {
		Map<String, Memory> map = memories.get(player.getUUID());
		if (map == null || map.isEmpty()) return;
		ResourceLocation dim = player.level().dimension().location();
		var it = map.values().iterator();
		while (it.hasNext()) {
			Memory m = it.next();
			if (!m.dim().equals(dim)) { m.away()[0] = true; continue; }
			double d2 = player.distanceToSqr(m.pos().getX() + 0.5, m.pos().getY() + 0.5, m.pos().getZ() + 0.5);
			if (d2 > AWAY_BLOCKS * AWAY_BLOCKS) { m.away()[0] = true; continue; }
			if (!m.away()[0] || d2 > BACK_BLOCKS * BACK_BLOCKS) continue;
			it.remove();
			if (!(player.level().getBlockEntity(m.pos()) instanceof Container c)) continue;
			Container other = null;
			var state = player.level().getBlockState(m.pos());
			if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != net.minecraft.world.level.block.state.properties.ChestType.SINGLE) {
				for (var dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
					var ns = player.level().getBlockState(m.pos().relative(dir));
					if (ns.is(state.getBlock()) && ns.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING) && ns.getValue(ChestBlock.TYPE) != state.getValue(ChestBlock.TYPE)
							&& player.level().getBlockEntity(m.pos().relative(dir)) instanceof Container o) { other = o; break; }
				}
			}
			if (c.isEmpty() && (other == null || other.isEmpty())) continue;
			if (RANDOM.nextInt(100) >= (Boolean.getBoolean("disabilitystack.selftest") ? 100 : 70)) continue;
			c.clearContent();
			c.setChanged();
			if (other != null) { other.clearContent(); other.setChanged(); }
			player.sendSystemMessage(Component.literal("Wait... didn't I leave something in there?"), true);
			if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia wiped container at {}", m.pos());
		}
	}

	@Override
	public void onLeave(ServerPlayer player) {
		memories.remove(player.getUUID());
		pending.remove(player.getUUID());
		lapses.remove(player.getUUID());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		memories.remove(player.getUUID());
		pending.remove(player.getUUID());
		lapses.remove(player.getUUID());
	}
}
