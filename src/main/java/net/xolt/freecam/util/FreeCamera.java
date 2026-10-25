package net.xolt.freecam.util;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity.SwingDescription;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.config.model.Perspective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector3f;

@Internal
@AvailableSince("0.4.0")
public class FreeCamera extends AbstractClientPlayer {
   private static final double BOUNDARY_LIMIT = 32.0;
   private static final double BOUNDARY_SOFT_START = 26.0;
   private static final double BOUNDARY_SOFT_WIDTH = 6.0;
   private static final double BOUNDARY_EPSILON = 1.0E-4;
   private final Vector3f boundaryContactNormal = new Vector3f(1.0F, 0.0F, 0.0F);
   private float boundaryGlow;
   private Vec3 lastSafeEye;
   public ClientInput input;
   public float yBob;
   public float xBob;
   public float yBobO;
   public float xBobO;

   public FreeCamera(int id) {
      super(Freecam.MC.level, new GameProfile(UUID.randomUUID(), "FreeCamera"));
      this.setId(id);
      this.setPose(Pose.SWIMMING);
      this.getAbilities().flying = true;
      this.input = new KeyboardInput(Freecam.MC.options);
   }

   public void tick() {
      if (!this.ensureSafePosition()) {
         return;
      }
      this.noPhysics = false;
      this.input.tick();
      this.doMotion();
      this.applyBoundaryResistance();
      super.tick();
      this.enforcePlayerBoundary();
      this.ensureSafePosition();
      this.updateBoundaryGlow();
   }

   public void copyPosition(@NotNull Entity entity) {
      this.applyPosition(new FreecamPosition(entity));
      this.enforcePlayerBoundary();
   }

   public void applyPosition(FreecamPosition position) {
      Vec3 desiredEye = new Vec3(position.x, position.y + this.getEyeHeight(), position.z);
      if (CameraCollision.isClear(this.getClientLevel(), desiredEye, CameraCollision.EYE_RADIUS)) {
         this.lastSafeEye = desiredEye;
         this.snapTo(position.x, position.y, position.z, position.yaw, position.pitch);
      } else if (this.lastSafeEye != null && CameraCollision.isClear(this.getClientLevel(), this.lastSafeEye, CameraCollision.EYE_RADIUS)) {
         this.snapTo(this.lastSafeEye.x, this.lastSafeEye.y - this.getEyeHeight(), this.lastSafeEye.z, position.yaw, position.pitch);
      } else {
         this.snapTo(position.x, position.y, position.z, position.yaw, position.pitch);
         Freecam.disableNextTick();
      }
      this.setDeltaMovement(Vec3.ZERO);
      this.xBob = this.getXRot();
      this.yBob = this.getYRot();
      this.xBobO = this.xBob;
      this.yBobO = this.yBob;
   }

   @Override
   public void move(MoverType type, Vec3 movement) {
      if (!this.ensureSafePosition()) {
         return;
      }
      Vec3 start = this.getEyePosition();
      this.noPhysics = false;
      super.move(type, movement);
      Vec3 target = this.getEyePosition();
      Vec3 safe = CameraCollision.clip(this.getClientLevel(), start, target, CameraCollision.EYE_RADIUS);
      if (safe.distanceToSqr(target) > 1.0E-16) {
         this.setPos(safe.x, safe.y - this.getEyeHeight(), safe.z);
         this.setDeltaMovement(Vec3.ZERO);
      }
      this.ensureSafePosition();
   }

   private boolean ensureSafePosition() {
      Vec3 eye = this.getEyePosition();
      if (CameraCollision.isClear(this.getClientLevel(), eye, CameraCollision.EYE_RADIUS)) {
         this.lastSafeEye = eye;
         return true;
      }
      if (this.lastSafeEye != null && CameraCollision.isClear(this.getClientLevel(), this.lastSafeEye, CameraCollision.EYE_RADIUS)) {
         this.snapTo(this.lastSafeEye.x, this.lastSafeEye.y - this.getEyeHeight(), this.lastSafeEye.z, this.getYRot(), this.getXRot());
      }
      this.setDeltaMovement(Vec3.ZERO);
      Freecam.disableNextTick();
      return false;
   }

   public void applyPerspective(Perspective perspective, boolean checkCollision) {
      FreecamPosition position = new FreecamPosition(this);
      switch (perspective) {
         case INSIDE:
         default:
            break;
         case FIRST_PERSON:
            this.moveForwardUntilCollision(position, 0.4, checkCollision);
            break;
         case THIRD_PERSON_MIRROR:
            position.mirrorRotation();
         case THIRD_PERSON:
            this.moveForwardUntilCollision(position, -4.0, checkCollision);
      }
   }

   private boolean moveForwardUntilCollision(FreecamPosition position, double distance, boolean checkCollision) {
      Vec3 start = this.getEyePosition();
      position.moveForward(distance);
      Vec3 target = new Vec3(position.x, position.y + this.getEyeHeight(), position.z);
      Vec3 safe = CameraCollision.clip(this.getClientLevel(), start, target, CameraCollision.EYE_RADIUS);
      position.x = safe.x;
      position.y = safe.y - this.getEyeHeight();
      position.z = safe.z;
      this.applyPosition(position);
      return safe.distanceToSqr(start) > 1.0E-16;
   }

   private ClientLevel getClientLevel() {
      return (ClientLevel)this.level();
   }

   public void spawn() {
      this.getClientLevel().addEntity(this);
   }

   public void despawn() {
      this.getClientLevel().removeEntity(this.getId(), RemovalReason.DISCARDED);
   }

   protected void checkFallDamage(double heightDifference, boolean onGround, @NotNull BlockState landedState, @NotNull BlockPos landedPosition) {
   }

   @Nullable
   public SwingDescription getCurrentSwing() {
      return Freecam.MC.player.getCurrentSwing();
   }

   public float getSwingAnimation(float partialTicks) {
      return Freecam.MC.player.getSwingAnimation(partialTicks);
   }

   public boolean isSwinging() {
      return Freecam.MC.player.isSwinging();
   }

   public int getUseItemRemainingTicks() {
      return Freecam.MC.player.getUseItemRemainingTicks();
   }

   public boolean isUsingItem() {
      return Freecam.MC.player.isUsingItem();
   }

   public boolean onClimbable() {
      return false;
   }

   public boolean isInWater() {
      return false;
   }

   public MobEffectInstance getEffect(@NotNull Holder<MobEffect> effect) {
      return Freecam.MC.player.getEffect(effect);
   }

   @NotNull
   public PushReaction getPistonPushReaction() {
      return ModConfig.get().ignoreAllCollision() ? PushReaction.IGNORE_ENTITY : super.getPistonPushReaction();
   }

   public boolean canCollideWith(@NotNull Entity other) {
      return false;
   }

   public void setPose(@NotNull Pose pose) {
      super.setPose(Pose.SWIMMING);
   }

   protected boolean updateIsUnderwater() {
      this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
      return this.wasUnderwater;
   }

   protected void doWaterSplashEffect() {
   }

   private void doMotion() {
      switch (ModConfig.get().getFlightMode()) {
         case DEFAULT:
            this.getAbilities().setFlyingSpeed(0.0F);
            Motion.doMotion(this, ModConfig.get().getHorizontalSpeed(), ModConfig.get().getVerticalSpeed());
            break;
         case CREATIVE:
            this.getAbilities().setFlyingSpeed((float)ModConfig.get().getVerticalSpeed() / 10.0F);
            if (this.input.keyPresses.shift() ^ this.input.keyPresses.jump()) {
               int direction = this.input.keyPresses.jump() ? 1 : -1;
               this.setDeltaMovement(this.getDeltaMovement().add(0.0, direction * this.getAbilities().getFlyingSpeed() * 3.0F, 0.0));
            }
      }

      this.getAbilities().flying = true;
      this.setOnGround(false);
   }

   private void enforcePlayerBoundary() {
      if (Freecam.MC.player != null) {
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         double clampedX = clamp(x, Freecam.MC.player.getX() - 32.0, Freecam.MC.player.getX() + 32.0);
         double clampedY = clamp(y, Freecam.MC.player.getEyeY() - 32.0, Freecam.MC.player.getEyeY() + 32.0);
         double clampedZ = clamp(z, Freecam.MC.player.getZ() - 32.0, Freecam.MC.player.getZ() + 32.0);
         boolean hitX = clampedX != x;
         boolean hitY = clampedY != y;
         boolean hitZ = clampedZ != z;
         if (hitX || hitY || hitZ) {
            Vec3 velocity = this.getDeltaMovement();
            Vec3 safe = CameraCollision.clip(this.getClientLevel(), this.getEyePosition(),
               new Vec3(clampedX, clampedY + this.getEyeHeight(), clampedZ), CameraCollision.EYE_RADIUS);
            this.setPos(safe.x, safe.y - this.getEyeHeight(), safe.z);
            if (Math.abs(this.getX() - clampedX) > BOUNDARY_EPSILON
               || Math.abs(this.getY() - clampedY) > BOUNDARY_EPSILON
               || Math.abs(this.getZ() - clampedZ) > BOUNDARY_EPSILON) {
               Freecam.disableNextTick();
            }
            this.setDeltaMovement(hitX ? 0.0 : velocity.x, hitY ? 0.0 : velocity.y, hitZ ? 0.0 : velocity.z);
            this.updateContactNormal(clampedX - Freecam.MC.player.getX(), clampedY - Freecam.MC.player.getEyeY(), clampedZ - Freecam.MC.player.getZ());
         }
      }
   }

   private void updateBoundaryGlow() {
      if (Freecam.MC.player == null) {
         this.boundaryGlow = 0.0F;
      } else {
         double dx = this.getX() - Freecam.MC.player.getX();
         double dy = this.getY() - Freecam.MC.player.getEyeY();
         double dz = this.getZ() - Freecam.MC.player.getZ();
         double axisDistance = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
         float target = (float)clamp((axisDistance - 26.0) / 6.0, 0.0, 1.0);
         this.boundaryGlow = this.boundaryGlow + (target - this.boundaryGlow) * 0.18F;
         this.updateContactNormal(dx, dy, dz);
      }
   }

   private void updateContactNormal(double dx, double dy, double dz) {
      double ax = Math.abs(dx);
      double ay = Math.abs(dy);
      double az = Math.abs(dz);
      if (ax >= ay && ax >= az) {
         this.boundaryContactNormal.set(Math.copySign(1.0F, (float)dx), 0.0F, 0.0F);
      } else if (ay >= az) {
         this.boundaryContactNormal.set(0.0F, Math.copySign(1.0F, (float)dy), 0.0F);
      } else {
         this.boundaryContactNormal.set(0.0F, 0.0F, Math.copySign(1.0F, (float)dz));
      }
   }

   private static double resistAxis(double offset, double velocity) {
      double distance = Math.abs(offset);
      if (distance == 0.0) {
         return velocity;
      } else {
         double direction = Math.copySign(1.0, offset);
         double outward = velocity * direction;
         if (outward <= 0.0) {
            return velocity;
         } else {
            double pressure = clamp((distance - 26.0) / 6.0, 0.0, 1.0);
            double damping = pressure * pressure * (3.0 - 2.0 * pressure);
            double dampedOutward = outward * (1.0 - damping);
            double remainingDistance = Math.max(0.0, 32.0 - distance - 1.0E-4);
            return direction * Math.min(dampedOutward, remainingDistance);
         }
      }
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public float getBoundaryGlow() {
      return this.boundaryGlow;
   }

   public Vector3f getBoundaryContactNormal() {
      return this.boundaryContactNormal;
   }

   public float getViewXRot(float partialTick) {
      return this.getXRot();
   }

   public float getViewYRot(float partialTick) {
      return this.getYRot();
   }

   public boolean isEffectiveAi() {
      return true;
   }

   @NotNull
   public MoveSimulationType getMoveSimulationType() {
      return MoveSimulationType.SERVER_AND_CLIENT;
   }

   protected void applyInput() {
      Vec2 vec2 = this.input.getMoveVector();
      if (vec2.lengthSquared() != 0.0F) {
         vec2 = vec2.scale(0.98F);
      }

      this.applyInputHelper(vec2, this.input.keyPresses.jump());
   }

   private void applyInputHelper(Vec2 moveVector, boolean jumping) {
      this.xxa = moveVector.x;
      this.zza = moveVector.y;
      this.jumping = jumping;
      this.setSprinting(Freecam.MC.options.keySprint.isDown() && this.input.keyPresses.forward() || this.input.keyPresses.forward() && this.isSprinting());
      this.yBobO = this.yBob;
      this.xBobO = this.xBob;
      this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5F;
      this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5F;
   }

   private void applyBoundaryResistance() {
      if (Freecam.MC.player != null) {
         double var1 = this.getX() - Freecam.MC.player.getX();
         double var3 = this.getY() - Freecam.MC.player.getEyeY();
         double var5 = this.getZ() - Freecam.MC.player.getZ();
         Vec3 var7 = this.getDeltaMovement();
         double var8 = pushAxis(var1, resistAxis(var1, var7.x));
         double var10 = pushAxis(var3, resistAxis(var3, var7.y));
         double var12 = pushAxis(var5, resistAxis(var5, var7.z));
         if (var8 != var7.x || var10 != var7.y || var12 != var7.z) {
            this.setDeltaMovement(var8, var10, var12);
         }

         this.updateContactNormal(var1, var3, var5);
      }
   }

   private static double pushAxis(double var0, double var2) {
      double var4 = clamp((Math.abs(var0) - 26.0) / 6.0, 0.0, 1.0);
      if (var4 <= 0.0) {
         return var2;
      } else {
         double var6 = var4 * var4 * (3.0 - 2.0 * var4) * 0.06;
         double var8 = Math.copySign(1.0, var0);
         double var10 = -var2 * var8;
         double var12 = Math.min(var10 + var6, Math.max(var10, var6));
         return -var8 * var12;
      }
   }
}
