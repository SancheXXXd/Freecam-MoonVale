package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
   @Invoker
   abstract EntityRenderState callExtractEntity(Entity var1, float var2);

   @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
   private void onExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState, CallbackInfo ci) {
      if (Freecam.isEnabled() && Freecam.isOutlineEnabled()) {
         float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
         EntityRenderState state = this.callExtractEntity(Freecam.MC.player, partialTick);
         state.outlineColor = ARGB.opaque(Freecam.MC.player.getTeamColor());
         levelRenderState.entityRenderStates.add(state);
         levelRenderState.shouldShowEntityOutlines = true;
      }
   }
}
