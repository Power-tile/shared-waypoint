package me.jizhengh.client.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import me.jizhengh.client.shared.SharedWaypointClientState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.MapElementGraphics;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.graphics.MapRenderHelper;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderer;

@Mixin(value = WaypointRenderer.class, remap = false)
public class WorldMapWaypointRendererHaloMixin {
	@Unique
	private static final ThreadLocal<Boolean> sharedwaypoint$haloBlitReentry = ThreadLocal.withInitial(() -> false);
	@Unique
	private static final ThreadLocal<Boolean> sharedwaypoint$currentSharedWaypoint = ThreadLocal.withInitial(() -> false);

	@Inject(method = "renderElement", at = @At("HEAD"), remap = false, require = 0)
	private void sharedwaypoint$captureSharedWaypoint(
		Waypoint waypoint,
		boolean selected,
		double param3,
		float scale,
		double x,
		double y,
		ElementRenderInfo renderInfo,
		MapElementGraphics graphics,
		MultiBufferSource.BufferSource buffers,
		MultiTextureRenderTypeRendererProvider renderers,
		CallbackInfoReturnable<Boolean> cir
	) {
		sharedwaypoint$currentSharedWaypoint.set(SharedWaypointClientState.get().isSharedWorldMapWaypoint(waypoint));
	}

	@Inject(method = "renderElement", at = @At("RETURN"), remap = false, require = 0)
	private void sharedwaypoint$clearSharedWaypointFlag(
		Waypoint waypoint,
		boolean selected,
		double param3,
		float scale,
		double x,
		double y,
		ElementRenderInfo renderInfo,
		MapElementGraphics graphics,
		MultiBufferSource.BufferSource buffers,
		MultiTextureRenderTypeRendererProvider renderers,
		CallbackInfoReturnable<Boolean> cir
	) {
		sharedwaypoint$currentSharedWaypoint.set(false);
	}

	@Redirect(
		method = "renderElement",
		at = @At(
			value = "INVOKE",
			target = "Lxaero/map/graphics/MapRenderHelper;blitIntoMultiTextureRenderer(Lorg/joml/Matrix4f;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRenderer;FFIIIIFFFFLcom/mojang/blaze3d/textures/GpuTexture;)V",
			remap = false
		),
		remap = false,
		require = 0
	)
	private void sharedwaypoint$drawWorldMapHaloAndBase(
		Matrix4f matrix,
		MultiTextureRenderTypeRenderer renderer,
		float x,
		float y,
		int u,
		int v,
		int width,
		int height,
		float r,
		float g,
		float b,
		float a,
		GpuTexture texture
	) {
		if (sharedwaypoint$currentSharedWaypoint.get() && !sharedwaypoint$haloBlitReentry.get()) {
			sharedwaypoint$haloBlitReentry.set(true);
			try {
				float haloR = 1f;
				float haloG = 1f;
				float haloB = 1f;
				if (r >= 0.98f && g >= 0.98f && b >= 0.98f) {
					haloR = 0.7f;
					haloG = 0.7f;
					haloB = 0.7f;
				}
				MapRenderHelper.blitIntoMultiTextureRenderer(
					matrix,
					renderer,
					x - 4.0f,
					y - 4.0f,
					u,
					v,
					width,
					height,
					haloR,
					haloG,
					haloB,
					0.8f * a,
					texture
				);
			} finally {
				sharedwaypoint$haloBlitReentry.set(false);
			}
		}
		MapRenderHelper.blitIntoMultiTextureRenderer(matrix, renderer, x, y, u, v, width, height, r, g, b, a, texture);
	}
}
