package net.xolt.freecam.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.MoonValeVoice;
import net.xolt.freecam.util.VoiceSpatialTransform;
import org.lwjgl.openal.AL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "su.plo.voice.client.audio.device.source.BaseAlSource", remap = false)
public abstract class PlasmoVoiceSpatialMixin {
   @Shadow(remap = false)
   public abstract boolean isRelative();

   @Redirect(
      method = "setFloatArray(I[F)V",
      at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL11;alSourcefv(II[F)V", remap = false),
      remap = false,
      require = 1
   )
   private void freecam$projectVoiceBearing(int pointer, int parameter, float[] values) {
      Entity listener = MoonValeVoice.playerListener();
      if (parameter == AL11.AL_POSITION && listener != null && !this.isRelative()) {
         Vec3 player = listener.getEyePosition();
         Vec3 camera = Freecam.MC.gameRenderer.mainCamera().position();
         values = VoiceSpatialTransform.reproject(player.x, player.y, player.z, camera.x, camera.y, camera.z, values);
      }
      AL11.alSourcefv(pointer, parameter, values);
   }
}
