package me.jizhengh.client.mixin;

import me.jizhengh.client.render.SharedWaypointHaloState;
import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.mods.gui.Waypoint;

@Mixin(value = Waypoint.class, remap = false)
public class WorldMapWaypointColorMixin {
	@Inject(method = "getColor", at = @At("HEAD"), cancellable = true, remap = false)
	private void sharedwaypoint$whiteHaloColor(CallbackInfoReturnable<Integer> cir) {
		Waypoint self = (Waypoint) (Object) this;
		if (SharedWaypointHaloState.isHaloPass() && SharedWaypointClientState.get().isSharedWorldMapWaypoint(self)) {
			cir.setReturnValue(0xFFFFFF);
		}
	}
}
