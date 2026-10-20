package net.xolt.freecam.config.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.xolt.freecam.config.load.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicConfigController<T> implements ConfigController<T> {
   private static final Logger LOGGER = LoggerFactory.getLogger(BasicConfigController.class);
   private final ConfigLoader<T> loader;
   private final List<Runnable> listeners = new ArrayList<>();
   private final T defaults;
   private final Supplier<T> defaultSupplier;
   private T config;

   public BasicConfigController(ConfigLoader<T> loader, Supplier<T> defaultSupplier) {
      this.loader = loader;
      this.defaults = defaultSupplier.get();
      this.defaultSupplier = defaultSupplier;
   }

   @Override
   public T getConfig() {
      if (this.config == null) {
         throw new IllegalStateException("Freecam config used before load");
      } else {
         return this.config;
      }
   }

   @Override
   public T getDefaults() {
      return this.defaults;
   }

   @Override
   public void load() {
      try {
         this.config = this.loader.read();
      } catch (Exception var2) {
         LOGGER.error("Failed to load config, using defaults", var2);
         this.config = this.defaultSupplier.get();
      }

      this.listeners.forEach(Runnable::run);
   }

   @Override
   public void save() {
      T config = this.config;
      if (config != null) {
         try {
            this.loader.write(config);
         } catch (Exception var3) {
            LOGGER.error("Failed to save config", var3);
         }

         this.listeners.forEach(Runnable::run);
      }
   }

   @Override
   public void registerListener(Runnable listener) {
      this.listeners.add(listener);
   }
}
