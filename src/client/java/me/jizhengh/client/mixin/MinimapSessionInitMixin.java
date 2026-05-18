package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointSessionHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.minimap.module.MinimapSession;

@Mixin(value = MinimapSession.class, remap = false)
public abstract class MinimapSessionInitMixin {
	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void sharedwaypoint$captureSessionEarly(CallbackInfo ci) {
		SharedWaypointSessionHolder.set((MinimapSession) (Object) this);
	}
}
