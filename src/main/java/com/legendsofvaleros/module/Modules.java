package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.scheduler.InternalScheduler;

import java.util.*;

public class Modules {
    public static Map<String, String> packages = new HashMap<>();

    public static Map<String, Module> modules = new LinkedHashMap<>();

    public static List<Module> modulesToLoad = new ArrayList<>();
    public static List<Module> modulesToUnload = new ArrayList<>();

    // scheduler/thread for each module
    public static Map<String, InternalScheduler> schedulers = new LinkedHashMap<>();

    public static void loadModules() {
        for(int i = 0; i < modulesToLoad.size(); i++) {
            Module moduleToLoad = modulesToLoad.get(i);
            String pack = getModulePackage(moduleToLoad.getClass());

            LegendsOfValeros.getInstance().getLogger().info("(" + (i + 1) + "/" + modulesToLoad.size() + ") Loading " + moduleToLoad.getName() + "... (" + pack + ")");

            modules.put(moduleToLoad.getName(), moduleToLoad);
            packages.put(pack, moduleToLoad.getName());

            schedulers.put(moduleToLoad.getName(), new InternalScheduler(moduleToLoad.getName()).startup());

            moduleToLoad.onLoad();

            LegendsOfValeros.getInstance().getLogger().info("");
        }

        LegendsOfValeros.getInstance().getLogger().info("Loaded " + modules.size() + " modules");

        modulesToLoad.clear();
    }

    public static void unloadModules() {
        modules.values().forEach(module ->
                modulesToUnload.add(module));

        // Unload modules in reverse
        for(int i = modulesToUnload.size() - 1; i >= 0; i--) {
            Module moduleToUnload = modulesToUnload.get(i);

            LegendsOfValeros.getInstance().getLogger().info("(" + (i + 1) + "/" + modulesToUnload.size() + ") Unloading " + moduleToUnload.getName() + "...");
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

    public static boolean isEnabled(Class<? extends Module> clazz) {
        return true;
    }

    public static String getModulePackage(Class<?> clazz) {
        String pack = clazz.getName();
        String[] ss = pack.split("com\\.legendsofvaleros\\.modules\\.", 2);
        if(ss.length < 2) return null;
        return "com.legendsofvaleros.modules." + ss[1].split("\\.", 2)[0];
    }

    public static String getModuleID(Class<?> clazz) {
        String pack = getModulePackage(clazz);
        if(pack != null && packages.containsKey(pack))
            return packages.get(pack);
        return null;
    }

    public static Module getModule(Class<?> clazz) {
        String id = getModuleID(clazz);
        return id != null ? modules.get(id) : null;
    }
}