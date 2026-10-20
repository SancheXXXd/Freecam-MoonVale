package net.xolt.freecam.config.load;

import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigratingConfigLoader<T> implements ConfigLoader<T> {
   private static final Logger LOGGER = LoggerFactory.getLogger(MigratingConfigLoader.class);
   private final ConfigLoader<T> primaryLoader;
   private final ConfigLoader<T> legacyLoader;

   public MigratingConfigLoader(ConfigLoader<T> primaryLoader, ConfigLoader<T> legacyLoader) {
      this.primaryLoader = primaryLoader;
      this.legacyLoader = legacyLoader;
   }

   @Override
   public T read() throws Exception {
      Path primaryPath = this.primaryLoader.getFilepath();
      Path legacyPath = this.legacyLoader.getFilepath();
      if (!Files.exists(primaryPath) && Files.exists(legacyPath)) {
         LOGGER.info("{} not found, attempting to migrate legacy config {}", primaryPath.getFileName(), legacyPath.getFileName());

         try {
            return this.legacyLoader.read();
         } catch (Exception var4) {
            LOGGER.warn("Failed to migrate legacy config {}, falling back to defaults", legacyPath.getFileName(), var4);
         }
      }

      return this.primaryLoader.read();
   }

   @Override
   public void write(T config) throws Exception {
      this.primaryLoader.write(config);
   }

   @Override
   public Path getFilepath() {
      return this.primaryLoader.getFilepath();
   }
}
