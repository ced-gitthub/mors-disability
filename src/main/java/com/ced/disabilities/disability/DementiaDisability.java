package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.resources.Identifier;
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

/**
 * False memories: furnaces, chests and other containers you used are empty when you come back
 * (you only thought you filled them), plus sudden lapses that darken the world and change your held slot.
 */
public class DementiaDisability extends Disability {
	private static final Random RANDOM = new Random();
	private static final int AWAY_BLOCKS = 12;
	private static final int BACK_BLOCKS = 6;
	private static final int MAX_TRACKED = 8;
	private static boolean registered;

	/** Per player: containers they used (dimension + position) and whether they have walked away from them since. */
	private final Map<UUID, Map<String, Memory>> memories = new HashMap<>();

	/** Container each player just clicked; remembered only if its menu actually opened. */
	private final Map<UUID, BlockPos> pending = new HashMap<>();

	private record Memory(Identifier dim, BlockPos pos, boolean[] away) {}

	public DementiaDisability() {
		super("dementia", "Dementia", "You misremember: containers you used lose their contents while you're away");
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
		Identifier dim = player.level().dimension().identifier();
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
		if (RANDOM.nextInt(odds(4800)) != 0) return;
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0, true, false, false));
		int slot = RANDOM.nextInt(9);
		player.getInventory().setSelectedSlot(slot);
		player.connection.send(new ClientboundSetHeldSlotPacket(slot));
		player.sendSystemMessage(Component.literal("Where am I...?"), true);
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia fired");
	}

	/** Once the player has walked away from a used container and come back, its contents are gone. */
	private void forget(ServerPlayer player) {
		Map<String, Memory> map = memories.get(player.getUUID());
		if (map == null || map.isEmpty()) return;
		Identifier dim = player.level().dimension().identifier();
		var it = map.values().iterator();
		while (it.hasNext()) {
			Memory m = it.next();
			if (!m.dim().equals(dim)) { m.away()[0] = true; continue; }
			double d2 = player.distanceToSqr(m.pos().getX() + 0.5, m.pos().getY() + 0.5, m.pos().getZ() + 0.5);
			if (d2 > AWAY_BLOCKS * AWAY_BLOCKS) { m.away()[0] = true; continue; }
			if (!m.away()[0] || d2 > BACK_BLOCKS * BACK_BLOCKS) continue;
			it.remove();
			if (!(player.level().getBlockEntity(m.pos()) instanceof Container c)) continue;
			Container whole = c;
			var state = player.level().getBlockState(m.pos());
			if (state.getBlock() instanceof ChestBlock cb) {
				Container combined = ChestBlock.getContainer(cb, state, player.level(), m.pos(), true);
				if (combined != null) whole = combined;
			}
			if (whole.isEmpty()) continue;
			if (RANDOM.nextInt(100) >= (Boolean.getBoolean("disabilitystack.selftest") ? 100 : 70)) continue;
			whole.clearContent();
			whole.setChanged();
			player.sendSystemMessage(Component.literal("Wait... didn't I leave something in there?"), true);
			if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] dementia wiped container at {}", m.pos());
		}
	}

	@Override
	public void onLeave(ServerPlayer player) {
		memories.remove(player.getUUID());
		pending.remove(player.getUUID());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		memories.remove(player.getUUID());
		pending.remove(player.getUUID());
	}
}
