package me.jizhengh.network;

import me.jizhengh.shared.SharedWaypointEntry;
import me.jizhengh.shared.SharedWaypointId;
import me.jizhengh.shared.SharedWaypointRules;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

final class SharedWaypointPayloadCodecs {
	private SharedWaypointPayloadCodecs() {
	}

	static void writeId(RegistryFriendlyByteBuf buf, SharedWaypointId id) {
		buf.writeUtf(id.worldPath());
		buf.writeUtf(id.waypointSet());
		buf.writeVarInt(id.x());
		buf.writeVarInt(id.y());
		buf.writeVarInt(id.z());
		buf.writeUtf(id.name());
		buf.writeUtf(id.initials());
	}

	static SharedWaypointId readId(RegistryFriendlyByteBuf buf) {
		return new SharedWaypointId(
			buf.readUtf(),
			buf.readUtf(),
			buf.readVarInt(),
			buf.readVarInt(),
			buf.readVarInt(),
			buf.readUtf(),
			buf.readUtf()
		);
	}

	static void writeEntry(RegistryFriendlyByteBuf buf, SharedWaypointEntry entry) {
		writeId(buf, entry.id());
		buf.writeBoolean(entry.yIncluded());
		buf.writeBoolean(entry.rotationIncluded());
		buf.writeVarInt(entry.yaw());
		buf.writeVarInt(entry.color());
		buf.writeVarInt(entry.visibilityType());
	}

	static SharedWaypointEntry readEntry(RegistryFriendlyByteBuf buf) {
		return new SharedWaypointEntry(
			readId(buf),
			buf.readBoolean(),
			buf.readBoolean(),
			buf.readVarInt(),
			buf.readVarInt(),
			SharedWaypointRules.clampVisibilityType(buf.readVarInt())
		);
	}

	static void writeEntries(RegistryFriendlyByteBuf buf, List<SharedWaypointEntry> entries) {
		buf.writeVarInt(entries.size());
		for (SharedWaypointEntry entry : entries) {
			writeEntry(buf, entry);
		}
	}

	static List<SharedWaypointEntry> readEntries(RegistryFriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<SharedWaypointEntry> entries = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			entries.add(readEntry(buf));
		}
		return entries;
	}
}
