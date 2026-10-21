package com.ced.disabilities.client.mixin;

import com.ced.disabilities.client.Hallucination;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Schizophrenia: draw nearby mobs and players as a different ground mob. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void mors$hallucinate(E entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
		Entity fake = Hallucination.replacement(entity);
		if (fake != null) cir.setReturnValue(((EntityRenderDispatcher) (Object) this).extractEntity(fake, partialTick));
	}
}
