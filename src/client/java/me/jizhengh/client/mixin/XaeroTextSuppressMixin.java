package me.jizhengh.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jizhengh.client.render.SharedWaypointHaloState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.misc.Misc;

@Mixin(value = Misc.class, remap = false)
public class XaeroTextSuppressMixin {
	@Inject(
		method = "drawNormalText",
		at = @At("HEAD"),
		cancellable = true,
		remap = false,
		require = 0
	)
	private static void sharedwaypoint$suppressNormalTextString(
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
