package net.xolt.freecam.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamPosition {
   public double x;
   public double y;
   public double z;
   public float pitch;
   public float yaw;
   private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   private final Vector3f verticalPlane = new Vector3f(0.0F, 1.0F, 0.0F);
   private final Vector3f diagonalPlane = new Vector3f(1.0F, 0.0F, 0.0F);
   private final Vector3f horizontalPlane = new Vector3f(0.0F, 0.0F, 1.0F);

   public FreecamPosition(Entity entity) {
      this.x = entity.getX();
      this.y = getSwimmingY(entity);
      this.z = entity.getZ();
      this.setRotation(entity.getYRot(), entity.getXRot());
   }

   public void setRotation(float yaw, float pitch) {
      this.pitch = pitch;
      this.yaw = yaw;
      this.rotation.rotationYXZ(-yaw * (float) (Math.PI / 180.0), pitch * (float) (Math.PI / 180.0), 0.0F);
      this.horizontalPlane.set(0.0F, 0.0F, 1.0F);
      this.verticalPlane.set(0.0F, 1.0F, 0.0F);
      this.diagonalPlane.set(1.0F, 0.0F, 0.0F);
      this.horizontalPlane.rotate(this.rotation);
      this.verticalPlane.rotate(this.rotation);
      this.diagonalPlane.rotate(this.rotation);
   }

   public void mirrorRotation() {
      this.setRotation(this.yaw + 180.0F, -this.pitch);
   }

   public void moveForward(double distance) {
      this.move(distance, 0.0, 0.0);
   }

   public void move(double fwd, double up, double right) {
      this.x = this.x + (this.horizontalPlane.x() * fwd + this.verticalPlane.x() * up + this.diagonalPlane.x() * right);
      this.y = this.y + (this.horizontalPlane.y() * fwd + this.verticalPlane.y() * up + this.diagonalPlane.y() * right);
      this.z = this.z + (this.horizontalPlane.z() * fwd + this.verticalPlane.z() * up + this.diagonalPlane.z() * right);
   }

   public ChunkPos getChunkPos() {
      return new ChunkPos((int)(this.x / 16.0), (int)(this.z / 16.0));
   }

   private static double getSwimmingY(Entity entity) {
      return entity.getPose() == Pose.SWIMMING ? entity.getY() : entity.getY() - entity.getEyeHeight(Pose.SWIMMING) + entity.getEyeHeight(entity.getPose());
   }
}
