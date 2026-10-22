// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeDisability extends Disability {
	private final Holder<Attribute> attribute;
	private final double amount;
	private final AttributeModifier.Operation operation;
	private final Identifier modifierId;

	public AttributeDisability(String id, String name, String description, Holder<Attribute> attribute,
							   double amount, AttributeModifier.Operation operation) {
		super(id, name, description);
		this.attribute = attribute;
		this.amount = amount;
		this.operation = operation;
		this.modifierId = Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, id);
	}

	@Override
	public void tick(ServerPlayer player) {

		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null && !instance.hasModifier(modifierId)) {
			instance.addTransientModifier(new AttributeModifier(modifierId, amount, operation));
		}
	}

	@Override
	public void onRemove(ServerPlayer player) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null) instance.removeModifier(modifierId);
	}
}