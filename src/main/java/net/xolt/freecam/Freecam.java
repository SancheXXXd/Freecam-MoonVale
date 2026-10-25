package net.xolt.freecam;

import java.util.Optional;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ChunkPos;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.config.keys.Tickable;
import net.xolt.freecam.tripod.TripodRegistry;
import net.xolt.freecam.tripod.TripodSlot;
import net.xolt.freecam.util.FreeCamera;
import net.xolt.freecam.util.FreecamPosition;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public class Freecam {
   public static final Minecraft MC = Minecraft.getInstance();
   public static final String MOD_ID = "freecam";
   private static volatile boolean freecamEnabled = false;
   private static volatile boolean tripodEnabled = false;
   private static boolean playerControlEnabled = false;
   private static boolean disableNextTick = false;
   private static boolean outlineEnabled = false;
   private static final TripodRegistry tripods = new TripodRegistry();
   private static TripodSlot activeTripod = TripodSlot.NONE;
   private static FreeCamera freeCamera;
   private static CameraType rememberedF5 = null;

   @Internal
   public static void preTick(Minecraft mc) {
      if ((disableNextTick || isRestrictedOnServer()) && isEnabled()) {
         toggle();
      }

      disableNextTick = false;
      if (isEnabled() && mc.player != null && mc.player.input instanceof KeyboardInput && !isPlayerControlEnabled()) {
         ClientInput input = new ClientInput();
         Input keyPresses = mc.player.input.keyPresses;
         input.keyPresses = new Input(false, false, false, false, false, keyPresses.shift(), false);
         mc.player.input = input;
      }
   }

   @Internal
   public static void postTick(Minecraft mc) {
      ModBindings.forEach(Tickable::tick);
   }

   @Internal
   public static void onDisconnect() {
      if (isEnabled()) {
         toggle();
      }

      tripods.clear();
   }

   @Internal
   public static boolean activateTripodHandler() {
      boolean activated = false;
      KeyMapping[] var1 = MC.options.keyHotbarSlots;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyMapping combo = var1[var3]; combo.consumeClick(); activated = true) {
            toggleTripod(TripodSlot.ofKeyCode(combo.getDefaultKey().getValue()));
         }
      }

      return activated;
   }

   @Internal
   public static boolean resetTripodHandler() {
      boolean reset = false;
      KeyMapping[] var1 = MC.options.keyHotbarSlots;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyMapping key = var1[var3]; key.consumeClick(); reset = true) {
            resetCamera(TripodSlot.ofKeyCode(key.getDefaultKey().getValue()));
         }
      }

      return reset;
   }

   @AvailableSince("0.3.1")
   public static void toggle() {
      if (isRestrictedOnServer()) {
         if (ModConfig.get().shouldNotifyFreecam()) {
            sendOverlayMessage(Component.translatable("moonvalefreecam.msg.restricted.server", new Object[]{MC.getCurrentServer().ip}));
         }
      } else if (tripodEnabled) {
         toggleTripod(activeTripod);
      } else {
         if (freecamEnabled) {
            onDisableFreecam();
         } else {
            onEnableFreecam();
         }

         freecamEnabled = !freecamEnabled;
         if (!freecamEnabled) {
            onDisabled();
         }
      }
   }

   private static void toggleTripod(TripodSlot tripod) {
      if (tripod != TripodSlot.NONE) {
         if (isRestrictedOnServer()) {
            if (ModConfig.get().shouldNotifyTripod()) {
               sendOverlayMessage(Component.translatable("moonvalefreecam.msg.restricted.server", new Object[]{MC.getCurrentServer().ip}));
            }
         } else {
            if (tripodEnabled) {
               if (activeTripod == tripod) {
                  onDisableTripod();
                  tripodEnabled = false;
               } else {
                  onDisableTripod();
                  onEnableTripod(tripod);
               }
            } else {
               if (freecamEnabled) {
                  toggle();
               }

               onEnableTripod(tripod);
               tripodEnabled = true;
            }

            if (!tripodEnabled) {
               onDisabled();
            }
         }
      }
   }

   @AvailableSince("1.1.8")
   public static void switchControls() {
      if (isEnabled()) {
         if (playerControlEnabled) {
            freeCamera.input = new KeyboardInput(MC.options);
         } else {
            MC.player.input = new KeyboardInput(MC.options);
            freeCamera.input = new ClientInput();
         }

         playerControlEnabled = !playerControlEnabled;
      }
   }

   private static void onEnableTripod(TripodSlot tripod) {
      onEnable();
      FreecamPosition position = tripods.get(tripod);
      boolean chunkLoaded = false;
      if (position != null) {
         ChunkPos chunkPos = position.getChunkPos();
         chunkLoaded = MC.level.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z());
      }

      if (!chunkLoaded) {
         resetCamera(tripod);
         position = null;
      }

      freeCamera = new FreeCamera(-420 - tripod.ordinal());
      if (position == null) {
         moveToPlayer();
      } else {
         moveToPosition(position);
      }

      freeCamera.spawn();
      MC.setCameraEntity(freeCamera);
      activeTripod = tripod;
      if (ModConfig.get().shouldNotifyTripod()) {
         sendOverlayMessage(Component.translatable("moonvalefreecam.msg.tripod.open", new Object[]{tripod}));
      }
   }

   private static void onDisableTripod() {
      tripods.put(activeTripod, new FreecamPosition(freeCamera));
      onDisable();
      if (MC.player != null && ModConfig.get().shouldNotifyTripod()) {
         sendOverlayMessage(Component.translatable("moonvalefreecam.msg.tripod.close", new Object[]{activeTripod}));
      }

      activeTripod = TripodSlot.NONE;
   }

   private static void onEnableFreecam() {
      onEnable();
      freeCamera = new FreeCamera(-420);
      moveToPlayer();
      freeCamera.spawn();
      MC.setCameraEntity(freeCamera);
      if (ModConfig.get().shouldNotifyFreecam()) {
         sendOverlayMessage(Component.translatable("moonvalefreecam.msg.enabled"));
      }
   }

   private static void onDisableFreecam() {
      onDisable();
      if (MC.player != null && ModConfig.get().shouldNotifyFreecam()) {
         sendOverlayMessage(Component.translatable("moonvalefreecam.msg.disabled"));
      }
   }

   private static void onEnable() {
      MC.smartCull = true;
      outlineEnabled = ModConfig.get().shouldOutlinePlayer();
      if (!isEnabled()) {
         rememberedF5 = MC.options.getCameraType();
      }

      if (MC.gameRenderer.mainCamera().isDetached()) {
         MC.options.setCameraType(CameraType.FIRST_PERSON);
      }
   }

   private static void onDisable() {
      MC.smartCull = true;
      MC.setCameraEntity(MC.player);
      playerControlEnabled = false;
      freeCamera.despawn();
      freeCamera.input = new ClientInput();
      freeCamera = null;
      if (MC.player != null) {
         MC.player.input = new KeyboardInput(MC.options);
      }
   }

   private static void onDisabled() {
      if (rememberedF5 != null) {
         MC.options.setCameraType(rememberedF5);
      }
   }

   private static void resetCamera(TripodSlot tripod) {
      if (tripodEnabled && activeTripod != TripodSlot.NONE && activeTripod == tripod && freeCamera != null) {
         moveToPlayer();
      } else {
         tripods.put(tripod, null);
      }

      if (ModConfig.get().shouldNotifyTripod()) {
         sendOverlayMessage(Component.translatable("moonvalefreecam.msg.tripod.reset", new Object[]{tripod}));
      }
   }

   private static void sendOverlayMessage(Component message) {
      Optional.ofNullable(MC.player).ifPresent(player -> player.sendOverlayMessage(message));
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToEntity(@Nullable Entity entity) {
      if (freeCamera != null) {
         if (entity == null) {
            moveToPlayer();
         } else {
            freeCamera.copyPosition(entity);
         }
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToPosition(@Nullable FreecamPosition position) {
      if (freeCamera != null) {
         if (position == null) {
            moveToPlayer();
         } else {
            freeCamera.applyPosition(position);
         }
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToPlayer() {
      if (freeCamera != null) {
         freeCamera.copyPosition(MC.player);
         freeCamera.applyPerspective(ModConfig.get().getInitialPerspective(), ModConfig.get().shouldCheckInitialCollision());
      }
   }

   @AvailableSince("0.4.0")
   public static FreeCamera getFreeCamera() {
      return freeCamera;
   }

   @AvailableSince("1.2.3")
   public static void disableNextTick() {
      disableNextTick = true;
   }

   @AvailableSince("0.2.2")
   public static boolean isEnabled() {
      return freecamEnabled || tripodEnabled;
   }

   public static void toggleOutline() {
      if (isEnabled()) {
         outlineEnabled = !outlineEnabled;
      }
   }

   public static boolean isOutlineEnabled() {
      return outlineEnabled;
   }

   @Experimental
   @AvailableSince("1.0.0")
   public static boolean isPlayerControlEnabled() {
      return playerControlEnabled;
   }

   @Experimental
   @AvailableSince("1.2.4")
   public static boolean isRestrictedOnServer() {
      ServerData server = MC.getCurrentServer();
      return server != null && !MC.hasSingleplayerServer() && ModConfig.get().isRestrictedOnServer(server.ip);
   }
}
