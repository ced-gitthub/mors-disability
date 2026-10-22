// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CancerDisability extends Disability {
	private static final UUID MODIFIER = UUID.nameUUIDFromBytes("mors-disability:cancer".getBytes(java.nio.charset.StandardCharsets.UTF_8));
	private static final int INTERVAL_TICKS = 1200;
	private static final double STEP = 1.0;
	private static final double MIN_HEALTH = 2.0;

	public CancerDisability() {
		super("cancer", "Cancer", "Constant weakness, a body that wastes away and waves of nausea");
	}

	@Override
	public void tick(ServerPlayer player) {
		var weak = player.getEffect(MobEffects.WEAKNESS);
		if (weak == null || !weak.isInfiniteDuration()) {
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
		}
		if (player.tickCount % 6000 == 3000) player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false, false));
		AttributeInstance max = player.getAttribute(Attributes.MAX_HEALTH);
		if (max == null) return;
		AttributeModifier current = max.getModifier(MODIFIER);
		double loss = current == null ? 0 : -current.getAmount();
		if (current == null || player.tickCount % INTERVAL_TICKS == 0) {
			double next = current == null ? 0 : loss + STEP;
			double base = max.getBaseValue();
			if (base - next < MIN_HEALTH) next = base - MIN_HEALTH;
			if (current == null || next != loss) {
				max.removeModifier(MODIFIER);
				max.addPermanentModifier(
						new AttributeModifier(MODIFIER, "mors-disability:cancer", -next, AttributeModifier.Operation.ADDITION));
			}
		}
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		var weak = player.getEffect(MobEffects.WEAKNESS);
		if (weak != null && weak.isInfiniteDuration()) player.removeEffect(MobEffects.WEAKNESS);
		AttributeInstance max = player.getAttribute(Attributes.MAX_HEALTH);
		if (max != null) max.removeModifier(MODIFIER);
	}
}
