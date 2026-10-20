package net.xolt.freecam.config.keys;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;

public class FreecamKeyMapping extends KeyMapping implements Tickable {
   private static final Category FREECAM_CATEGORY = Category.register(Identifier.fromNamespaceAndPath("moonvalefreecam", "controls"));
   private final Consumer<FreecamKeyMapping> onTick;

   protected FreecamKeyMapping(String translationKey, Type type, int code) {
      this(translationKey, type, code, null);
   }

   FreecamKeyMapping(String translationKey, Type type, int code, Consumer<FreecamKeyMapping> onTick) {
      super("key.moonvalefreecam." + translationKey, type, code, FREECAM_CATEGORY);
      this.onTick = onTick;
   }

   @Override
   public void tick() {
      this.onTick.accept(this);
   }

   public void reset() {
      while (this.consumeClick()) {
      }
   }
}
