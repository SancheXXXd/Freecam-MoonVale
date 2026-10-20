package net.xolt.freecam.mixins;

import net.minecraft.client.player.LocalPlayer;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends EntityMixin {
   @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
   private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
      if (Freecam.isEnabled() && this.freecam$this() == Freecam.MC.player) {
         cir.setReturnValue(true);
      }
   }

   @Override
   protected void onGetViewXRot(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (this.freecam$useFreecamRotation()) {
         cir.setReturnValue(Freecam.getFreeCamera().getViewXRot(partialTick));
      }
   }

   @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
   private void onGetViewYRot(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (this.freecam$useFreecamRotation()) {
         cir.setReturnValue(Freecam.getFreeCamera().getViewYRot(partialTick));
      }
   }

   @Unique
   private boolean freecam$useFreecamRotation() {
      return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && !ModConfig.get().allowInteractionsFromPlayer();
   }

   @Unique
   private LocalPlayer freecam$this() {
      return (LocalPlayer)(Object)this;
   }
}
