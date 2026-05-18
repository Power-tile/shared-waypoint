package me.jizhengh.client.render;

public final class SharedWaypointHaloState {
	private static final ThreadLocal<Boolean> HALO_PASS = ThreadLocal.withInitial(() -> false);

	private SharedWaypointHaloState() {
	}

	public static boolean isHaloPass() {
		return HALO_PASS.get();
	}

	public static void setHaloPass(boolean value) {
		HALO_PASS.set(value);
	}
}
