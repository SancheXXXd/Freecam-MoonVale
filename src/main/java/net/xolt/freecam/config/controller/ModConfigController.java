package net.xolt.freecam.config.controller;

import java.util.ArrayList;
import java.util.List;
import net.xolt.freecam.config.model.ModConfigDTO;
import net.xolt.freecam.config.model.ModConfigImpl;

public class ModConfigController implements ConfigController<ModConfigImpl> {
   private final ConfigController<ModConfigDTO> controller;
   private final List<Runnable> listeners = new ArrayList<>();
   private final ModConfigImpl defaultConfig;
   private ModConfigImpl config;

   public ModConfigController(ConfigController<ModConfigDTO> dtoController) {
      this.controller = dtoController;
      this.defaultConfig = new ModConfigImpl(new ModConfigDTO());
      this.controller.registerListener(this::onChange);
   }

   private void onChange() {
      this.config = new ModConfigImpl(this.controller.getConfig());
      this.listeners.forEach(Runnable::run);
   }

   public ModConfigImpl getConfig() {
      return this.config;
   }

   public ModConfigImpl getDefaults() {
      return this.defaultConfig;
   }

   @Override
   public void load() {
      this.controller.load();
   }

   @Override
   public void save() {
      this.controller.save();
   }

   @Override
   public void registerListener(Runnable listener) {
      this.listeners.add(listener);
   }
}
