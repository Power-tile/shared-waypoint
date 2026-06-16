package me.jizhengh.client.shared;

import me.jizhengh.shared.SharedWaypointRules;
import xaero.common.gui.WaypointEditForm;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;

public final class SharedWaypointClientRules {
	private SharedWaypointClientRules() {
	}

	public static WaypointVisibilityType clampVisibility(WaypointVisibilityType visibilityType) {
		return visibilityType == WaypointVisibilityType.GLOBAL
			? WaypointVisibilityType.GLOBAL
			: WaypointVisibilityType.LOCAL;
	}

	public static WaypointVisibilityType cycleVisibility(WaypointVisibilityType current) {
		return current == WaypointVisibilityType.GLOBAL
			? WaypointVisibilityType.LOCAL
			: WaypointVisibilityType.GLOBAL;
	}

	public static void enforceOnWaypoint(Waypoint waypoint) {
		if (waypoint == null) {
			return;
		}
		enforceOnWaypoint(waypoint, waypoint.getVisibilityType());
	}

	public static void enforceOnWaypoint(Waypoint waypoint, int visibilityType) {
		if (waypoint == null) {
			return;
		}
		waypoint.setDisabled(false);
		waypoint.setTemporary(false);
		waypoint.setPurpose(WaypointPurpose.NORMAL);
		waypoint.setOneoffDestination(false);
		waypoint.setVisibilityType(SharedWaypointRules.clampVisibilityType(visibilityType));
	}

	public static void enforceOnForm(WaypointEditForm form) {
		if (form == null) {
			return;
		}
		form.disabledOrTemporary = 0;
		form.visibilityType = clampVisibility(form.visibilityType);
	}
}
