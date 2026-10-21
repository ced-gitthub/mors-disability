package com.ced.disabilities.client.mixin;

import com.ced.disabilities.DisabilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Amputated Offhand: the left arm (and sleeve) is not drawn. */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	/** Models whose arm we hid, so it is shown again afterwards and no other model is touched. */
	private static final java.util.Set<PlayerModel> HIDDEN = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void mors$hideOffhandArm(AvatarRenderState state, CallbackInfo ci) {
		var level = Minecraft.getInstance().level;
		Entity e = level == null ? null : level.getEntity(state.id);
		PlayerModel self = (PlayerModel) (Object) this;
		if (e != null && e.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("amputated_offhand")) {
			self.leftArm.visible = false;
			self.leftSleeve.visible = false;
			HIDDEN.add(self);
		} else if (HIDDEN.remove(self)) {
			self.leftArm.visible = true;
			self.leftSleeve.visible = true;
		}
	}
}
