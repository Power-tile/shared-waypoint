# Shared Waypoint

Fabric mod that integrates with Xaero's Minimap + World Map to add server-synced shared waypoints.

## Summary

- Mark waypoints as shared from the waypoint Add/Edit screen.
- Shared waypoints are stored server-side and synced to all clients on join and on updates.
- Shared waypoints have special rendering (halo/outline) on minimap, world map, and in-world markers.
- Shared waypoints are protected from accidental deletion paths:
  - waypoint list `DELETE` is disabled for shared selections
  - world map right-click delete/share options are removed for shared waypoints
  - world map `DEL` delete path is blocked for shared waypoints

## Server-Side Installation and Config

### Install

1. Install Fabric server for the target Minecraft version.
2. Put this mod jar in the server `mods` folder.
3. Ensure clients also run this mod and required Xaero mods.
4. Start server once to generate config/data files.

### Config

Config file path:

- `config/shared-waypoint.yml`

Config keys:

- `permission_level` (default `2`)
  - `0`: everyone can create/delete shared waypoints
  - `1`: moderator-level commands
  - `2`: gamemaster-level commands
  - `3`: admin-level commands
  - `4`: owner-level commands

Notes:

- Shared waypoint data is persisted by the mod and broadcast by server networking.
- Changing `permission_level` affects who can manage shared waypoints client-side.

## Client-Side Operations

### Create Shared Waypoint

1. Open waypoint Add/Edit screen.
2. Toggle `Shared: ON`.
3. Confirm waypoint.

Behavior:

- When marking a new waypoint as shared, enable/disable/temporary is forced to **enabled** before lock.
- Non-shared waypoint fields behave normally.

### Edit Existing Shared Waypoint

- Shared waypoint fields are locked (name/coords/color/visibility and related fields).
- Depending on permission:
  - with permission: confirm acts as `Delete Shared`
  - without permission: confirm is disabled (`Shared (No Permission)`)

### Delete Shared Waypoint

- Waypoint list screen:
  - `DELETE` is disabled for shared selections
  - tooltip explains to delete via Add/Edit interface (or no-permission message)
- Add/Edit screen:
  - authorized users can delete shared waypoint from this interface

### Share/World Map UX

- Waypoint list `Share` button is disabled for shared selections with tooltip:
  - `This is already a shared waypoint sync'ed to all players.`
- On world map right-click menu, `Share waypoint` is removed for shared waypoints.
