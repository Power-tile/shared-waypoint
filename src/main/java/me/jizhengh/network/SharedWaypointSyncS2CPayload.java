package me.jizhengh.network;

import me.jizhengh.SharedWaypoint;
import me.jizhengh.shared.SharedWaypointEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SharedWaypointSyncS2CPayload(List<SharedWaypointEntry> entries) implements CustomPacketPayload {
	public static final Type<SharedWaypointSyncS2CPayload> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath(SharedWaypoint.MOD_ID, "sync_waypoints_s2c"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SharedWaypointSyncS2CPayload> STREAM_CODEC =
		CustomPacketPayload.codec(SharedWaypointSyncS2CPayload::write, SharedWaypointSyncS2CPayload::new);

	private SharedWaypointSyncS2CPayload(RegistryFriendlyByteBuf buf) {
		this(SharedWaypointPayloadCodecs.readEntries(buf));
	}

	private void write(RegistryFriendlyByteBuf buf) {
		SharedWaypointPayloadCodecs.writeEntries(buf, entries);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
