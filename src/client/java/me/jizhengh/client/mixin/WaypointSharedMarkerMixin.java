package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;

@Mixin(value = Waypoint.class, remap = false)
public class WaypointSharedMarkerMixin {
	@Inject(method = "getInitials", at = @At("RETURN"), cancellable = true, remap = false)
	private void sharedwaypoint$addSharedBadge(CallbackInfoReturnable<String> cir) {
		Waypoint self = (Waypoint) (Object) this;
		if (!SharedWaypointClientState.get().isSharedByValue(self)) {
			return;
		}
		String initials = cir.getReturnValue();
		if (initials == null || initials.isEmpty()) {
			cir.setReturnValue("*");
			return;
		}
		if (!initials.startsWith("*")) {
			cir.setReturnValue("*" + initials);
		}
	}
}
