package me.jizhengh.shared;

import java.util.Objects;

public record SharedWaypointId(
	String worldPath,
	String waypointSet,
	int x,
	int y,
	int z,
	String name,
	String initials
) {
	public SharedWaypointId {
		Objects.requireNonNull(worldPath, "worldPath");
		Objects.requireNonNull(waypointSet, "waypointSet");
		name = name == null ? "" : name;
		initials = initials == null ? "" : initials;
	}
}
