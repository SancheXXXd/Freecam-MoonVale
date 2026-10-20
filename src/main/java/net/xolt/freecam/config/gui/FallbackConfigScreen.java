package net.xolt.freecam.config.gui;

import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class FallbackConfigScreen extends GenericMessageScreen {
   private static final Component TITLE = Component.translatable("moonvalefreecam.config.gui.fallback.title");
   private final Screen parent;

   FallbackConfigScreen(Screen parent) {
      super(TITLE);
      this.parent = parent;
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }
}
