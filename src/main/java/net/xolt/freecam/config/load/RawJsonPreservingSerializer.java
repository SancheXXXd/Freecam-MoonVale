package net.xolt.freecam.config.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Reader;
import java.io.Writer;
import java.util.Map.Entry;

public class RawJsonPreservingSerializer implements ConfigSerializer<JsonElement> {
   private final ConfigSerializer<JsonElement> delegate;

   public RawJsonPreservingSerializer() {
      this(new GsonSerializer());
   }

   public RawJsonPreservingSerializer(ConfigSerializer<JsonElement> delegate) {
      this.delegate = delegate;
   }

   public <T> T deserialize(JsonElement json, Class<T> configClass) {
      T config = this.delegate.deserialize(json, configClass);
      if (config instanceof RawJsonHolder holder && json.isJsonObject()) {
         holder.setRawJson(json.getAsJsonObject());
      }

      return config;
   }

   public <T> JsonElement serialize(T config) {
      JsonElement current = this.delegate.serialize(config);
      if (config instanceof RawJsonHolder holder) {
         JsonObject previous = holder.getRawJson();
         if (previous != null) {
            return this.merge(current, previous);
         }
      }

      return current;
   }

   public JsonElement parse(Reader reader) throws Exception {
      return this.delegate.parse(reader);
   }

   public void write(JsonElement json, Writer writer) throws Exception {
      this.delegate.write(json, writer);
   }

   JsonElement merge(JsonElement current, JsonElement previous) {
      if (current == null) {
         return previous;
      } else if (previous == null) {
         return current;
      } else if (current.isJsonObject() && previous.isJsonObject()) {
         JsonObject result = GsonCompat.deepCopy(current).getAsJsonObject();

         for (Entry<String, JsonElement> entry : previous.getAsJsonObject().entrySet()) {
            String key = entry.getKey();
            JsonElement previousValue = entry.getValue();
            JsonElement currentValue = result.get(key);
            if (currentValue == null || currentValue.isJsonObject() && previousValue.isJsonObject()) {
               result.add(key, this.merge(currentValue, previousValue));
            }
         }

         return result;
      } else {
         return current;
      }
   }
}
