package com.legendsofvaleros.modules;

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
        modulesToLoad.forEach(moduleToLoad ->
        {
            System.out.println("[" + moduleToLoad.getClass().getSimpleName() + "] Loading module...");

            modules.put(moduleToLoad.getClass().getSimpleName(), moduleToLoad);
            schedulers.put(moduleToLoad.getClass().getSimpleName(), new InternalScheduler(moduleToLoad.getClass().getSimpleName()).startup());
            moduleToLoad.onLoad();

            System.out.println("[" + moduleToLoad.getClass().getSimpleName() + "] Has been loaded");
        });
        modulesToLoad.clear();
    }

    public static void unloadModules() {
        modules.values().forEach(module ->
        {
            System.out.println("[" + module.getClass().getSimpleName() + "] Unloading module ...");
            modulesToUnload.add(module);
        });
        modulesToUnload.forEach(moduleToUnload ->
        {
            System.out.println("[" + moduleToUnload.getClass().getSimpleName() + "] has been unloaded");
            moduleToUnload.onUnload();

            //stopping the modules thread
            try {
                InternalScheduler scheduler = moduleToUnload.getScheduler();
                if (scheduler.isAlive()) {
                    scheduler.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        modulesToUnload.clear();
    }

    public static void registerModule(Module module) {
        modulesToLoad.add(module);
    }
}