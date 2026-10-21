package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sprinting for more than ten seconds triggers an attack: one heart per second for three seconds, even if you stop. */
public class AsthmaDisability extends Disability {
	private static final int LIMIT_TICKS = 200;
	private static final int ATTACK_TICKS = 60;
	private final Map<UUID, Integer> sprinting = new HashMap<>();
	private final Map<UUID, Integer> attack = new HashMap<>();

	public AsthmaDisability() {
		super("asthma", "Asthma", "Sprinting for 10s causes an attack: 1 heart per second for 3s");
	}

	@Override
	public void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		Integer left = attack.get(id);
		if (left != null) {
			if (left % 20 == 0) player.hurtServer(player.level(), player.damageSources().drown(), 2.0f);
			if (left <= 1) attack.remove(id); else attack.put(id, left - 1);
			return;
		}
		if (!player.isSprinting()) {
			sprinting.remove(id);
			return;
		}
		if (sprinting.merge(id, 1, Integer::sum) > LIMIT_TICKS) {
			sprinting.remove(id);
			attack.put(id, ATTACK_TICKS);
		}
	}

	@Override
	public void onLeave(ServerPlayer player) {
		sprinting.remove(player.getUUID());
		attack.remove(player.getUUID());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		onLeave(player);
	}
}
