package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.scheduler.InternalScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {
    public static ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
    public static List<Module> modulesToLoad = new ArrayList<>();
    public static List<Module> modulesToUnload = new ArrayList<>();

    // scheduler/thread for each module
    public static HashMap<String, InternalScheduler> schedulers = new HashMap<>();

    public static void loadModules() {
        for(int i = 0; i < modulesToLoad.size(); i++) {
            Module moduleToLoad = modulesToLoad.get(i);

            LegendsOfValeros.getInstance().getLogger().info((i + 1) + "/" + modulesToLoad.size() + " Loading " + moduleToLoad.getName() + "...");

            modules.put(moduleToLoad.getName(), moduleToLoad);
            schedulers.put(moduleToLoad.getName(), new InternalScheduler(moduleToLoad.getName()).startup());
            moduleToLoad.onLoad();
        }

        LegendsOfValeros.getInstance().getLogger().info("Loaded " + modules.size() + " modules");

        modulesToLoad.clear();
    }

    public static void unloadModules() {
        modules.values().forEach(module ->
        {
            LegendsOfValeros.getInstance().getLogger().info("Unloading " + module.getName() + "...");
            modulesToUnload.add(module);
        });

        for(int i = 0; i < modulesToUnload.size(); i++) {
            Module moduleToUnload = modulesToUnload.get(i);

            LegendsOfValeros.getInstance().getLogger().info((i + 1) + "/" + modulesToUnload.size() + " Unloading " + moduleToUnload.getName() + "...");
            moduleToUnload.onUnload();

            // stopping the modules thread
            try {
                InternalScheduler scheduler = moduleToUnload.getScheduler();
                if (scheduler.isAlive()) {
                    scheduler.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        modulesToUnload.clear();
    }

    public static void registerModule(Class<? extends Module> clazz) throws Exception {
        // TODO: Add a check to verify that the module should actually be loaded. Perhaps use this method
        // to register possible modules, then a separate loadModules method to determine load order and "enabled"?
        try {
            modulesToLoad.add(clazz.newInstance());
        } catch(Exception e) {
            LegendsOfValeros.getInstance().getLogger().severe("Failed to load module. Aborting! Offender: " + clazz.getSimpleName());
            throw e;
        }
    }
}