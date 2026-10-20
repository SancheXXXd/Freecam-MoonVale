package net.xolt.freecam.fabric.mixins;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.xolt.freecam.fabric.BoundaryRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class BoundaryMixin {
   @Shadow
   @Final
   private SubmitNodeStorage submitNodeStorage;

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
         shift = Shift.AFTER
      )
   )
   private void freecam$submitBoundary(
      GraphicsResourceAllocator allocator,
      boolean renderBlockOutline,
      CameraRenderState cameraState,
      GpuBufferSlice fogBuffer,
      Vector4f clearColor,
      boolean blit,
      boolean renderSky,
      CallbackInfo ci
   ) {
      BoundaryRenderer.submit(this.submitNodeStorage, cameraState);
   }
}
