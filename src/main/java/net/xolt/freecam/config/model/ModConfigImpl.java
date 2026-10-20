package net.xolt.freecam.config.model;

import net.minecraft.world.level.block.Block;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.MoonValePatches;

public class ModConfigImpl implements ModConfig {
   private final ModConfigDTO data;

   public ModConfigImpl(ModConfigDTO var1) {
      this.data = var1;
   }

   public ModConfigDTO getData() {
      return this.data;
   }

   @Override
   public FlightMode getFlightMode() {
      return this.data.movement.flightMode;
   }

   @Override
   public double getHorizontalSpeed() {
      return MoonValePatches.clampSpeed(this.data.movement.horizontalSpeed);
   }

   @Override
   public double getVerticalSpeed() {
      return MoonValePatches.clampSpeed(this.data.movement.verticalSpeed);
   }

   @Override
   public boolean ignoreAllCollision() {
      return false;
   }

   @Override
   public boolean shouldCheckInitialCollision() {
      return true;
   }

   @Override
   public boolean ignoreCollisionWith(Block var1) {
      return false;
   }

   @Override
   public Perspective getInitialPerspective() {
      return this.data.visual.perspective;
   }

   @Override
   public boolean shouldShowPlayer() {
      return this.data.visual.showPlayer;
   }

   @Override
   public boolean shouldShowHand() {
      return this.data.visual.showHand;
   }

   @Override
   public boolean isFullBrightEnabled() {
      return this.data.visual.fullBright;
   }

   @Override
   public boolean shouldShowSubmersionFog() {
      return true;
   }

   @Override
   public boolean shouldDisableOnDamage() {
      return this.data.utility.disableOnDamage;
   }

   @Override
   public boolean shouldFreezePlayer() {
      return false;
   }

   @Override
   public boolean shouldPreventInteractions() {
      return true;
   }

   public boolean allowInteractionsFrom(ModConfigDTO.InteractionMode var1) {
      return false;
   }

   @Override
   public boolean allowInteractionsFromCamera() {
      return false;
   }

   @Override
   public boolean allowInteractionsFromPlayer() {
      return false;
   }

   @Override
   public boolean isRestrictedOnServer(String var1) {
      return false;
   }

   @Override
   public boolean shouldNotifyFreecam() {
      return this.data.notification.notifyFreecam;
   }

   @Override
   public boolean shouldNotifyTripod() {
      return this.data.notification.notifyTripod;
   }

   @Override
   public boolean shouldOutlinePlayer() {
      return false;
   }

   @Override
   public boolean isDiscordRpcEnabled() {
      return this.data.discord.enabled;
   }
}
