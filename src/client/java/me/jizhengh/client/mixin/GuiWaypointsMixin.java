package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import me.jizhengh.client.shared.SharedWaypointSessionHolder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.gui.GuiWaypointSets;
import xaero.common.gui.GuiWaypoints;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

@Mixin(value = GuiWaypoints.class, remap = false)
public abstract class GuiWaypointsMixin {
	@Shadow
	private MinimapWorld displayedWorld;

	@Shadow
	private GuiWaypointSets sets;

	@Shadow
	private Button clearButton;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void sharedwaypoint$rememberSession(HudMod mod, MinimapSession session, Screen parent, Screen escape, CallbackInfo ci) {
		SharedWaypointSessionHolder.set(session);
	}

	@Inject(method = {"init", "method_25426", "updateButtons"}, at = @At("TAIL"), remap = false, require = 0)
	private void sharedwaypoint$removeClearSetButton(CallbackInfo ci) {
		if (clearButton == null) {
			return;
		}
		clearButton.active = false;
		try {
			clearButton.visible = false;
		} catch (Throwable ignored) {
			// Mapping-safe fallback: if "visible" is unavailable, keeping it inactive still prevents crashes.
		}
	}

	@Redirect(
		method = "lambda$init$5",
		at = @At(
			value = "INVOKE",
			target = "Lxaero/hud/minimap/waypoint/set/WaypointSet;remove(Lxaero/common/minimap/waypoints/Waypoint;)V",
			remap = false
		),
		require = 0,
		remap = false
	)
	private void sharedwaypoint$sendDeleteOnRemove(WaypointSet set, Waypoint waypoint) {
		String setId = sets.getCurrentSetKey();
		String worldPath = SharedWaypointClientState.worldPath(displayedWorld);
		SharedWaypointClientState state = SharedWaypointClientState.get();
		if (state.isSharedInContext(worldPath, setId, waypoint)) {
			state.sendDelete(state.toEntry(worldPath, setId, waypoint).id());
		}
		set.remove(waypoint);
	}
}
