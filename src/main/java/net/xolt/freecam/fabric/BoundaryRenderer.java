package net.xolt.freecam.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.util.FreeCamera;

public final class BoundaryRenderer {
   private static final double LIMIT = 32.0;
   private static final double SOFT_START = 26.0;
   private static final double SOFT_WIDTH = 6.0;
   private static final double VIEW_RADIUS = 10.0;
   private static final double GRID_STEP = 2.0;
   private static final double SEGMENT = 1.0;
   private static final double NEAR_FADE_START = 0.15;
   private static final double NEAR_FADE_WIDTH = 0.6;
   private static final float LINE_WIDTH = 2.5F;

   private BoundaryRenderer() {
   }

   public static void submit(SubmitNodeCollector var0, CameraRenderState var1) {
      if (Freecam.isEnabled() && Freecam.MC.player != null && var1.pos != null) {
         FreeCamera var2 = Freecam.getFreeCamera();
         if (var2 != null) {
            float var3 = var1.cameraEntityPartialTicks;
            Vec3 var4 = Freecam.MC.player.getEyePosition(var3).add(0.0, var2.getEyeHeight(), 0.0);
            Vec3 var5 = var1.pos;
            double var6 = var5.x - var4.x;
            double var8 = var5.y - var4.y;
            double var10 = var5.z - var4.z;
            if (!(pressure(Math.max(Math.abs(var6), Math.max(Math.abs(var8), Math.abs(var10)))) < 0.01)) {
               long var12 = Freecam.MC.level == null ? 0L : Freecam.MC.level.getGameTime();
               float var14 = ((float)var12 + var3) * 0.08F;
               PoseStack var15 = new PoseStack();
               var15.translate(-var6, -var8, -var10);
               var0.submitCustomGeometry(var15, RenderTypes.linesTranslucent(), (var7, var8x) -> drawFaces(var7, var8x, var6, var8, var10, var14));
            }
         }
      }
   }

   private static void drawFaces(Pose var0, VertexConsumer var1, double var2, double var4, double var6, float var8) {
      double[] var9 = new double[]{var2, var4, var6};

      for (int var10 = 0; var10 < 3; var10++) {
         for (byte var11 = -1; var11 <= 1; var11 += 2) {
            float var12 = (float)pressure(var11 * var9[var10]);
            if (var12 >= 0.01F) {
               drawFace(var0, var1, var9, var10, var11, var12, var8);
            }
         }
      }
   }

   private static void drawFace(Pose var0, VertexConsumer var1, double[] var2, int var3, int var4, float var5, float var6) {
      int var7 = (var3 + 1) % 3;
      int var8 = (var3 + 2) % 3;
      double var9 = clamp(var2[var7], -32.0, 32.0);
      double var11 = clamp(var2[var8], -32.0, 32.0);
      double var13 = var4 * 32.0;
      drawGrid(var0, var1, var2, var3, var7, var8, var9, var11, var13, var5, var6);
      drawGrid(var0, var1, var2, var3, var8, var7, var11, var9, var13, var5, var6);
   }

   private static void drawGrid(
      Pose var0, VertexConsumer var1, double[] var2, int var3, int var4, int var5, double var6, double var8, double var10, float var12, float var13
   ) {
      double var14 = Math.max(-32.0, Math.ceil((var6 - 10.0) / 2.0) * 2.0);
      double var16 = Math.min(32.0, Math.floor((var6 + 10.0) / 2.0) * 2.0);

      for (double var18 = var14; var18 <= var16 + 1.0E-6; var18 += 2.0) {
         double var20 = Math.abs(var18 - var6);
         double var22 = Math.sqrt(Math.max(0.0, 100.0 - var20 * var20));
         if (!(var22 <= 0.0)) {
            double var24 = Math.max(-32.0, var8 - var22);
            double var26 = Math.min(32.0, var8 + var22);
            double var28 = var24;

            while (var28 < var26 - 1.0E-6) {
               double var30 = Math.min(var26, Math.floor(var28 / 1.0 + 1.0E-6) * 1.0 + 1.0);
               segment(var0, var1, var2, var3, var4, var5, var10, var18, var28, var30, var6, var8, var12, var13);
               var28 = var30;
            }
         }
      }
   }

   private static void segment(
      Pose var0,
      VertexConsumer var1,
      double[] var2,
      int var3,
      int var4,
      int var5,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      float var18,
      float var19
   ) {
      double[] var20 = new double[3];
      double[] var21 = new double[3];
      var20[var3] = var6;
      var21[var3] = var6;
      var20[var4] = var8;
      var21[var4] = var8;
      var20[var5] = var10;
      var21[var5] = var12;
      int var22 = color(var20, var2, var8 - var14, var10 - var16, var18, var19);
      int var23 = color(var21, var2, var8 - var14, var12 - var16, var18, var19);
      if (var22 >>> 24 >= 2 || var23 >>> 24 >= 2) {
         float var24 = var5 == 0 ? 1.0F : 0.0F;
         float var25 = var5 == 1 ? 1.0F : 0.0F;
         float var26 = var5 == 2 ? 1.0F : 0.0F;
         var1.addVertex(var0, (float)var20[0], (float)var20[1], (float)var20[2]).setColor(var22).setNormal(var0, var24, var25, var26).setLineWidth(2.5F);
         var1.addVertex(var0, (float)var21[0], (float)var21[1], (float)var21[2]).setColor(var23).setNormal(var0, var24, var25, var26).setLineWidth(2.5F);
      }
   }

   private static int color(double[] var0, double[] var1, double var2, double var4, float var6, float var7) {
      double var8 = Math.sqrt(var2 * var2 + var4 * var4);
      double var10 = 1.0 - smooth(var8 / 10.0);
      double var12 = var0[0] - var1[0];
      double var14 = var0[1] - var1[1];
      double var16 = var0[2] - var1[2];
      double var18 = smooth((Math.sqrt(var12 * var12 + var14 * var14 + var16 * var16) - 0.15) / 0.6);
      float var20 = (float)(0.5 + 0.5 * Math.cos(var8 * 0.7 - var7));
      float var21 = 0.35F + 0.65F * var20;
      float var22 = (float)(var6 * var10 * var18) * (0.55F + 0.45F * var20) * 0.9F;
      int var23 = Math.round(clamp(var22, 0.0, 1.0) * 255.0F);
      int var24 = mix(57, 169, var21);
      int var25 = mix(125, 208, var21);
      int var26 = mix(236, 252, var21);
      return var23 << 24 | var24 << 16 | var25 << 8 | var26;
   }

   private static double pressure(double var0) {
      return smooth((var0 - 26.0) / 6.0);
   }

   private static double smooth(double var0) {
      var0 = clamp(var0, 0.0, 1.0);
      return var0 * var0 * (3.0 - 2.0 * var0);
   }

   private static float clamp(float var0, double var1, double var3) {
      return (float)Math.max(var1, Math.min(var3, (double)var0));
   }

   private static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }

   private static int mix(int var0, int var1, float var2) {
      return Math.round(var0 + (var1 - var0) * var2);
   }
}
