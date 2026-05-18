package me.jizhengh;

import me.jizhengh.network.SharedWaypointNetworking;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedWaypoint implements ModInitializer {
	public static final String MOD_ID = "shared_waypoint";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SharedWaypointNetworking.init();
		LOGGER.info("Shared Waypoint initialized.");
	}
}