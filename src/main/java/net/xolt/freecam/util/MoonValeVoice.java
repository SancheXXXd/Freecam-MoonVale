package net.xolt.freecam.util;

import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;

public final class MoonValeVoice {
   private MoonValeVoice() {
   }

   public static Entity playerListener() {
      if (Freecam.MC.player != null
         && (Freecam.isEnabled() || Freecam.MC.getCameraEntity() instanceof FreeCamera)) {
         return Freecam.MC.player;
      }
      return null;
   }
}
