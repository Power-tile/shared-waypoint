package me.jizhengh.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import me.jizhengh.shared.SharedWaypointEntry;
import me.jizhengh.shared.SharedWaypointId;
import me.jizhengh.shared.SharedWaypointRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SharedWaypointServerStore {
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(SharedWaypointEntry.class, new SharedWaypointEntryDeserializer())
		.create();
	private static final Type LIST_TYPE = new TypeToken<List<SharedWaypointEntry>>() {}.getType();

	private SharedWaypointServerStore() {
	}

	private static Path storePath(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("shared-waypoints.json");
	}

	public static List<SharedWaypointEntry> getAll(MinecraftServer server) {
		Path path = storePath(server);
		if (!Files.exists(path)) {
			return List.of();
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			List<SharedWaypointEntry> list = GSON.fromJson(reader, LIST_TYPE);
			return list == null ? List.of() : List.copyOf(list);
		} catch (IOException e) {
			return List.of();
		}
	}

	private static synchronized void saveAll(MinecraftServer server, List<SharedWaypointEntry> entries) {
		Path path = storePath(server);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(entries, LIST_TYPE, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static void upsert(MinecraftServer server, SharedWaypointEntry entry) {
		List<SharedWaypointEntry> current = new java.util.ArrayList<>(getAll(server));
		current.removeIf(existing -> existing.id().equals(entry.id()));
		current.add(normalizeEntry(entry));
		saveAll(server, current);
	}

	public static void remove(MinecraftServer server, SharedWaypointId id) {
		List<SharedWaypointEntry> current = new java.util.ArrayList<>(getAll(server));
		current.removeIf(existing -> existing.id().equals(id));
		saveAll(server, current);
	}

	private static SharedWaypointEntry normalizeEntry(SharedWaypointEntry entry) {
		int visibilityType = SharedWaypointRules.clampVisibilityType(entry.visibilityType());
		if (visibilityType == entry.visibilityType()) {
			return entry;
		}
		return new SharedWaypointEntry(
			entry.id(),
			entry.yIncluded(),
			entry.rotationIncluded(),
			entry.yaw(),
			entry.color(),
			visibilityType
		);
	}

	private static final class SharedWaypointEntryDeserializer implements JsonDeserializer<SharedWaypointEntry> {
		@Override
		public SharedWaypointEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			JsonObject object = json.getAsJsonObject();
			return new SharedWaypointEntry(
				context.deserialize(object.get("id"), SharedWaypointId.class),
				object.get("yIncluded").getAsBoolean(),
				object.get("rotationIncluded").getAsBoolean(),
				object.get("yaw").getAsInt(),
				object.get("color").getAsInt(),
				SharedWaypointRules.clampVisibilityType(
					object.has("visibilityType") ? object.get("visibilityType").getAsInt() : 0
				)
			);
		}
	}
}
