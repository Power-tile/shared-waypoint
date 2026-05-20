package me.jizhengh.client.mixin;

import me.jizhengh.client.shared.SharedWaypointClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Mixin(value = WaypointReader.class, remap = false)
public class WorldMapWaypointDeleteOptionMixin {
	@Inject(method = "getRightClickOptions", at = @At("RETURN"), remap = false, require = 0)
	private void sharedwaypoint$removeDeleteOption(
		Waypoint element,
		IRightClickableElement rightClickableElement,
		CallbackInfoReturnable<ArrayList<RightClickOption>> cir
	) {
		if (!SharedWaypointClientState.get().isSharedWorldMapWaypoint(element)) {
			return;
		}
		ArrayList<RightClickOption> options = cir.getReturnValue();
		if (options == null || options.isEmpty()) {
			return;
		}
		options.removeIf(this::sharedwaypoint$isDeleteOrShareOption);
	}

	private boolean sharedwaypoint$isDeleteOrShareOption(RightClickOption option) {
		if (option == null) {
			return false;
		}
		String className = option.getClass().getName();
		if (className.endsWith("WaypointReader$7") || className.endsWith("WaypointReader$5")) {
			return true;
		}
		try {
			Field nameField = RightClickOption.class.getDeclaredField("name");
			nameField.setAccessible(true);
			Object rawName = nameField.get(option);
			if (rawName instanceof String name) {
				return name.contains("waypoint_delete") || name.contains("waypoint_share");
			}
		} catch (ReflectiveOperationException ignored) {
		}
		return false;
	}
}
