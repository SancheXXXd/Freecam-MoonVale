package net.xolt.freecam.mixins;

import net.minecraft.client.renderer.GameRenderer;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
   @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
   private void onRenderItemInHand(CallbackInfo ci) {
      if (Freecam.isEnabled() && ModConfig.get().shouldHideHand()) {
         ci.cancel();
      }
   }

   @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
   private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
      if (Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && ModConfig.get().shouldPreventInteractions()) {
         cir.setReturnValue(false);
      }
   }
}
