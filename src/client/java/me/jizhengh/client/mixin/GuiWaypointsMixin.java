package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import me.jizhengh.client.shared.SharedWaypointPermission;
import me.jizhengh.client.shared.SharedWaypointSessionHolder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

import java.util.ArrayList;
import java.lang.reflect.Method;

@Mixin(value = GuiWaypoints.class, remap = false)
public abstract class GuiWaypointsMixin {
	@Shadow
	private MinimapWorld displayedWorld;

	@Shadow
	private GuiWaypointSets sets;

	@Shadow
	private Button deleteButton;

	@Shadow
	private Button editButton;

	@Shadow
	private Button shareButton;

	@Shadow
	private Button clearButton;

	@Shadow
	protected abstract ArrayList<Waypoint> getSelectedWaypointsList();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void sharedwaypoint$rememberSession(HudMod mod, MinimapSession session, Screen parent, Screen escape, CallbackInfo ci) {
		SharedWaypointSessionHolder.set(session);
	}

	@Inject(method = {"init", "method_25426", "updateButtons"}, at = @At("TAIL"), remap = false, require = 0)
	private void sharedwaypoint$removeClearSetButton(CallbackInfo ci) {
		if (clearButton == null) {
			sharedwaypoint$enforceSharedSelectionLocks();
			return;
		}
		clearButton.active = false;
		try {
			clearButton.visible = false;
		} catch (Throwable ignored) {
			// Mapping-safe fallback: if "visible" is unavailable, keeping it inactive still prevents crashes.
		}
		sharedwaypoint$enforceSharedSelectionLocks();
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
			return;
		}
		set.remove(waypoint);
	}

	@Unique
	private void sharedwaypoint$enforceSharedSelectionLocks() {
		if (deleteButton == null) {
			return;
		}
		if (displayedWorld == null || sets == null) {
			sharedwaypoint$setButtonTooltip(deleteButton, null);
			sharedwaypoint$setButtonTooltip(shareButton, null);
			return;
		}
		ArrayList<Waypoint> selected = getSelectedWaypointsList();
		if (selected == null || selected.isEmpty()) {
			sharedwaypoint$setButtonTooltip(deleteButton, null);
			sharedwaypoint$setButtonTooltip(shareButton, null);
			return;
		}
		String setId = sets.getCurrentSetKey();
		String worldPath = SharedWaypointClientState.worldPath(displayedWorld);
		SharedWaypointClientState state = SharedWaypointClientState.get();
		boolean hasSharedSelected = false;
		for (Waypoint waypoint : selected) {
			if (state.isSharedInContext(worldPath, setId, waypoint)) {
				hasSharedSelected = true;
				break;
			}
		}
		if (!hasSharedSelected) {
			sharedwaypoint$setButtonTooltip(deleteButton, null);
			sharedwaypoint$setButtonTooltip(shareButton, null);
			return;
		}

		boolean canManageSharedWaypoints = SharedWaypointPermission.canManageSharedWaypoints();
		if (deleteButton != null) {
			deleteButton.active = false;
			sharedwaypoint$setButtonTooltip(
				deleteButton,
				canManageSharedWaypoints
					? "To delete, press Add/Edit and delete from that interface"
					: "No permission to delete shared waypoint"
			);
		}
		if (shareButton != null) {
			shareButton.active = false;
			sharedwaypoint$setButtonTooltip(shareButton, "This is already a shared waypoint sync'ed to all players.");
		}
	}

	@Unique
	private void sharedwaypoint$setButtonTooltip(Button button, String text) {
		if (button == null) {
			return;
		}
		try {
			Class<?> tooltipClass = Class.forName("net.minecraft.client.gui.components.Tooltip");
			Method createMethod = tooltipClass.getMethod("create", Component.class);
			Method setTooltipMethod = button.getClass().getMethod("setTooltip", tooltipClass);
			Object tooltip = text == null ? null : createMethod.invoke(null, Component.literal(text));
			setTooltipMethod.invoke(button, tooltip);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
