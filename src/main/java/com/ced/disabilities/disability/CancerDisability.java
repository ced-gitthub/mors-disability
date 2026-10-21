package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Max health wastes away by half a heart every minute, down to one heart. */
public class CancerDisability extends Disability {
	private static final Identifier MODIFIER = Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "cancer");
	private static final int INTERVAL_TICKS = 1200;
	private static final double STEP = 1.0;
	private static final double MIN_HEALTH = 2.0;

	public CancerDisability() {
		super("cancer", "Cancer", "Your max health slowly wastes away");
	}

	@Override
	public void tick(ServerPlayer player) {
		AttributeInstance max = player.getAttribute(Attributes.MAX_HEALTH);
		if (max == null) return;
		AttributeModifier current = max.getModifier(MODIFIER);
		double loss = current == null ? 0 : -current.amount();
		if (current == null || player.tickCount % INTERVAL_TICKS == 0) {
			double next = current == null ? 0 : loss + STEP;
			double base = max.getBaseValue();
			if (base - next < MIN_HEALTH) next = base - MIN_HEALTH;
			if (current == null || next != loss) {
				max.addOrReplacePermanentModifier(
						new AttributeModifier(MODIFIER, -next, AttributeModifier.Operation.ADD_VALUE));
			}
		}
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	@Override
	public void onRemove(ServerPlayer player) {
		AttributeInstance max = player.getAttribute(Attributes.MAX_HEALTH);
		if (max != null) max.removeModifier(MODIFIER);
	}
}
