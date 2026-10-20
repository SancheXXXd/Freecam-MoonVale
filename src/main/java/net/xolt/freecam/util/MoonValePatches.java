package net.xolt.freecam.util;

public final class MoonValePatches {
   public static final double MIN_SPEED = 0.0;
   public static final double MAX_SPEED = 10.0;

   private MoonValePatches() {
   }

   public static double clampSpeed(double var0) {
      return Double.isNaN(var0) ? 1.0 : Math.max(0.0, Math.min(10.0, var0));
   }
}
