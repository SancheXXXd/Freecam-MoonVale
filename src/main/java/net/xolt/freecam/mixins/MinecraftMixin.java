package net.xolt.freecam.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void onDoAttack(CallbackInfoReturnable<Boolean> ci) {
      if (freecam$disableInteract()) {
         ci.cancel();
      }
   }

   @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
   private void onDoItemPick(CallbackInfo ci) {
      if (freecam$disableInteract()) {
         ci.cancel();
      }
   }

   @ModifyVariable(
      method = "pick(F)V",
      at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;")
   )
   private Entity onGetHitTargetSource(Entity entity) {
      return (Entity)(!Freecam.isEnabled() || !Freecam.isPlayerControlEnabled() && !ModConfig.get().allowInteractionsFromPlayer() ? entity : Freecam.MC.player);
   }

   @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
   private void onHandleBlockBreaking(CallbackInfo ci) {
      if (freecam$disableInteract()) {
         ci.cancel();
      }
   }

   @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2), cancellable = true)
   private void onHandleInputEvents(CallbackInfo ci) {
      if (ModBindings.KEY_TOGGLE.get().isDown() || ModBindings.KEY_TRIPOD_RESET.get().isDown()) {
         ci.cancel();
      }
   }

   @Inject(method = "disconnect*", at = @At("HEAD"))
   private void onDisconnect(CallbackInfo ci) {
      Freecam.onDisconnect();
   }

   @Unique
   private static boolean freecam$disableInteract() {
      return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && ModConfig.get().shouldPreventInteractions();
   }
}
