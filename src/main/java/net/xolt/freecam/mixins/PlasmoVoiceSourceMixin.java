package net.xolt.freecam.mixins;

import net.minecraft.world.entity.Entity;
import net.xolt.freecam.util.MoonValeVoice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "su.plo.voice.client.audio.source.BaseClientAudioSource", remap = false)
public class PlasmoVoiceSourceMixin {
   @Inject(method = "getListener()Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
   private void freecam$usePlayerForVoiceDistance(CallbackInfoReturnable<Entity> cir) {
      Entity listener = MoonValeVoice.playerListener();
      if (listener != null) {
         cir.setReturnValue(listener);
      }
   }
}
