package net.xolt.freecam.fabric.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
   @Shadow
   protected abstract EntityRenderState extractEntity(Entity var1, float var2);

   @Inject(method = "extractVisibleEntities", at = @At("RETURN"))
   private void onExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState renderState, CallbackInfo ci) {
      if (Freecam.MC.level != null && Freecam.isEnabled() && ModConfig.get().shouldShowPlayer()) {
         Entity player = Freecam.MC.player;
         TickRateManager tickRateManager = Freecam.MC.level.tickRateManager();
         float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(player));
         EntityRenderState entityRenderState = this.extractEntity(player, g);
         renderState.entityRenderStates.add(entityRenderState);
      }
   }
}
