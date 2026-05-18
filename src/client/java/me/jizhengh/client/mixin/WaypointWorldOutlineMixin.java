package me.jizhengh.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jizhengh.client.render.SharedWaypointHaloState;
import me.jizhengh.client.shared.SharedWaypointClientState;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer;
import xaero.lib.client.graphics.XaeroBufferProvider;

@Mixin(value = WaypointWorldRenderer.class, remap = false)
public abstract class WaypointWorldOutlineMixin {
	@Unique
	private static final ThreadLocal<Boolean> sharedwaypoint$outlineReentry = ThreadLocal.withInitial(() -> false);

	@Shadow
	private void renderIcon(Waypoint waypoint, boolean invert, PoseStack poseStack, Font font, XaeroBufferProvider minimapBufferSource) {
	}

	@Inject(method = "renderIcon", at = @At("HEAD"), remap = false, require = 0)
	private void sharedwaypoint$outlineWorldIcon(
		Waypoint waypoint,
		boolean invert,
		PoseStack poseStack,
		Font font,
		XaeroBufferProvider minimapBufferSource,
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
			sharedwaypoint$drawOffset(waypoint, invert, poseStack, font, minimapBufferSource, -1.0f, -1.0f);
		} finally {
			SharedWaypointHaloState.setHaloPass(false);
			sharedwaypoint$outlineReentry.set(false);
			waypoint.setWaypointColor(oldColor);
		}
	}

	@Unique
	private void sharedwaypoint$drawOffset(
		Waypoint waypoint,
		boolean invert,
		PoseStack poseStack,
		Font font,
		XaeroBufferProvider minimapBufferSource,
		float xOffset,
		float yOffset
	) {
		poseStack.pushPose();
		poseStack.translate(xOffset, yOffset, 0f);
		renderIcon(waypoint, invert, poseStack, font, minimapBufferSource);
		poseStack.popPose();
	}
}
