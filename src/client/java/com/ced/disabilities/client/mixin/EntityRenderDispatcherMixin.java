// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.client.mixin;

import com.ced.disabilities.client.Hallucination;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void mors$hallucinate(E entity, double x, double y, double z, float yaw, float partialTick,
													 PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
		Entity fake = Hallucination.replacement(entity);
		if (fake == null) return;
		((EntityRenderDispatcher) (Object) this).render(fake, x, y, z, yaw, partialTick, pose, buffers, light);
		ci.cancel();
	}
}
