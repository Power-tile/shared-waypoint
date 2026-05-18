package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;

@Mixin(value = Waypoint.class, remap = false)
public class WaypointDebugBlackMixin {
	@Inject(method = "getWaypointColor", at = @At("HEAD"), cancellable = true, remap = false)
	private void sharedwaypoint$forceBlackForShared(CallbackInfoReturnable<WaypointColor> cir) {
		Waypoint self = (Waypoint) (Object) this;
		if (SharedWaypointClientState.get().isSharedByValue(self)) {
			cir.setReturnValue(WaypointColor.BLACK);
		}
	}
}
