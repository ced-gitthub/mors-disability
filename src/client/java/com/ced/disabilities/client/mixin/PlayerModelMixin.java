// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.client.mixin;

import com.ced.disabilities.DisabilityManager;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

	private static final java.util.Set<PlayerModel<?>> HIDDEN = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	private void mors$hideOffhandArm(LivingEntity e, float a, float b, float c, float d, float f, CallbackInfo ci) {
		PlayerModel<?> self = (PlayerModel<?>) (Object) this;
		boolean arm = DisabilityManager.ownedOf(e).contains("amputated_offhand");
		boolean leg = DisabilityManager.ownedOf(e).contains("amputated_leg");
		if (arm || leg) {
			if (arm) { self.leftArm.visible = false; self.leftSleeve.visible = false; }
			if (leg) { self.leftLeg.visible = false; self.leftPants.visible = false; }
			HIDDEN.add(self);
		} else if (HIDDEN.remove(self)) {
			self.leftArm.visible = true;
			self.leftSleeve.visible = true;
			self.leftLeg.visible = true;
			self.leftPants.visible = true;
		}
	}
}
