package net.xolt.freecam.config.load;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class BasicConfigLoader<T, S> implements ConfigLoader<T> {
   private final ConfigSerializer<S> serializer;
   private final Class<T> configClass;
   private final Path filepath;

   public BasicConfigLoader(ConfigSerializer<S> serializer, Class<T> configClass, Path filepath) {
      this.serializer = serializer;
      this.configClass = configClass;
      this.filepath = filepath;
   }

   @Override
   public T read() throws Exception {
      if (!Files.exists(this.filepath)) {
         return this.configClass.getConstructor().newInstance();
      } else {
         S data;
         try (BufferedReader reader = Files.newBufferedReader(this.filepath)) {
            data = this.serializer.parse(reader);
         }

         return this.serializer.deserialize(data, this.configClass);
      }
   }

   @Override
   public void write(T config) throws Exception {
      S data = this.serializer.serialize(config);
      Files.createDirectories(this.filepath.getParent());

      try (BufferedWriter writer = Files.newBufferedWriter(this.filepath)) {
         this.serializer.write(data, writer);
      }
   }

   @Override
   public Path getFilepath() {
      return this.filepath;
   }
}
