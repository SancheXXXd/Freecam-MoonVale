package net.xolt.freecam.config.keys;

import com.mojang.blaze3d.platform.InputConstants.Type;

class FreecamComboKeyMapping extends FreecamKeyMapping {
   private final Runnable action;
   private final HoldAction holdAction;
   private final long maxHoldTicks;
   private long sinceLastUse = 0L;
   private long sinceLastPress = 0L;
   private long queued = 0L;

   FreecamComboKeyMapping(String translationKey, Type type, int code, Runnable action, HoldAction holdAction, long maxHoldTicks) {
      super(translationKey, type, code);
      this.action = action;
      this.holdAction = holdAction;
      this.maxHoldTicks = maxHoldTicks;
   }

   @Override
   public void tick() {
      this.sinceLastUse++;
      this.sinceLastPress++;
      if (this.isDown()) {
         if (this.holdAction.run()) {
            this.markUsed();
         }
      } else {
         while (this.dequeue()) {
            this.action.run();
         }
      }
   }

   public void setDown(boolean down) {
      if (down != this.isDown()) {
         if (down) {
            this.keyDown();
         } else {
            this.keyUp();
         }
      }

      super.setDown(down);
   }

   private void keyDown() {
      this.sinceLastPress = 0L;
   }

   private void keyUp() {
      if (this.sinceLastUse > this.sinceLastPress && this.sinceLastPress < this.maxHoldTicks) {
         this.queued++;
      }

      this.markUsed();
   }

   private void markUsed() {
      this.sinceLastUse = 0L;
   }

   private boolean dequeue() {
      if (this.queued < 1L) {
         return false;
      } else {
         this.queued--;
         return true;
      }
   }
}
