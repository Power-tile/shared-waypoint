package me.jizhengh.config;

import me.jizhengh.SharedWaypoint;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SharedWaypointConfig {
	private static final int DEFAULT_PERMISSION_LEVEL = 2;
	private static volatile int permissionLevel = DEFAULT_PERMISSION_LEVEL;

	private SharedWaypointConfig() {
	}

	public static synchronized void init() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("shared-waypoint.yml");
		try {
			if (!Files.exists(configPath)) {
				Files.createDirectories(configPath.getParent());
				Files.writeString(configPath, defaultConfigText(), StandardCharsets.UTF_8);
			}
			load(configPath);
			SharedWaypoint.LOGGER.info("Loaded shared-waypoint config: permission_level={}", permissionLevel);
		} catch (IOException e) {
			permissionLevel = DEFAULT_PERMISSION_LEVEL;
			SharedWaypoint.LOGGER.warn("Failed to load shared-waypoint config, using default permission_level={}", DEFAULT_PERMISSION_LEVEL, e);
		}
	}

	public static int getPermissionLevel() {
		return permissionLevel;
	}

	private static void load(Path configPath) throws IOException {
		List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
		int loaded = DEFAULT_PERMISSION_LEVEL;
		for (String rawLine : lines) {
			String line = rawLine.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			if (line.startsWith("permission_level:")) {
				String value = line.substring("permission_level:".length()).trim();
				try {
					loaded = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					SharedWaypoint.LOGGER.warn("Invalid permission_level value '{}' in {}", value, configPath);
				}
			}
		}
		permissionLevel = Math.max(0, loaded);
	}

	private static String defaultConfigText() {
		return """
			# Required permission level to create/delete shared waypoints.
			# 0 = everyone, 2 = commands/moderator-like, 4 = server operator.
			permission_level: 2
			""";
	}
}
