package net.xolt.freecam.config.gui;

import java.util.stream.Stream;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.LongSliderEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.KeyCodeBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.config.controller.ConfigController;
import net.xolt.freecam.config.model.FlightMode;
import net.xolt.freecam.config.model.ModConfigDTO;
import net.xolt.freecam.config.model.Perspective;

public class ModConfigScreenFactory {
   private static final double SLIDER_SCALE = 100.0;
   private static final double MIN_SPEED = 0.0;
   private static final double MAX_SPEED = 10.0;
   private final ConfigController<ModConfigDTO> controller;

   public ModConfigScreenFactory(ConfigController<ModConfigDTO> controller) {
      this.controller = controller;
   }

   public Screen getConfigScreen(Screen parent) {
      return this.builder().setParentScreen(parent).build();
   }

   private ModConfigDTO config() {
      return this.controller.getConfig();
   }

   private ModConfigDTO defaults() {
      return this.controller.getDefaults();
   }

   private ConfigBuilder builder() {
      ConfigBuilder builder = ConfigBuilder.create()
         .setTitle(Component.translatable("moonvalefreecam.config.gui.title"))
         .transparentBackground()
         .setSavingRunnable(this.controller::save);
      ConfigEntryBuilder entries = builder.entryBuilder();
      ConfigCategory category = builder.getOrCreateCategory(Component.empty());
      Stream.of(
            controlsCategory(entries),
            this.movementCategory(entries),
            this.visualCategory(entries),
            this.utilityCategory(entries),
            this.notificationCategory(entries),
            this.discordCategory(entries)
         )
         .forEach(category::addEntry);
      return builder;
   }

   private static SubCategoryListEntry controlsCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.controls.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.controls.tooltip")});
      ModBindings.stream()
         .map(bind -> entries.fillKeybindingField(Component.translatable(bind.getName()), bind))
         .map(KeyCodeBuilder::build)
         .forEach(builder::add);
      return builder.build();
   }

   private SubCategoryListEntry movementCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.movement.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.movement.tooltip")});
      EnumListEntry<FlightMode> flightMode = entries.startEnumSelector(
            Component.translatable("moonvalefreecam.config.movement.flightMode.label"), FlightMode.class, this.config().movement.flightMode
         )
         .setEnumNameProvider(value -> Component.translatable("moonvalefreecam.config.movement.flightMode." + value.name()))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.movement.flightMode.tooltip")})
         .setDefaultValue(this.defaults().movement.flightMode)
         .setSaveConsumer(value -> this.config().movement.flightMode = value)
         .build();
      LongSliderEntry horizontalSpeed = entries.startLongSlider(
            Component.translatable("moonvalefreecam.config.movement.speed.horizontal.label"),
            toSlider(this.config().movement.horizontalSpeed),
            toSlider(0.0),
            toSlider(10.0)
         )
         .setDefaultValue(() -> toSlider(this.defaults().movement.horizontalSpeed))
         .setSaveConsumer(value -> this.config().movement.horizontalSpeed = fromSlider(value))
         .setTextGetter(value -> Component.translatable("moonvalefreecam.config.movement.speed.value", new Object[]{fromSlider(value)}))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.movement.speed.horizontal.tooltip")})
         .build();
      LongSliderEntry verticalSpeed = entries.startLongSlider(
            Component.translatable("moonvalefreecam.config.movement.speed.vertical.label"),
            toSlider(this.config().movement.verticalSpeed),
            toSlider(0.0),
            toSlider(10.0)
         )
         .setDefaultValue(() -> toSlider(this.defaults().movement.verticalSpeed))
         .setSaveConsumer(value -> this.config().movement.verticalSpeed = fromSlider(value))
         .setTextGetter(value -> Component.translatable("moonvalefreecam.config.movement.speed.value", new Object[]{fromSlider(value)}))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.movement.speed.vertical.tooltip")})
         .build();
      Stream.of(flightMode, horizontalSpeed, verticalSpeed).forEach(builder::add);
      return builder.build();
   }

   private SubCategoryListEntry visualCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.visual.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.visual.tooltip")});
      EnumListEntry<Perspective> perspective = entries.startEnumSelector(
            Component.translatable("moonvalefreecam.config.visual.perspective.label"), Perspective.class, this.config().visual.perspective
         )
         .setEnumNameProvider(value -> Component.translatable("moonvalefreecam.config.visual.perspective." + value.name()))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.visual.perspective.tooltip")})
         .setDefaultValue(this.defaults().visual.perspective)
         .setSaveConsumer(value -> this.config().visual.perspective = value)
         .build();
      BooleanListEntry showPlayer = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.visual.showPlayer.label"), this.config().visual.showPlayer
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.visual.showPlayer.tooltip")})
         .setDefaultValue(this.defaults().visual.showPlayer)
         .setSaveConsumer(value -> this.config().visual.showPlayer = value)
         .build();
      BooleanListEntry showHand = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.visual.showHand.label"), this.config().visual.showHand
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.visual.showHand.tooltip")})
         .setDefaultValue(this.defaults().visual.showHand)
         .setSaveConsumer(value -> this.config().visual.showHand = value)
         .build();
      BooleanListEntry fullBright = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.visual.fullBright.label"), this.config().visual.fullBright
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.visual.fullBright.tooltip")})
         .setDefaultValue(this.defaults().visual.fullBright)
         .setSaveConsumer(value -> this.config().visual.fullBright = value)
         .build();
      Stream.of(perspective, showPlayer, showHand, fullBright).forEach(builder::add);
      return builder.build();
   }

   private SubCategoryListEntry utilityCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.utility.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.utility.tooltip")});
      BooleanListEntry disableOnDamage = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.utility.disableOnDamage.label"), this.config().utility.disableOnDamage
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.utility.disableOnDamage.tooltip")})
         .setDefaultValue(this.defaults().utility.disableOnDamage)
         .setSaveConsumer(value -> this.config().utility.disableOnDamage = value)
         .build();
      builder.add(disableOnDamage);
      return builder.build();
   }

   private SubCategoryListEntry notificationCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.notification.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.notification.tooltip")});
      BooleanListEntry notifyFreecam = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.notification.freecam.label"), this.config().notification.notifyFreecam
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.notification.freecam.tooltip")})
         .setDefaultValue(this.defaults().notification.notifyFreecam)
         .setSaveConsumer(value -> this.config().notification.notifyFreecam = value)
         .build();
      BooleanListEntry notifyTripod = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.notification.tripod.label"), this.config().notification.notifyTripod
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.notification.tripod.tooltip")})
         .setDefaultValue(this.defaults().notification.notifyTripod)
         .setSaveConsumer(value -> this.config().notification.notifyTripod = value)
         .build();
      Stream.of(notifyFreecam, notifyTripod).forEach(builder::add);
      return builder.build();
   }

   private SubCategoryListEntry discordCategory(ConfigEntryBuilder entries) {
      SubCategoryBuilder builder = entries.startSubCategory(Component.translatable("moonvalefreecam.config.discord.label"))
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.discord.tooltip")});
      BooleanListEntry enabled = entries.startBooleanToggle(
            Component.translatable("moonvalefreecam.config.discord.enabled.label"), this.config().discord.enabled
         )
         .setTooltip(new Component[]{Component.translatable("moonvalefreecam.config.discord.enabled.tooltip")})
         .setDefaultValue(this.defaults().discord.enabled)
         .setSaveConsumer(value -> this.config().discord.enabled = value)
         .build();
      builder.add(enabled);
      return builder.build();
   }

   private static long toSlider(double value) {
      return (long)(value * 100.0);
   }

   private static double fromSlider(long value) {
      return value / 100.0;
   }
}
