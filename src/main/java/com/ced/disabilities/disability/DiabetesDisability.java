package com.ced.disabilities.disability;

import net.minecraft.server.level.ServerPlayer;

/** Hunger drains much faster. */
public class DiabetesDisability extends Disability {
	public DiabetesDisability() {
		super("diabetes", "Diabetes", "You get hungry much faster");
	}

	@Override
	public void tick(ServerPlayer player) {
		player.causeFoodExhaustion(0.01f);
	}
}