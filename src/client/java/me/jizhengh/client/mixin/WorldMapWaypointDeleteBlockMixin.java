package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.mods.SupportXaeroMinimap;
import xaero.map.mods.gui.Waypoint;

@Mixin(value = SupportXaeroMinimap.class, remap = false)
public class WorldMapWaypointDeleteBlockMixin {
	@Inject(method = "deleteWaypoint", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void sharedwaypoint$blockDeleteFromWorldMap(Waypoint waypoint, CallbackInfo ci) {
		if (SharedWaypointClientState.get().isSharedWorldMapWaypoint(waypoint)) {
			ci.cancel();
		}
	}
}
