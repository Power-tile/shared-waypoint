package me.jizhengh.client.shared;

import me.jizhengh.config.SharedWaypointConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public final class SharedWaypointPermission {
	private SharedWaypointPermission() {
	}

	public static boolean canManageSharedWaypoints() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return false;
		}
		int requiredLevel = Math.max(0, SharedWaypointConfig.getPermissionLevel());
		if (requiredLevel == 0) {
			return true;
		}
		Permission requiredPermission = sharedwaypoint$permissionForLevel(requiredLevel);
		PermissionSet permissionSet = minecraft.player.permissions();
		return requiredPermission != null && permissionSet.hasPermission(requiredPermission);
	}

	private static Permission sharedwaypoint$permissionForLevel(int level) {
		return switch (Math.min(level, 4)) {
			case 1 -> Permissions.COMMANDS_MODERATOR;
			case 2 -> Permissions.COMMANDS_GAMEMASTER;
			case 3 -> Permissions.COMMANDS_ADMIN;
			default -> Permissions.COMMANDS_OWNER;
		};
	}
}
