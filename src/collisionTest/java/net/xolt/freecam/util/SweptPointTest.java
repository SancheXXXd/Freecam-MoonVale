package net.xolt.freecam.util;

import java.util.Random;

public final class SweptPointTest {
   private static int checks;

   public static void main(String[] args) {
      equal(hit(0, 0.5, 0.5, 2, 0, 0), 0.5, "forward wall");
      equal(hit(3, 0.5, 0.5, -4, 0, 0), 0.25, "backward wall");
      equal(hit(0, 0.5, 0.5, 100, 0, 0), 0.01, "high speed tunnelling");
      equal(hit(0, 0, 0.5, 3, 1, 0), 1.0 / 3.0, "diagonal wall");
      absent(hit(0, 2, 0.5, 5, 0, 0), "parallel outside");
      absent(hit(0, 0, 0.5, 5, 0, 0), "tangent surface");
      absent(hit(1, 0.5, 0.5, -1, 0, 0), "moving away from contact");
      equal(hit(1, 0.5, 0.5, 1, 0, 0), 0, "moving into contact");
      equal(hit(1.5, 0.5, 0.5, 10, 0, 0), 0, "embedded start");
      absent(hit(0, 0.5, 0.5, -5, 0, 0), "moving away from wall");
      absent(hit(0, 0.5, 0.5, 0, 0, 0), "stationary clear");
      equal(SweptPoint.firstHit(0, 0, 0, 0, 10, 0, -1, 2, -1, 1, 3, 1), 0.2, "ceiling");
      equal(SweptPoint.firstHit(0, 0, 0, 0, -10, 0, -1, -3, -1, 1, -2, 1), 0.2, "floor");
      equal(SweptPoint.firstHit(0, 0, 0, -10, 0, 0, -3, -1, -1, -2, 1, 1), 0.2, "negative coordinates");
      equal(SweptPoint.firstHit(0, 0, 0, 15, 0, 0, 1, -1, -1, 1.0625, 1, 1), 1.0 / 15.0, "thin wall");
      Random random = new Random(1426L);
      for (int i = 0; i < 10000; i++) {
         double x = -1.0 - random.nextDouble() * 10.0;
         double speed = 2.0 - x + random.nextDouble() * 50.0;
         double t = hit(x, 0.1 + random.nextDouble() * 0.8, 0.5, speed, 0, 0);
         equal(t, (1.0 - x) / speed, "random forward sweep");
      }
      System.out.println("Passed " + checks + " swept-collision checks.");
   }

   private static double hit(double x, double y, double z, double dx, double dy, double dz) {
      return SweptPoint.firstHit(x, y, z, dx, dy, dz, 1, 0, 0, 2, 1, 1);
   }

   private static void equal(double actual, double expected, String label) {
      checks++;
      if (!Double.isFinite(actual) || Math.abs(actual - expected) > 1.0E-10) {
         throw new AssertionError(label + ": expected " + expected + ", got " + actual);
      }
   }

   private static void absent(double actual, String label) {
      checks++;
      if (actual != Double.POSITIVE_INFINITY) {
         throw new AssertionError(label + ": unexpected collision " + actual);
      }
   }
}
