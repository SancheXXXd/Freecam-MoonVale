package net.xolt.freecam.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public class HudMixin {
   @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
   private void onGetCameraPlayer(CallbackInfoReturnable<Player> cir) {
      if (Freecam.isEnabled()) {
         cir.setReturnValue(Freecam.MC.player);
      }
   }

   @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
   private void onRenderTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha, CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         ci.cancel();
      }
   }
}
