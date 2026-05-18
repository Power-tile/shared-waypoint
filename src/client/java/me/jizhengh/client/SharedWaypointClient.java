package me.jizhengh.client;

import me.jizhengh.client.shared.SharedWaypointClientState;
import me.jizhengh.network.SharedWaypointSyncS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SharedWaypointClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SharedWaypointSyncS2CPayload.TYPE, (payload, context) -> {
			SharedWaypointClientState.get().replaceFromServer(payload.entries());
		});
	}
}