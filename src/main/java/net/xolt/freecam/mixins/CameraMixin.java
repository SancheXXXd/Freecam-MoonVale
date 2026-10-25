package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.util.CameraCollision;
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

   @Shadow
   protected void setPosition(Vec3 position) {
      throw new AssertionError();
   }

   @Shadow
   private void alignWithEntity(float partialTick) {
      throw new AssertionError();
   }

   @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getViewRotationMatrix(Lorg/joml/Matrix4f;)Lorg/joml/Matrix4f;"))
   private void freecam$protectView(DeltaTracker tracker, CallbackInfo ci) {
      if (!(this.entity instanceof FreeCamera camera) || !Freecam.isEnabled()) {
         return;
      }
      Camera view = (Camera)(Object)this;
      double aspect = (double)Freecam.MC.getWindow().getWidth() / Math.max(1, Freecam.MC.getWindow().getHeight());
      double radius = CameraCollision.renderRadius(view.getFov(), aspect);
      Vec3 anchor = camera.getEyePosition();
      if (!CameraCollision.isClear(Freecam.MC.level, anchor, radius)) {
         Freecam.toggle();
         this.alignWithEntity(view.getCameraEntityPartialTicks(tracker));
         return;
      }
      Vec3 safe = CameraCollision.clip(Freecam.MC.level, anchor, view.position(), radius);
      this.setPosition(safe);
   }

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
