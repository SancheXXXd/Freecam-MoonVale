package net.xolt.freecam.util;

public final class VoiceSpatialTransform {
   private VoiceSpatialTransform() {
   }

   public static float[] reproject(double playerX, double playerY, double playerZ,
                                   double cameraX, double cameraY, double cameraZ, float[] source) {
      if (source == null || source.length != 3) {
         return source;
      }
      double dx = source[0] - playerX;
      double dy = source[1] - playerY;
      double dz = source[2] - playerZ;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      double bx = source[0] - cameraX;
      double by = source[1] - cameraY;
      double bz = source[2] - cameraZ;
      double bearingLength = Math.sqrt(bx * bx + by * by + bz * bz);
      if (!Double.isFinite(distance) || !Double.isFinite(bearingLength)) {
         return source;
      }
      if (bearingLength < 1.0E-6 || distance < 1.0E-6) {
         return source;
      }
      double scale = distance / bearingLength;
      double x = playerX + bx * scale;
      double y = playerY + by * scale;
      double z = playerZ + bz * scale;
      if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
         return source;
      }
      return new float[]{(float)x, (float)y, (float)z};
   }
}
