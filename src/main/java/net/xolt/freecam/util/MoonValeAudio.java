package net.xolt.freecam.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.lwjgl.openal.AL11;

public final class MoonValeAudio {
   private static final int AL_GAIN = 4106;
   private static final Map<Integer, Float> SAVED_SOURCE_GAIN = new ConcurrentHashMap<>();
   private static volatile boolean muted;

   private MoonValeAudio() {
   }

   public static void onListenerUpdate(boolean var0) {
      if (var0) {
         AL11.alListenerf(4106, 0.0F);
         muted = true;
      } else if (muted) {
         muted = false;
         AL11.alListenerf(4106, 1.0F);

         for (Entry var2 : SAVED_SOURCE_GAIN.entrySet()) {
            AL11.alSourcef((Integer)var2.getKey(), 4106, (Float)var2.getValue());
         }

         SAVED_SOURCE_GAIN.clear();
         AL11.alGetError();
      }
   }

   public static void setSourceFloat(int var0, int var1, float var2, boolean var3) {
      if (var1 == 4106) {
         if (var3) {
            SAVED_SOURCE_GAIN.put(var0, var2);
            muted = true;
            AL11.alSourcef(var0, var1, 0.0F);
            return;
         }

         SAVED_SOURCE_GAIN.remove(var0);
      }

      AL11.alSourcef(var0, var1, var2);
   }
}
