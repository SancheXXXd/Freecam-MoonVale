package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
   @Shadow
   private Entity entity;
   @Shadow
   private float eyeHeightOld;
   @Shadow
   private float eyeHeight;

   @Inject(method = "setEntity", at = @At("HEAD"))
   private void onSetEntity(Entity entity, CallbackInfo ci) {
      if (entity != null && this.entity != null) {
         if (entity instanceof FreeCamera || this.entity instanceof FreeCamera) {
            this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
         }
      }
   }

   @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
   public void onGetSubmersionType(CallbackInfoReturnable<FogType> cir) {
      if (Freecam.isEnabled() && ModConfig.get().shouldHideSubmersionFog()) {
         cir.setReturnValue(FogType.NONE);
      }
   }
}
