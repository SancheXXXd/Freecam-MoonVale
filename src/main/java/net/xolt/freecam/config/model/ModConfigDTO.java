package net.xolt.freecam.config.model;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.xolt.freecam.config.load.RawJsonHolder;
import org.jetbrains.annotations.Nullable;

public class ModConfigDTO implements RawJsonHolder {
   @Nullable
   private transient JsonObject rawJson;
   public ModConfigDTO.MovementConfig movement = new ModConfigDTO.MovementConfig();
   public ModConfigDTO.CollisionConfig collision = new ModConfigDTO.CollisionConfig();
   public ModConfigDTO.VisualConfig visual = new ModConfigDTO.VisualConfig();
   public ModConfigDTO.UtilityConfig utility = new ModConfigDTO.UtilityConfig();
   public ModConfigDTO.ServerConfig servers = new ModConfigDTO.ServerConfig();
   public ModConfigDTO.NotificationConfig notification = new ModConfigDTO.NotificationConfig();
   public ModConfigDTO.DiscordConfig discord = new ModConfigDTO.DiscordConfig();

   @Override
   public void setRawJson(@Nullable JsonObject rawJson) {
      this.rawJson = rawJson;
   }

   @Nullable
   @Override
   public JsonObject getRawJson() {
      return this.rawJson;
   }

   public static class CollisionConfig {
      public boolean ignoreTransparent = false;
      public boolean ignoreOpenable = false;
      public boolean ignoreCustom = false;
      public ModConfigDTO.CollisionConfig.CollisionWhitelist whitelist = new ModConfigDTO.CollisionConfig.CollisionWhitelist();
      public boolean ignoreAll = true;
      public boolean alwaysCheck = false;

      public static class CollisionWhitelist {
         public List<String> ids = new ArrayList<>();
         public List<String> patterns = new ArrayList<>();
      }
   }

   public static class DiscordConfig {
      public boolean enabled = true;
   }

   public static enum InteractionMode {
      CAMERA,
      PLAYER;
   }

   public static class MovementConfig {
      public FlightMode flightMode = FlightMode.DEFAULT;
      public double horizontalSpeed = 1.0;
      public double verticalSpeed = 1.0;
   }

   public static class NotificationConfig {
      public boolean notifyFreecam = true;
      public boolean notifyTripod = true;
   }

   public static class ServerConfig {
      public ModConfigDTO.ServerRestriction mode = ModConfigDTO.ServerRestriction.NONE;
      public List<String> whitelist = new ArrayList<>();
      public List<String> blacklist = new ArrayList<>();
   }

   public static enum ServerRestriction {
      NONE,
      WHITELIST,
      BLACKLIST;
   }

   public static class UtilityConfig {
      public boolean disableOnDamage = true;
      public boolean freezePlayer = false;
      public boolean allowInteract = false;
      public ModConfigDTO.InteractionMode interactionMode = ModConfigDTO.InteractionMode.CAMERA;
   }

   public static class VisualConfig {
      public Perspective perspective = Perspective.INSIDE;
      public boolean showPlayer = true;
      public boolean showHand = false;
      public boolean fullBright = false;
      public boolean showSubmersion = false;
      public boolean outlinePlayer = false;
   }
}
