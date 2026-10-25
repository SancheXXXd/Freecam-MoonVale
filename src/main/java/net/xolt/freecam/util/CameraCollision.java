package net.xolt.freecam.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CameraCollision {
   public static final double EYE_RADIUS = 0.20;
   private static final double SKIN = 1.0E-5;

   private CameraCollision() {
   }

   public static boolean isClear(ClientLevel level, Vec3 eye, double radius) {
      if (!valid(eye) || !Double.isFinite(radius) || radius <= 0.0 || radius > 1.0) {
         return false;
      }
      AABB box = bounds(eye, radius);
      return loaded(level, box) && level.noBlockCollision(null, box);
   }

   public static Vec3 clip(ClientLevel level, Vec3 start, Vec3 target, double radius) {
      if (!isClear(level, start, radius) || !valid(target)) {
         return start;
      }
      Vec3 delta = target.subtract(start);
      AABB swept = bounds(start, radius).expandTowards(delta);
      if (!loaded(level, swept)) {
         return start;
      }
      double first = Double.POSITIVE_INFINITY;
      for (VoxelShape shape : level.getBlockCollisions(null, swept.inflate(SKIN))) {
         for (AABB obstacle : shape.toAabbs()) {
            first = Math.min(first, SweptPoint.firstHit(start.x, start.y, start.z,
               delta.x, delta.y, delta.z,
               obstacle.minX - radius, obstacle.minY - radius, obstacle.minZ - radius,
               obstacle.maxX + radius, obstacle.maxY + radius, obstacle.maxZ + radius));
         }
      }
      if (!Double.isFinite(first)) {
         return isClear(level, target, radius) ? target : start;
      }
      double length = delta.length();
      double fraction = Math.max(0.0, first - SKIN / Math.max(length, SKIN));
      Vec3 result = start.add(delta.scale(fraction));
      return isClear(level, result, radius) ? result : start;
   }

   public static double renderRadius(float fov, double aspect) {
      if (!Float.isFinite(fov) || !Double.isFinite(aspect) || fov <= 0.0F || fov >= 179.0F || aspect <= 0.0) {
         return Double.POSITIVE_INFINITY;
      }
      double halfHeight = 0.05 * Math.tan(Math.toRadians(fov) * 0.5);
      return Math.max(EYE_RADIUS, Math.sqrt(0.05 * 0.05 + halfHeight * halfHeight * (1.0 + aspect * aspect)) + 0.02);
   }

   private static boolean valid(Vec3 point) {
      return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z)
         && Math.abs(point.x) < 3.0E7 && Math.abs(point.z) < 3.0E7 && Math.abs(point.y) < 3.0E7;
   }

   private static AABB bounds(Vec3 point, double radius) {
      return new AABB(point.x - radius, point.y - radius, point.z - radius,
                      point.x + radius, point.y + radius, point.z + radius);
   }

   private static boolean loaded(ClientLevel level, AABB box) {
      int minX = (int)Math.floor(box.minX) >> 4;
      int maxX = (int)Math.floor(box.maxX) >> 4;
      int minZ = (int)Math.floor(box.minZ) >> 4;
      int maxZ = (int)Math.floor(box.maxZ) >> 4;
      if (maxX - minX > 8 || maxZ - minZ > 8) {
         return false;
      }
      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            if (!level.getChunkSource().hasChunk(x, z)) {
               return false;
            }
         }
      }
      return true;
   }
}
