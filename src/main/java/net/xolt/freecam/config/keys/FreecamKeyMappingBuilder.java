package net.xolt.freecam.config.keys;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

public class FreecamKeyMappingBuilder {
   private final String translationKey;
   private Type type = Type.KEYBOARD;
   private int keyCode = InputConstants.UNKNOWN.getValue();
   private Runnable action;
   private HoldAction holdAction;
   private long maxTicks = 10L;

   private FreecamKeyMappingBuilder(String translationKey) {
      this.translationKey = translationKey;
   }

   public static FreecamKeyMappingBuilder builder(String translationKey) {
      return new FreecamKeyMappingBuilder(translationKey);
   }

   public FreecamKeyMappingBuilder type(Type type) {
      this.type = type;
      return this;
   }

   public FreecamKeyMappingBuilder maxHoldTicks(long ticks) {
      this.maxTicks = ticks;
      return this;
   }

   public FreecamKeyMappingBuilder defaultKey(int keyCode) {
      this.keyCode = keyCode;
      return this;
   }

   public FreecamKeyMappingBuilder action(Runnable action) {
      this.action = action;
      return this;
   }

   public FreecamKeyMappingBuilder holdAction(HoldAction action) {
      this.holdAction = action;
      return this;
   }

   public FreecamKeyMapping build() {
      if (this.action != null && this.holdAction != null) {
         return new FreecamComboKeyMapping(this.translationKey, this.type, this.keyCode, this.action, this.holdAction, this.maxTicks);
      } else if (this.action != null) {
         return new FreecamKeyMapping(this.translationKey, this.type, this.keyCode, self -> {
            while (self.consumeClick()) {
               this.action.run();
            }
         });
      } else if (this.holdAction != null) {
         return new FreecamKeyMapping(this.translationKey, this.type, this.keyCode, self -> {
            if (self.isDown()) {
               this.holdAction.run();
            }
         });
      } else {
         throw new IllegalStateException("No action defined.");
      }
   }
}
