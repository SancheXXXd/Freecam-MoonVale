package net.xolt.freecam.config.load;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.Reader;
import java.io.Writer;

public class GsonSerializer implements ConfigSerializer<JsonElement> {
   private final Gson gson;

   public GsonSerializer() {
      this(new GsonBuilder().setPrettyPrinting().create());
   }

   public GsonSerializer(Gson gson) {
      this.gson = gson;
   }

   public JsonElement parse(Reader reader) throws JsonIOException, JsonSyntaxException {
      return GsonCompat.parse(reader);
   }

   public void write(JsonElement json, Writer writer) throws JsonIOException {
      this.gson.toJson(json, writer);
   }

   public <T> T deserialize(JsonElement json, Class<T> configClass) {
      return (T)this.gson.fromJson(json, configClass);
   }

   public <T> JsonObject serialize(T config) {
      return this.gson.toJsonTree(config).getAsJsonObject();
   }
}
