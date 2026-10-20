package net.xolt.freecam.config.gui;

import net.minecraft.client.gui.screens.Screen;
import net.xolt.freecam.config.controller.ConfigControllerRegistry;
import net.xolt.freecam.config.model.ModConfigDTO;
import org.jetbrains.annotations.Nullable;

public class ClothConfigScreenProvider implements ConfigScreenProvider, OptionalProvider {
   @Nullable
   private ModConfigScreenFactory factory;

   private ModConfigScreenFactory factory() {
      if (this.factory == null) {
         this.factory = new ModConfigScreenFactory(ConfigControllerRegistry.get(ModConfigDTO.class));
      }

      return this.factory;
   }

   @Override
   public String getName() {
      return "Cloth Config Freecam GUI";
   }

   @Override
   public Screen getConfigScreen(@Nullable Screen parent) {
      return this.factory().getConfigScreen(parent);
   }

   @Override
   public boolean isAvailable() {
      try {
         Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder", false, Thread.currentThread().getContextClassLoader());
         return true;
      } catch (LinkageError | ClassNotFoundException var2) {
         return false;
      }
   }
}
