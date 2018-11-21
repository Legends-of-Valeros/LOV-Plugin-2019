package com.legendsofvaleros.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.scheduler.InternalScheduler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.logging.Logger;

public class Modules {
    private static Logger getLogger() { return LegendsOfValeros.getInstance().getLogger(); }

    /** All available modules (ignoring "enabled") */
    private static Map<String, Class<? extends Module>> available = new HashMap<>();

    /** List of modules that are enabled. This does not mean that they are loaded. */
    private static Set<Class<? extends Module>> enabled = new HashSet<>();

    /** Module dependency pairs. Pair<optional, module> */
    private static Multimap<Class<? extends Module>, Pair<Boolean, Class<? extends Module>>> dependencies = HashMultimap.create();


    /** The packages that modules reside in. */
    private static Map<String, Class<? extends Module>> packages = new HashMap<>();
    private static Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();
    public static Collection<Module> getLoadedModules() { return modules.values(); }
    public static boolean isEnabled(Class<? extends Module> clazz) { return modules.containsKey(clazz.getSimpleName()); }


    /** Schedulers for each module. */
    private static Map<Class<? extends Module>, InternalScheduler> schedulers = new LinkedHashMap<>();
    public static Collection<InternalScheduler> getSchedulers() { return schedulers.values(); }
    public static InternalScheduler getScheduler(Class<? extends Module> clazz) { return schedulers.get(clazz); }
    public static InternalScheduler getScheduler(Module module) { return schedulers.get(module.getClass()); }

    /**
     * Attempt to load all modules that are marked as "enabled."
     */
    public static void loadModules() {
        // TODO: Check config file to enable/disable modules.
        enabled.addAll(available.values());

        getLogger().info("Attempting to load " + enabled.size() + " enabled modules...");

        int i = 0;

        List<Class<? extends Module>> items = new ArrayList<>(enabled);

        // While we still have modules waiting to be enabled
        while(items.size() > 0) {
            List<Class<? extends Module>> remaining = new ArrayList<>();
            boolean emitted = false;

            // Loop through each waiting module
            mods:
            for(Class<? extends Module> clazz : items) {
                Collection<Pair<Boolean, Class<? extends Module>>> depends = dependencies.get(clazz);

                // If their dependencies aren't loaded, add to remaining then try the next module
                for(Pair<Boolean, Class<? extends Module>> depend : depends) {
                    if(!isDependencyMet(depend)) {
                        remaining.add(clazz);
                        continue mods;
                    }
                }

                emitted = true;

                // Module has all dependencies satisfied. Load it.
                try {
                    getLogger().info("(" + (++i) + "/" + enabled.size() + ") Loading " + clazz.getSimpleName() + "...");

                    loadModule(clazz);
                } catch (Exception e) {
                    getLogger().severe("Failed to load module. Aborting! Offender: " + clazz.getSimpleName());
                }

                getLogger().info("");
            }

            // Unable to load any of the remaining modules. Bail.
            if(!emitted) {
                i++;

                // Output unloaded (enabled) modules and the dependencies that prevented them from loading
                for(Class<? extends Module> clazz : remaining) {
                    List<String> unmet = new ArrayList<>();
                    for(Pair<Boolean, Class<? extends Module>> depend : dependencies.get(clazz)) {
                        if(!isDependencyMet(depend))
                            unmet.add(depend.getRight().getSimpleName());
                    }

                    getLogger().severe(clazz.getSimpleName() + " has unmet dependencies: " + String.join(", ", unmet));
                }

                break;
            }

            items = remaining;
        }

        getLogger().fine("Loaded " + modules.size() + " modules");

        if(enabled.size() > modules.size())
            getLogger().severe("Failed to load " + (enabled.size() - modules.size()) + " modules!");
    }

    /**
     * Verify that a dependency is met for the dependency pair.
     */
    private static boolean isDependencyMet(Pair<Boolean, Class<? extends Module>> depend) {
        // Ignore optional dependencies if they're not enabled.
        if(depend.getLeft() && !enabled.contains(depend.getRight())) return true;

        // If the module is not yet loaded, add it to the remaining modules.
        if(!modules.containsKey(depend.getRight()))
            return false;

        return true;
    }

    /**
     * Attempt to load a module.
     */
    public static void loadModule(Class<? extends Module> clazz) throws IllegalAccessException, InstantiationException {
        if(modules.containsKey(clazz))
            throw new IllegalStateException("Attempted to initialize an already loaded module!");

        Module moduleToLoad = clazz.newInstance();

        modules.put(clazz, moduleToLoad);
        packages.put(getModulePackage(clazz), clazz);

        schedulers.put(clazz, new InternalScheduler(moduleToLoad.getName()).startup());

        moduleToLoad.onLoad();
    }

    /**
     * Unload all loaded modules.
     */
    public static void unloadModules() {
        List<Module> toUnload = new ArrayList<>();

        modules.values().forEach(module -> toUnload.add(module));

        // Unload modules in reverse
        for(int i = toUnload.size() - 1; i >= 0; i--) {
            Module moduleToUnload = modules.get(toUnload.get(i));

            getLogger().info("(" + (i + 1) + "/" + toUnload.size() + ") Unloading " + moduleToUnload.getName() + "...");
            moduleToUnload.onUnload();

            // stopping the modules thread
            try {
                InternalScheduler scheduler = moduleToUnload.getScheduler();
                if(scheduler.isAlive())
                    scheduler.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        enabled.clear();

        modules.clear();
        packages.clear();
        schedulers.clear();
    }

    public static void registerModule(Class<? extends Module> clazz) throws Exception {
        available.put(clazz.getSimpleName(), clazz);

        DependsOn[] depends = clazz.getAnnotationsByType(DependsOn.class);
        for(DependsOn dep : depends)
            dependencies.put(clazz, new ImmutablePair<>(dep.optional(), dep.value()));
    }

    public static String getModulePackage(Class<?> clazz) {
        String pack = clazz.getName();
        String[] ss = pack.split("com\\.legendsofvaleros\\.modules\\.", 2);
        if(ss.length < 2) return null;
        return "com.legendsofvaleros.modules." + ss[1].split("\\.", 2)[0];
    }

    public static Class<? extends Module> getModuleClass(Class<?> clazz) {
        String pack = getModulePackage(clazz);
        if(pack != null && packages.containsKey(pack))
            return packages.get(pack);
        return null;
    }

    public static Module getModule(Class<?> clazz) {
        Class<? extends Module> c = getModuleClass(clazz);
        return c != null ? modules.get(c) : null;
    }
}