package net.xolt.freecam.config.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

final class GsonCompat {
   private static final Method PARSE_READER = setupParseReader();
   private static final Method DEEP_COPY = setupDeepCopy();
   private static Gson gsonInstance = null;

   private GsonCompat() {
   }

   public static JsonElement parse(Reader reader) throws JsonIOException, JsonSyntaxException {
      if (PARSE_READER != null) {
         try {
            return (JsonElement)PARSE_READER.invoke(null, reader);
         } catch (InvocationTargetException | IllegalAccessException var2) {
            throw new RuntimeException("Failed to invoke JsonParser.parseReader", var2);
         }
      } else {
         Gson gson = getGson();
         return (JsonElement)gson.fromJson(reader, JsonElement.class);
      }
   }

   public static <T extends JsonElement> T deepCopy(T element) {
      if (DEEP_COPY != null) {
         try {
            return (T)DEEP_COPY.invoke(element);
         } catch (InvocationTargetException | IllegalAccessException var2) {
            throw new RuntimeException("Failed to invoke JsonElement.deepCopy", var2);
         }
      } else {
         Gson gson = getGson();
         return (T)gson.fromJson(gson.toJson(element), JsonElement.class);
      }
   }

   private static Gson getGson() {
      if (gsonInstance == null) {
         gsonInstance = new Gson();
      }

      return gsonInstance;
   }

   @Nullable
   private static Method setupParseReader() {
      try {
         return JsonParser.class.getMethod("parseReader", Reader.class);
      } catch (NoSuchMethodException var1) {
         return null;
      }
   }

   @Nullable
   private static Method setupDeepCopy() {
      try {
         return JsonElement.class.getMethod("deepCopy");
      } catch (NoSuchMethodException var1) {
         return null;
      }
   }
}
