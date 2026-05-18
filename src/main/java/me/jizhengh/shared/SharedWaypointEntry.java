package me.jizhengh.shared;

import java.util.Objects;

public record SharedWaypointEntry(
	SharedWaypointId id,
	boolean yIncluded,
	boolean rotationIncluded,
	int yaw,
	int color
) {
	public SharedWaypointEntry {
		Objects.requireNonNull(id, "id");
	}
}
