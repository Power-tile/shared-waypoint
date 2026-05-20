package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import me.jizhengh.client.shared.SharedWaypointPermission;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.gui.GuiAddWaypoint;
import xaero.common.gui.GuiWaypointSets;
import xaero.common.gui.GuiWaypointWorlds;
import xaero.common.gui.WaypointEditForm;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.gui.ScreenBase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

@Mixin(value = GuiAddWaypoint.class, remap = false)
public abstract class GuiAddWaypointMixin {
	@Shadow private MinimapSession session;
	@Shadow private MinimapWorldManager manager;
	@Shadow private GuiWaypointWorlds worlds;
	@Shadow private GuiWaypointSets sets;
	@Shadow private ArrayList<Waypoint> waypointsEdited;
	@Shadow private WaypointEditForm mutualForm;
	@Shadow private ArrayList<WaypointEditForm> editForms;
	@Shadow private int selectedWaypointIndex;
	@Shadow private boolean adding;
	@Shadow protected Button confirmButton;
	@Shadow private Button disableButton;
	@Shadow private Button visibilityTypeButton;
	@Shadow private EditBox nameTextField;
	@Shadow private EditBox xTextField;
	@Shadow private EditBox yTextField;
	@Shadow private EditBox zTextField;
	@Shadow private EditBox yawTextField;
	@Shadow private EditBox initialTextField;

	@Unique private Button sharedwaypoint$shareButton;
	@Unique private boolean sharedwaypoint$markShared;
	@Unique private boolean sharedwaypoint$lockedShared;

	@Inject(method = {"init", "method_25426"}, at = @At("TAIL"), remap = false)
	private void sharedwaypoint$initSharedToggle(CallbackInfo ci) {
		boolean canManageSharedWaypoints = SharedWaypointPermission.canManageSharedWaypoints();
		sharedwaypoint$lockedShared = sharedwaypoint$isCurrentShared();
		sharedwaypoint$markShared = sharedwaypoint$lockedShared;
		if (!canManageSharedWaypoints && !sharedwaypoint$lockedShared) {
			sharedwaypoint$markShared = false;
		}

		sharedwaypoint$shareButton = Button.builder(
			sharedwaypoint$shareText(),
			button -> {
				if (!sharedwaypoint$lockedShared) {
					sharedwaypoint$markShared = !sharedwaypoint$markShared;
					button.setMessage(sharedwaypoint$shareText());
					sharedwaypoint$syncSharedCreationState();
				}
			}
		).bounds(
			confirmButton != null ? confirmButton.getX() + 102 : 0,
			confirmButton != null ? confirmButton.getY() + 24 : 0,
			99,
			20
		).build();
		sharedwaypoint$shareButton.active = !sharedwaypoint$lockedShared && canManageSharedWaypoints;
		sharedwaypoint$setButtonTooltip(
			sharedwaypoint$shareButton,
			!canManageSharedWaypoints && !sharedwaypoint$lockedShared ? "No permission to create shared waypoint" : null
		);
		sharedwaypoint$addWidgetCompat(sharedwaypoint$shareButton);
		sharedwaypoint$syncSharedCreationState();

		if (sharedwaypoint$lockedShared && confirmButton != null) {
			confirmButton.setMessage(Component.literal(canManageSharedWaypoints ? "Delete Shared" : "Shared (No Permission)"));
			confirmButton.active = canManageSharedWaypoints;
			sharedwaypoint$disableEditingControls();
		}
	}

	@Inject(method = "lambda$init$3", at = @At("HEAD"), cancellable = true, remap = false)
	private void sharedwaypoint$deleteInsteadOfEdit(ClientConfigManager configManager, Button button, CallbackInfo ci) {
		if (!sharedwaypoint$lockedShared) {
			return;
		}
		if (!SharedWaypointPermission.canManageSharedWaypoints()) {
			ci.cancel();
			return;
		}

		Waypoint waypoint = sharedwaypoint$currentWaypoint();
		MinimapWorld world = sharedwaypoint$currentWorld();
		if (waypoint == null || world == null) {
			return;
		}
		String setId = sets.getCurrentSetKey();
		WaypointSet set = world.getWaypointSet(setId);
		if (set != null) {
			set.remove(waypoint);
		}

		SharedWaypointClientState state = SharedWaypointClientState.get();
		String worldPath = SharedWaypointClientState.worldPath(world);
		state.sendDelete(state.toEntry(worldPath, setId, waypoint).id());

		try {
			session.getWorldManagerIO().saveWorld(world);
		} catch (IOException ignored) {
		}
		((ScreenBase) (Object) this).goBack();
		ci.cancel();
	}

	@Inject(method = "lambda$init$3", at = @At("RETURN"), remap = false)
	private void sharedwaypoint$shareAfterConfirm(ClientConfigManager configManager, Button button, CallbackInfo ci) {
		if (sharedwaypoint$lockedShared || !sharedwaypoint$markShared || !SharedWaypointPermission.canManageSharedWaypoints()) {
			return;
		}
		sharedwaypoint$forceEnabledSharedState();
		Waypoint waypoint = sharedwaypoint$currentWaypoint();
		MinimapWorld world = sharedwaypoint$currentWorld();
		if (waypoint == null || world == null) {
			return;
		}
		String setId = sets.getCurrentSetKey();
		String worldPath = SharedWaypointClientState.worldPath(world);
		SharedWaypointClientState.get().sendShare(
			SharedWaypointClientState.get().toEntry(worldPath, setId, waypoint)
		);
	}

	@Inject(method = {"updateConfirmButton", "method_56131"}, at = @At("TAIL"), remap = false, require = 0)
	private void sharedwaypoint$keepSharedCreationEnabledLocked(CallbackInfo ci) {
		sharedwaypoint$syncSharedCreationState();
	}

	@Unique
	private Component sharedwaypoint$shareText() {
		return Component.literal(sharedwaypoint$markShared ? "Shared: ON" : "Shared: OFF");
	}

	@Unique
	private Waypoint sharedwaypoint$currentWaypoint() {
		if (waypointsEdited == null || waypointsEdited.isEmpty()) {
			return null;
		}
		int i = Math.max(0, Math.min(selectedWaypointIndex, waypointsEdited.size() - 1));
		return waypointsEdited.get(i);
	}

	@Unique
	private MinimapWorld sharedwaypoint$currentWorld() {
		if (worlds == null || manager == null) {
			return null;
		}
		XaeroPath path = (XaeroPath) worlds.getCurrentKey();
		return manager.getWorld(path);
	}

	@Unique
	private boolean sharedwaypoint$isCurrentShared() {
		if (adding) {
			return false;
		}
		Waypoint waypoint = sharedwaypoint$currentWaypoint();
		MinimapWorld world = sharedwaypoint$currentWorld();
		if (waypoint == null || world == null || sets == null) {
			return false;
		}
		String setId = sets.getCurrentSetKey();
		String worldPath = SharedWaypointClientState.worldPath(world);
		return SharedWaypointClientState.get().isSharedInContext(worldPath, setId, waypoint);
	}

	@Unique
	private void sharedwaypoint$disableEditingControls() {
		if (nameTextField != null) nameTextField.setEditable(false);
		if (xTextField != null) xTextField.setEditable(false);
		if (yTextField != null) yTextField.setEditable(false);
		if (zTextField != null) zTextField.setEditable(false);
		if (yawTextField != null) yawTextField.setEditable(false);
		if (initialTextField != null) initialTextField.setEditable(false);
		if (disableButton != null) disableButton.active = false;
		if (visibilityTypeButton != null) visibilityTypeButton.active = false;
		sharedwaypoint$disableColorSelectorReflectively();
	}

	@Unique
	private void sharedwaypoint$syncSharedCreationState() {
		boolean canManageSharedWaypoints = SharedWaypointPermission.canManageSharedWaypoints();
		if (sharedwaypoint$lockedShared) {
			if (confirmButton != null) {
				confirmButton.setMessage(Component.literal(canManageSharedWaypoints ? "Delete Shared" : "Shared (No Permission)"));
				confirmButton.active = canManageSharedWaypoints;
			}
			if (disableButton != null) {
				disableButton.active = false;
			}
			return;
		}
		boolean lockEnabledState = sharedwaypoint$markShared;
		if (lockEnabledState) {
			sharedwaypoint$forceEnabledSharedState();
		}
		if (disableButton != null) {
			disableButton.active = !lockEnabledState;
		}
	}

	@Unique
	private void sharedwaypoint$forceEnabledSharedState() {
		Waypoint waypoint = sharedwaypoint$currentWaypoint();
		if (waypoint != null) {
			waypoint.setDisabled(false);
			waypoint.setTemporary(false);
		}
		if (mutualForm != null) {
			mutualForm.disabledOrTemporary = 0;
		}
		if (editForms != null) {
			for (WaypointEditForm form : editForms) {
				if (form != null) {
					form.disabledOrTemporary = 0;
				}
			}
		}
	}

	@Unique
	private void sharedwaypoint$disableColorSelectorReflectively() {
		try {
			Field colorField = this.getClass().getDeclaredField("colorDD");
			colorField.setAccessible(true);
			Object colorDropdown = colorField.get(this);
			if (colorDropdown == null) {
				return;
			}

			// Prefer calling a setter if present; fall back to toggling widget "active" flags.
			for (Method method : colorDropdown.getClass().getMethods()) {
				if (method.getName().equals("setActive") && method.getParameterCount() == 1
					&& (method.getParameterTypes()[0] == boolean.class || method.getParameterTypes()[0] == Boolean.class)) {
					method.invoke(colorDropdown, false);
					return;
				}
			}

			for (String fieldName : new String[]{"active", "field_22763"}) {
				try {
					Field activeField = colorDropdown.getClass().getDeclaredField(fieldName);
					activeField.setAccessible(true);
					activeField.setBoolean(colorDropdown, false);
					return;
				} catch (NoSuchFieldException ignored) {
				}
			}
		} catch (ReflectiveOperationException ignored) {
		}
	}

	@Unique
	private void sharedwaypoint$addWidgetCompat(Button button) {
		// Xaero classes are not remapped consistently, so we try both mapped and intermediary method names.
		for (String methodName : new String[]{"addRenderableWidget", "method_37063"}) {
			try {
				for (Method method : this.getClass().getMethods()) {
					if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
						method.invoke(this, button);
						return;
					}
				}
			} catch (ReflectiveOperationException ignored) {
			}
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
