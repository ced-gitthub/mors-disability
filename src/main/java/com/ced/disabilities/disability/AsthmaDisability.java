package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sprinting for more than ten seconds makes you choke until you stop. */
public class AsthmaDisability extends Disability {
	private static final int LIMIT_TICKS = 200;
	private final Map<UUID, Integer> sprinting = new HashMap<>();

	public AsthmaDisability() {
		super("asthma", "Asthma", "Sprinting too long makes you suffocate");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (!player.isSprinting()) {
			sprinting.remove(player.getUUID());
			return;
		}
		int t = sprinting.merge(player.getUUID(), 1, Integer::sum);
		if (t > LIMIT_TICKS && t % 20 == 0) {
			player.hurtServer(player.level(), player.damageSources().drown(), 1.0f);
		}
	}

	@Override
	public void onLeave(ServerPlayer player) {
		sprinting.remove(player.getUUID());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		sprinting.remove(player.getUUID());
	}
}
