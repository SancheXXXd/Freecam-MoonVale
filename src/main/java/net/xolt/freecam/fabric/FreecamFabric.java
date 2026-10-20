package net.xolt.freecam.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.rpc.DiscordRpc;

public class FreecamFabric implements ClientModInitializer {
   public void onInitializeClient() {
      ModConfig.setup();
      ModBindings.forEach(KeyMappingHelper::registerKeyMapping);
      ClientTickEvents.START_CLIENT_TICK.register(Freecam::preTick);
      ClientTickEvents.END_CLIENT_TICK.register(Freecam::postTick);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         DiscordRpc.setEnabled(ModConfig.get().isDiscordRpcEnabled());
         DiscordRpc.update(client);
      });
   }
}
