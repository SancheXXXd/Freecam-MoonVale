package net.xolt.freecam.mixins;

import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
   @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
   protected void onGetViewXRot(float partialTick, CallbackInfoReturnable<Float> cir) {
   }

   @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
   private void onChangeLookDirection(double rotation, double pitch, CallbackInfo ci) {
      if (Freecam.isEnabled() && this.freecam$this() == Freecam.MC.player && !Freecam.isPlayerControlEnabled()) {
         Freecam.getFreeCamera().turn(rotation, pitch);
         ci.cancel();
      }
   }

   @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
      if (Freecam.isEnabled() && (entity == Freecam.getFreeCamera() || this.freecam$this() == Freecam.getFreeCamera())) {
         ci.cancel();
      }
   }

   @Inject(method = "setDeltaMovement(DDD)V", at = @At("HEAD"), cancellable = true)
   private void onSetVelocity(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
   private void onUpdateVelocity(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
   private void onSetPosition(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
   private void onSetPos(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Unique
   private Entity freecam$this() {
      return (Entity)(Object)this;
   }

   @Unique
   private boolean freecam$shouldFreeze() {
      return Freecam.isEnabled() && this.freecam$this() == Freecam.MC.player && this.freecam$allowFreeze();
   }

   @Unique
   private boolean freecam$allowFreeze() {
      return ModConfig.get().shouldFreezePlayer() && !Freecam.isPlayerControlEnabled();
   }
}
