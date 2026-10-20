package net.xolt.freecam.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.config.model.FlightMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Shadow
   public abstract float getHealth();

   @Inject(method = "getFrictionInfluencedSpeed", at = @At("HEAD"), cancellable = true)
   private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
      if (Freecam.isEnabled() && ModConfig.get().getFlightMode().equals(FlightMode.CREATIVE) && this.freecam$this() == Freecam.getFreeCamera()) {
         cir.setReturnValue((float)(ModConfig.get().getHorizontalSpeed() / 10.0) * (Freecam.getFreeCamera().isSprinting() ? 2 : 1));
      }
   }

   @Inject(method = "setHealth", at = @At("HEAD"))
   private void onSetHealth(float health, CallbackInfo ci) {
      if (Freecam.isEnabled()
         && ModConfig.get().shouldDisableOnDamage()
         && this.freecam$this() == Freecam.MC.player
         && !Freecam.MC.player.isCreative()
         && this.getHealth() > health) {
         Freecam.disableNextTick();
      }
   }

   @Unique
   private LivingEntity freecam$this() {
      return (LivingEntity)(Object)this;
   }
}
