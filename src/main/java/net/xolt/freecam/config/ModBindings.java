package net.xolt.freecam.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.gui.ConfigScreenProvider;
import net.xolt.freecam.config.keys.FreecamKeyMapping;
import net.xolt.freecam.config.keys.FreecamKeyMappingBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ModBindings {
   KEY_TOGGLE(() -> FreecamKeyMappingBuilder.builder("toggle").action(Freecam::toggle).holdAction(Freecam::activateTripodHandler).defaultKey(61).build()),
   KEY_TRIPOD_RESET(() -> FreecamKeyMappingBuilder.builder("tripod.reset").holdAction(Freecam::resetTripodHandler).build()),
   KEY_CONFIG_GUI(() -> FreecamKeyMappingBuilder.builder("config.open").action(ConfigScreenProvider.provider()::openConfigScreen).build());

   private static final Logger LOGGER = LoggerFactory.getLogger(ModBindings.class);
   private final Supplier<FreecamKeyMapping> lazyMapping;

   private ModBindings(Supplier<FreecamKeyMapping> mappingSupplier) {
      this.lazyMapping = Suppliers.memoize(mappingSupplier);
   }

   public FreecamKeyMapping get() {
      return (FreecamKeyMapping)this.lazyMapping.get();
   }

   public static void forEach(@NotNull Consumer<FreecamKeyMapping> action) {
      Objects.requireNonNull(action);
      iterator().forEachRemaining(action);
   }

   @NotNull
   public static Iterator<FreecamKeyMapping> iterator() {
      return stream().iterator();
   }

   @NotNull
   public static Spliterator<FreecamKeyMapping> spliterator() {
      return stream().spliterator();
   }

   @NotNull
   public static Stream<FreecamKeyMapping> stream() {
      return Arrays.stream(values()).map(ModBindings::get);
   }

   private static Logger logger() {
      return LOGGER;
   }
}
