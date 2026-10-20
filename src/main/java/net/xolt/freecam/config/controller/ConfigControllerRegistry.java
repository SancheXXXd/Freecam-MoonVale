package net.xolt.freecam.config.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigControllerRegistry {
   private static final Map<Class<?>, ConfigController<?>> CONTROLLERS = new ConcurrentHashMap<>();

   private ConfigControllerRegistry() {
   }

   public static <T> ConfigController<T> get(Class<T> configClass) {
      ConfigController<T> controller = (ConfigController<T>)CONTROLLERS.get(configClass);
      if (controller == null) {
         throw new IllegalArgumentException("No controller registered for " + configClass.getSimpleName());
      } else {
         return controller;
      }
   }

   public static <T> void register(Class<T> configClass, ConfigController<T> controller) {
      CONTROLLERS.put(configClass, controller);
   }
}
