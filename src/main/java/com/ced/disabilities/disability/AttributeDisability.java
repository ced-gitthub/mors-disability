// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeDisability extends Disability {
	private final Attribute attribute;
	private final double amount;
	private final AttributeModifier.Operation operation;
	private final UUID modifierId;
	private final String modifierName;

	public AttributeDisability(String id, String name, String description, Attribute attribute,
							   double amount, AttributeModifier.Operation operation) {
		super(id, name, description);
		this.attribute = attribute;
		this.amount = amount;
		this.operation = operation;
		this.modifierName = "mors-disability:" + id;
		this.modifierId = UUID.nameUUIDFromBytes(modifierName.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	@Override
	public void tick(ServerPlayer player) {

		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null && instance.getModifier(modifierId) == null) {
			instance.addTransientModifier(new AttributeModifier(modifierId, modifierName, amount, operation));
		}
	}

	@Override
	public void onRemove(ServerPlayer player) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null) instance.removeModifier(modifierId);
	}
}