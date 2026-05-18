package me.jizhengh.client.shared;

import me.jizhengh.SharedWaypoint;
import me.jizhengh.network.DeleteSharedWaypointC2SPayload;
import me.jizhengh.network.ShareWaypointC2SPayload;
import me.jizhengh.shared.SharedWaypointEntry;
import me.jizhengh.shared.SharedWaypointId;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.path.XaeroPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SharedWaypointClientState {
	private static final SharedWaypointClientState INSTANCE = new SharedWaypointClientState();

	private final Map<SharedWaypointId, SharedWaypointEntry> sharedEntries = new LinkedHashMap<>();
	private final Set<Waypoint> sharedWaypointInstances = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Set<SharedWaypointId> removedIds = new java.util.HashSet<>();

	public static SharedWaypointClientState get() {
		return INSTANCE;
	}

	public synchronized void replaceFromServer(List<SharedWaypointEntry> entries) {
		removedIds.clear();
		removedIds.addAll(sharedEntries.keySet());
		sharedEntries.clear();
		for (SharedWaypointEntry entry : entries) {
			sharedEntries.put(entry.id(), entry);
			removedIds.remove(entry.id());
		}
		applyToXaero();
	}

	public synchronized boolean isSharedInstance(Waypoint waypoint) {
		return sharedWaypointInstances.contains(waypoint);
	}

	public synchronized boolean isSharedByValue(Waypoint waypoint) {
		for (SharedWaypointId id : sharedEntries.keySet()) {
			if (id.x() == waypoint.getX()
				&& id.y() == waypoint.getY()
				&& id.z() == waypoint.getZ()) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean isSharedInContext(String worldPath, String waypointSet, Waypoint waypoint) {
		return sharedEntries.containsKey(toId(worldPath, waypointSet, waypoint));
	}

	public synchronized boolean isSharedWorldMapWaypoint(xaero.map.mods.gui.Waypoint waypoint) {
		Object original = waypoint.getOriginal();
		if (original instanceof Waypoint minimapWaypoint) {
			return isSharedByValue(minimapWaypoint);
		}
		for (SharedWaypointId id : sharedEntries.keySet()) {
			if (id.x() == waypoint.getX()
				&& id.y() == waypoint.getY()
				&& id.z() == waypoint.getZ()) {
				return true;
			}
		}
		return false;
	}

	public synchronized SharedWaypointEntry toEntry(String worldPath, String waypointSet, Waypoint waypoint) {
		return new SharedWaypointEntry(
			toId(worldPath, waypointSet, waypoint),
			waypoint.isYIncluded(),
			waypoint.isRotation(),
			waypoint.getYaw(),
			waypoint.getColor()
		);
	}

	public void sendShare(SharedWaypointEntry entry) {
		ClientPlayNetworking.send(new ShareWaypointC2SPayload(entry));
	}

	public void sendDelete(SharedWaypointId id) {
		ClientPlayNetworking.send(new DeleteSharedWaypointC2SPayload(id));
	}

	public void onSessionAvailable() {
		applyToXaero();
	}

	private void applyToXaero() {
		Minecraft.getInstance().execute(() -> {
			MinimapSession session = SharedWaypointSessionHolder.get();
			if (session == null) {
				return;
			}

			synchronized (this) {
				sharedWaypointInstances.clear();
				for (SharedWaypointId removedId : removedIds) {
					MinimapWorld world = session.getWorldManager().getWorld(parsePath(removedId.worldPath()));
					if (world == null) {
						continue;
					}
					WaypointSet set = world.getWaypointSet(removedId.waypointSet());
					if (set == null) {
						continue;
					}
					Waypoint waypoint = findWaypoint(set, removedId);
					if (waypoint != null) {
						set.remove(waypoint);
					}
				}

				for (SharedWaypointEntry entry : sharedEntries.values()) {
					MinimapWorld world = session.getWorldManager().getWorld(parsePath(entry.id().worldPath()));
					if (world == null) {
						continue;
					}

					WaypointSet set = world.getWaypointSet(entry.id().waypointSet());
					if (set == null) {
						world.addWaypointSet(entry.id().waypointSet());
						set = world.getWaypointSet(entry.id().waypointSet());
					}

					Waypoint waypoint = findWaypoint(set, entry.id());
					if (waypoint == null) {
						waypoint = new Waypoint(
							entry.id().x(),
							entry.id().y(),
							entry.id().z(),
							entry.id().name(),
							entry.id().initials(),
							entry.color()
						);
						set.add(waypoint);
					}

					waypoint.setX(entry.id().x());
					waypoint.setY(entry.id().y());
					waypoint.setZ(entry.id().z());
					waypoint.setName(entry.id().name());
					waypoint.setInitials(entry.id().initials());
					waypoint.setYIncluded(entry.yIncluded());
					waypoint.setRotation(entry.rotationIncluded());
					if (entry.rotationIncluded()) {
						waypoint.setYaw(entry.yaw());
					}
					waypoint.setColor(entry.color());
					sharedWaypointInstances.add(waypoint);

					try {
						session.getWorldManagerIO().saveWorld(world);
					} catch (IOException e) {
						SharedWaypoint.LOGGER.warn("Failed to save world after shared waypoint sync", e);
					}
				}
				removedIds.clear();
			}
		});
	}

	private XaeroPath parsePath(String pathString) {
		String[] nodes = pathString.split("/");
		if (nodes.length == 0) {
			return XaeroPath.root(pathString);
		}
		XaeroPath path = XaeroPath.root(nodes[0]);
		for (int i = 1; i < nodes.length; i++) {
			if (!nodes[i].isEmpty()) {
				path = path.resolve(nodes[i]);
			}
		}
		return path;
	}

	private Waypoint findWaypoint(WaypointSet set, SharedWaypointId id) {
		List<Waypoint> buffered = new ArrayList<>();
		set.addTo(buffered);
		for (Waypoint waypoint : buffered) {
			if (waypoint.getX() == id.x()
				&& waypoint.getY() == id.y()
				&& waypoint.getZ() == id.z()
				&& waypoint.getName().equals(id.name())
				&& waypoint.getInitials().equals(id.initials())) {
				return waypoint;
			}
		}
		return null;
	}

	private SharedWaypointId toId(String worldPath, String setId, Waypoint waypoint) {
		return new SharedWaypointId(
			worldPath,
			setId,
			waypoint.getX(),
			waypoint.getY(),
			waypoint.getZ(),
			waypoint.getName(),
			waypoint.getInitials()
		);
	}

	public static String worldPath(MinimapWorld world) {
		return world.getFullPath().toString();
	}
}
