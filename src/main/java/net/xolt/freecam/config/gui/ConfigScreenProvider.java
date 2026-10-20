package net.xolt.freecam.config.gui;

import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.MultiServiceLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ConfigScreenProvider {
   String getName();

   Screen getConfigScreen(Screen var1);

   default void openConfigScreen() {
      this.openConfigScreen(Freecam.MC.gui.screen());
   }

   default void openConfigScreen(Screen parent) {
      Freecam.MC.gui.setScreen(this.getConfigScreen(parent));
   }

   static ConfigScreenProvider provider() {
      return ConfigScreenProvider.Holder.INSTANCE;
   }

   public static final class Holder {
      private static final Logger LOGGER = LoggerFactory.getLogger(ConfigScreenProvider.class);
      private static final ConfigScreenProvider INSTANCE;

      private Holder() {
      }

      @Nullable
      private static ConfigScreenProvider attemptLoad(Provider<ConfigScreenProvider> provider) {
         try {
            return provider.get();
         } catch (Throwable var2) {
            LOGGER.error("{} provider {} failed to load", new Object[]{ConfigScreenProvider.class.getSimpleName(), provider.type().getSimpleName(), var2});
            return null;
         }
      }

      static {
         Collection<Provider<ConfigScreenProvider>> providers = MultiServiceLoader.get(ConfigScreenProvider.class);
         LOGGER.debug("Loaded: {}", providers.stream().map(provider -> provider.type().getSimpleName()).collect(Collectors.joining(", ")));
         INSTANCE = providers.stream()
            .map(ConfigScreenProvider.Holder::attemptLoad)
            .filter(Objects::nonNull)
            .filter(provider -> !(provider instanceof OptionalProvider optional && !optional.isAvailable()))
            .findFirst()
            .orElseGet(FallbackConfigScreenProvider::new);
         LOGGER.info("Using {}", INSTANCE.getName());
      }
   }
}
