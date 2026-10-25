package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.MoonValeVoice;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "su.plo.voice.client.audio.device.AlOutputDevice$AlListener", remap = false)
public class PlasmoVoiceListenerMixin {
   @Redirect(
      method = "update()V",
      at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL11;alListener3f(IFFF)V", remap = false),
      remap = false,
      require = 1
   )
   private void freecam$anchorVoicePosition(int parameter, float x, float y, float z) {
      Entity listener = MoonValeVoice.playerListener();
      if (parameter == AL11.AL_POSITION && listener != null) {
         Vec3 eye = listener.getEyePosition();
         x = (float)eye.x;
         y = (float)eye.y;
         z = (float)eye.z;
      }
      AL11.alListener3f(parameter, x, y, z);
   }

   @Redirect(
      method = "update()V",
      at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL11;alListenerfv(I[F)V", remap = false),
      remap = false,
      require = 1
   )
   private void freecam$rotateVoiceWithCamera(int parameter, float[] orientation) {
      if (parameter == AL11.AL_ORIENTATION && MoonValeVoice.playerListener() != null) {
         Camera camera = Freecam.MC.gameRenderer.mainCamera();
         Vector3fc forward = camera.forwardVector();
         Vector3fc up = camera.upVector();
         orientation = new float[]{forward.x(), forward.y(), forward.z(), up.x(), up.y(), up.z()};
      }
      AL11.alListenerfv(parameter, orientation);
   }
}
