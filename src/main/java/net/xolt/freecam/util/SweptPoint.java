package net.xolt.freecam.util;

public final class SweptPoint {
   private SweptPoint() {
   }

   public static double firstHit(double x, double y, double z, double dx, double dy, double dz,
                                 double minX, double minY, double minZ,
                                 double maxX, double maxY, double maxZ) {
      double enter = Double.NEGATIVE_INFINITY;
      double exit = Double.POSITIVE_INFINITY;
      double[] origin = {x, y, z};
      double[] motion = {dx, dy, dz};
      double[] min = {minX, minY, minZ};
      double[] max = {maxX, maxY, maxZ};
      for (int axis = 0; axis < 3; axis++) {
         if (motion[axis] == 0.0) {
            if (origin[axis] <= min[axis] || origin[axis] >= max[axis]) {
               return Double.POSITIVE_INFINITY;
            }
         } else {
            double a = (min[axis] - origin[axis]) / motion[axis];
            double b = (max[axis] - origin[axis]) / motion[axis];
            enter = Math.max(enter, Math.min(a, b));
            exit = Math.min(exit, Math.max(a, b));
            if (enter >= exit) {
               return Double.POSITIVE_INFINITY;
            }
         }
      }
      if (exit <= 0.0 || enter > 1.0) {
         return Double.POSITIVE_INFINITY;
      }
      return Math.max(0.0, enter);
   }
}
