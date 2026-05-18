package me.jizhengh.client.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jizhengh.client.render.SharedWaypointHaloState;
import me.jizhengh.client.shared.SharedWaypointClientState;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;
import xaero.lib.client.graphics.XaeroBufferProvider;

@Mixin(value = WaypointMapRenderer.class, remap = false)
public abstract class WaypointMapOutlineMixin {
	@Unique
	private static final ThreadLocal<Boolean> sharedwaypoint$outlineReentry = ThreadLocal.withInitial(() -> false);

	@Shadow
	public abstract void drawIcon(
		MinimapElementGraphics graphics,
		MinimapRendererHelper helper,
		Waypoint waypoint,
		int x,
		int y,
		int opacityPercent,
		XaeroBufferProvider minimapBufferSource,
		VertexConsumer waypointBackgroundConsumer,
		VertexConsumer texturedIconConsumer
	);

	@Shadow
	public abstract void drawIconGUI(GuiGraphics graphics, Waypoint waypoint, int x, int y, int opacityPercent);

	@Inject(method = "drawIcon", at = @At("HEAD"), remap = false, require = 0)
	private void sharedwaypoint$outlineMapIcon(
		MinimapElementGraphics graphics,
		MinimapRendererHelper helper,
		Waypoint waypoint,
		int x,
		int y,
		int opacityPercent,
		XaeroBufferProvider minimapBufferSource,
		VertexConsumer waypointBackgroundConsumer,
		VertexConsumer texturedIconConsumer,
		CallbackInfo ci
	) {
		if (sharedwaypoint$outlineReentry.get() || !SharedWaypointClientState.get().isSharedByValue(waypoint)) {
			return;
		}
		WaypointColor oldColor = waypoint.getWaypointColor();
		WaypointColor haloColor = oldColor == WaypointColor.WHITE ? WaypointColor.GRAY : WaypointColor.WHITE;
		sharedwaypoint$outlineReentry.set(true);
		try {
			SharedWaypointHaloState.setHaloPass(true);
			waypoint.setWaypointColor(haloColor);
			opacityPercent = 80;
			drawIcon(graphics, helper, waypoint, x - 1, y - 1, opacityPercent, minimapBufferSource, waypointBackgroundConsumer, texturedIconConsumer);
		} finally {
			SharedWaypointHaloState.setHaloPass(false);
			sharedwaypoint$outlineReentry.set(false);
			waypoint.setWaypointColor(oldColor);
		}
	}

	@Inject(method = "drawIconGUI", at = @At("HEAD"), remap = false, require = 0)
	private void sharedwaypoint$outlineGuiIcon(GuiGraphics graphics, Waypoint waypoint, int x, int y, int opacityPercent, CallbackInfo ci) {
		if (sharedwaypoint$outlineReentry.get() || !SharedWaypointClientState.get().isSharedByValue(waypoint)) {
			return;
		}
		WaypointColor oldColor = waypoint.getWaypointColor();
		WaypointColor haloColor = oldColor == WaypointColor.WHITE ? WaypointColor.GRAY : WaypointColor.WHITE;
		sharedwaypoint$outlineReentry.set(true);
		try {
			SharedWaypointHaloState.setHaloPass(true);
			waypoint.setWaypointColor(haloColor);
			opacityPercent = 80;
			drawIconGUI(graphics, waypoint, x - 1, y - 1, opacityPercent);
		} finally {
			SharedWaypointHaloState.setHaloPass(false);
			sharedwaypoint$outlineReentry.set(false);
			waypoint.setWaypointColor(oldColor);
		}
	}
}
