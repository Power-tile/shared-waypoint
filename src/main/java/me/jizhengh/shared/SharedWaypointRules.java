package me.jizhengh.shared;

public final class SharedWaypointRules {
	public static final int VISIBILITY_LOCAL = 0;
	public static final int VISIBILITY_GLOBAL = 1;

	private SharedWaypointRules() {
	}

	public static int clampVisibilityType(int visibilityType) {
		return visibilityType == VISIBILITY_GLOBAL ? VISIBILITY_GLOBAL : VISIBILITY_LOCAL;
	}
}
