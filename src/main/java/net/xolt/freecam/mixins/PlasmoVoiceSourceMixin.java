package net.xolt.freecam.mixins;

import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.MoonValeAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "su.plo.voice.client.audio.device.source.BaseAlSource", remap = false)
public class PlasmoVoiceSourceMixin {
   private static final int AL_GAIN = 4106;

   @Redirect(method = "setFloat", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL11;alSourcef(IIF)V", remap = false), remap = false, require = 0)
   private void freecam$muteVoiceSource(int var1, int var2, float var3) {
      MoonValeAudio.setSourceFloat(var1, var2, var3, Freecam.isEnabled());
   }
}
