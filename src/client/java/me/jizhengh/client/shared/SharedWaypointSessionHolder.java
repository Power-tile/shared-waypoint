package me.jizhengh.client.shared;

import xaero.hud.minimap.module.MinimapSession;

public final class SharedWaypointSessionHolder {
	private static volatile MinimapSession minimapSession;

	private SharedWaypointSessionHolder() {
	}

	public static void set(MinimapSession session) {
		minimapSession = session;
		if (session != null) {
			SharedWaypointClientState.get().onSessionAvailable();
		}
	}

	public static MinimapSession get() {
		return minimapSession;
	}
}
