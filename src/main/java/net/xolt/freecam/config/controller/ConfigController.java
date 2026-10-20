package net.xolt.freecam.config.controller;

public interface ConfigController<T> {
   T getConfig();

   T getDefaults();

   void load();

   void save();

   void registerListener(Runnable var1);
}
