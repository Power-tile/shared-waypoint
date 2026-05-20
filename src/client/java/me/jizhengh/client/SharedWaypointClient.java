package me.jizhengh.client;

import me.jizhengh.SharedWaypoint;
import me.jizhengh.client.shared.SharedWaypointClientState;
import me.jizhengh.network.SharedWaypointSyncS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SharedWaypointClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SharedWaypoint.LOGGER.info("[shared-waypoint-debug] SharedWaypoint client entrypoint initialized.");
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
			SharedWaypoint.LOGGER.info("[shared-waypoint-debug] client joined server {} and is waiting for shared waypoint sync", handler.getConnection().getRemoteAddress())
		);
		ClientPlayNetworking.registerGlobalReceiver(SharedWaypointSyncS2CPayload.TYPE, (payload, context) -> {
			SharedWaypoint.LOGGER.info("[shared-waypoint-debug] client received sync payload with {} shared entries", payload.entries().size());
			SharedWaypointClientState.get().replaceFromServer(payload.entries());
		});
	}
}