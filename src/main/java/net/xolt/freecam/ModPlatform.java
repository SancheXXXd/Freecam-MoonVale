package net.xolt.freecam;

import java.util.Collection;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import net.xolt.freecam.util.MultiServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ModPlatform {
   boolean isModLoaded(String var1);

   static ModPlatform get() {
      return ModPlatform.Holder.INSTANCE;
   }

   public static final class Holder {
      private static final Logger LOGGER = LoggerFactory.getLogger("Freecam/ModPlatform");
      private static final ModPlatform INSTANCE;

      private Holder() {
      }

      static {
         Collection<Provider<ModPlatform>> providers = MultiServiceLoader.get(ModPlatform.class);
         LOGGER.debug("Loaded: {}", providers.stream().map(provider -> provider.type().getSimpleName()).collect(Collectors.joining(", ")));
         INSTANCE = providers.stream().findFirst().map(provider -> {
            LOGGER.debug("Using {}", provider.type().getSimpleName());
            return provider.get();
         }).orElseThrow(() -> {
            String msg = "No provider found";
            LOGGER.error(msg);
            return new IllegalStateException(msg);
         });
      }
   }
}
