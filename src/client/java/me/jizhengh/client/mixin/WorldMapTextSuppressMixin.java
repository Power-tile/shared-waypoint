package me.jizhengh.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jizhengh.client.render.SharedWaypointHaloState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.misc.Misc;

@Mixin(value = Misc.class, remap = false)
public class WorldMapTextSuppressMixin {
	@Inject(
		method = "drawNormalText(Lnet/minecraft/class_4587;Ljava/lang/String;FFIZLnet/minecraft/class_4597;)V",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	private static void sharedwaypoint$suppressWorldMapText(
		PoseStack poseStack,
		String text,
		float x,
		float y,
		int color,
		boolean shadow,
		MultiBufferSource buffer,
		CallbackInfo ci
	) {
		if (SharedWaypointHaloState.isHaloPass()) {
			ci.cancel();
		}
	}
}
