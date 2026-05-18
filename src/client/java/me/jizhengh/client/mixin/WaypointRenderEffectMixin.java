package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;

@Mixin(value = Waypoint.class, remap = false)
public class WaypointRenderEffectMixin {
	@Inject(method = "getActualColor", at = @At("HEAD"), cancellable = true, remap = false)
	private void sharedwaypoint$applyPulseColor(CallbackInfoReturnable<Integer> cir) {
		Waypoint self = (Waypoint) (Object) this;
		if (!SharedWaypointClientState.get().isSharedInstance(self)) {
			return;
		}
		double wave = (Math.sin(System.currentTimeMillis() / 220.0) + 1.0) * 0.5;
		int r = (int) (90 + 40 * wave);
		int g = (int) (210 + 30 * wave);
		int b = 255;
		cir.setReturnValue((r << 16) | (g << 8) | b);
	}
}
