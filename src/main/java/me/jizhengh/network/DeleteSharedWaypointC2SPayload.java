package me.jizhengh.network;

import me.jizhengh.SharedWaypoint;
import me.jizhengh.shared.SharedWaypointId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DeleteSharedWaypointC2SPayload(SharedWaypointId id) implements CustomPacketPayload {
	public static final Type<DeleteSharedWaypointC2SPayload> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath(SharedWaypoint.MOD_ID, "delete_waypoint_c2s"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DeleteSharedWaypointC2SPayload> STREAM_CODEC =
		CustomPacketPayload.codec(DeleteSharedWaypointC2SPayload::write, DeleteSharedWaypointC2SPayload::new);

	private DeleteSharedWaypointC2SPayload(RegistryFriendlyByteBuf buf) {
		this(SharedWaypointPayloadCodecs.readId(buf));
	}

	private void write(RegistryFriendlyByteBuf buf) {
		SharedWaypointPayloadCodecs.writeId(buf, id);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
