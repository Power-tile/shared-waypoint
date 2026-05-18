package me.jizhengh.network;

import me.jizhengh.server.SharedWaypointServerStore;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class SharedWaypointNetworking {
	private SharedWaypointNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(ShareWaypointC2SPayload.TYPE, ShareWaypointC2SPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(DeleteSharedWaypointC2SPayload.TYPE, DeleteSharedWaypointC2SPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SharedWaypointSyncS2CPayload.TYPE, SharedWaypointSyncS2CPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ShareWaypointC2SPayload.TYPE, (payload, context) -> {
			var server = context.player().level().getServer();
			SharedWaypointServerStore.upsert(server, payload.entry());
			broadcastSync(server.getPlayerList().getPlayers().toArray(new ServerPlayer[0]));
		});

		ServerPlayNetworking.registerGlobalReceiver(DeleteSharedWaypointC2SPayload.TYPE, (payload, context) -> {
			var server = context.player().level().getServer();
			SharedWaypointServerStore.remove(server, payload.id());
			broadcastSync(server.getPlayerList().getPlayers().toArray(new ServerPlayer[0]));
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.player, new SharedWaypointSyncS2CPayload(SharedWaypointServerStore.getAll(server)));
		});
	}

	public static void broadcastSync(ServerPlayer... players) {
		if (players.length == 0) {
			return;
		}
		SharedWaypointSyncS2CPayload payload = new SharedWaypointSyncS2CPayload(
			SharedWaypointServerStore.getAll(players[0].level().getServer())
		);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
