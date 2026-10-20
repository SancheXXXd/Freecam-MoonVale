package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.MoonValeAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "su.plo.voice.client.audio.device.AlOutputDevice$AlListener", remap = false)
public class PlasmoVoiceListenerMixin {
   private static final int AL_GAIN = 4106;

   @Redirect(
      method = "update",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;position()Lnet/minecraft/world/phys/Vec3;", remap = true),
      remap = false,
      require = 0
   )
   private Vec3 freecam$playerPosition(Camera camera) {
      return Freecam.isEnabled() && Freecam.MC.player != null ? Freecam.MC.player.getEyePosition() : camera.position();
   }

   @Inject(method = "update", at = @At("HEAD"), remap = false, require = 0)
   private void freecam$syncVoiceOutputGain(CallbackInfo var1) {
      MoonValeAudio.onListenerUpdate(Freecam.isEnabled());
   }
}
