package me.jizhengh.network;

import me.jizhengh.SharedWaypoint;
import me.jizhengh.shared.SharedWaypointEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShareWaypointC2SPayload(SharedWaypointEntry entry) implements CustomPacketPayload {
	public static final Type<ShareWaypointC2SPayload> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath(SharedWaypoint.MOD_ID, "share_waypoint_c2s"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareWaypointC2SPayload> STREAM_CODEC =
		CustomPacketPayload.codec(ShareWaypointC2SPayload::write, ShareWaypointC2SPayload::new);

	private ShareWaypointC2SPayload(RegistryFriendlyByteBuf buf) {
		this(SharedWaypointPayloadCodecs.readEntry(buf));
	}

	private void write(RegistryFriendlyByteBuf buf) {
		SharedWaypointPayloadCodecs.writeEntry(buf, entry);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
