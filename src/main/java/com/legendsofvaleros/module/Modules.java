package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.scheduler.InternalScheduler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Modules {
    private static Logger getLogger() {
        return LegendsOfValeros.getInstance().getLogger();
    }

    /**
     * The packages that modules reside in.
     */
    private static Map<String, Class<? extends Module>> packages = new HashMap<>();
    private static Map<Class<? extends Module>, InternalModule> modules = new LinkedHashMap<>();

    public static void registerModule(Class<? extends Module> clazz) throws Exception {
        modules.put(clazz, new InternalModule(clazz));
        packages.put(getModulePackage(clazz), clazz);
    }

    /**
     * Attempt to load all modules that are marked as "enabled."
     */
    public static void loadModules() {
        // TODO: Check config file to enable/disable modules.
        long enabled = modules.values().stream().filter(InternalModule::isEnabled).count();

        getLogger().info("Attempting to load " + enabled + " enabled modules...");

        int i = 0;

        List<InternalModule> items = modules.values().stream()
                .filter(InternalModule::isEnabled)
                .filter(im -> !im.isLoaded)
                .collect(Collectors.toList());

        // While we still have modules waiting to be enabled
        while (items.size() > 0) {
            // Filter out dependencies that
            List<InternalModule> remaining = new ArrayList<>();
            boolean emitted = false;

            // Loop through each waiting module
            for (InternalModule module : items) {
                // If their dependencies aren't loaded, add to remaining then try the next module
                if (!module.areDependenciesMet()) {
                    remaining.add(module);
                    continue;
                }

                emitted = true;

                // Module has all dependencies satisfied. Load it.
                try {
                    getLogger().info("(" + (++i) + "/" + enabled + ") Loading " + module.getName() + "...");

                    module.load();
                } catch (Exception e) {
                    getLogger().severe("Failed to load module. Aborting! Offender: " + module.getName());
                    e.printStackTrace();
                }

                getLogger().info("");
            }

            // Unable to load any of the remaining modules. Bail.
            if (!emitted) {
                i++;

                // Output unloaded (enabled) modules and the dependencies that prevented them from loading
                for (InternalModule module : remaining) {
                    List<String> unmet = module.dependencies.entrySet().stream()
                            .filter(dep -> isDependencyMet(dep.getKey(), dep.getValue()))
                            .map(dep -> modules.get(dep.getKey()).getName())
                            .collect(Collectors.toList());

                    getLogger().severe(module.getName() + " has unmet dependencies: " + String.join(", ", unmet));
                }

                break;
            }

            items = remaining;
        }

        getLogger().info("Loaded " + modules.size() + " modules");

        if (enabled > modules.size())
            getLogger().severe("Failed to load " + (enabled - modules.size()) + " modules!");

        i = 0;

        // For each loaded module
        for (InternalModule module : modules.values()) {
            if (module.isLoaded)
                i += module.loadIntegrations();
        }

        getLogger().info("Loaded " + i + " integrations");
    }

    @Deprecated
    public static void loadModuleBypass(Class<? extends Module> clazz) throws InstantiationException, IllegalAccessException {
        InternalModule im;

        modules.put(clazz, im = new InternalModule(clazz));
        packages.put(getModulePackage(clazz), clazz);

        im.load();
    }

    /**
     * Unload all loaded modules.
     */
    public static void unloadModules() {
        InternalModule[] toUnload = modules.values().stream()
                .filter(InternalModule::isLoaded)
                .toArray(length -> new InternalModule[length]);

        // Unload modules in reverse
        for (int i = toUnload.length - 1; i >= 0; i--) {
            InternalModule moduleToUnload = toUnload[i];
            getLogger().info("(" + (i + 1) + "/" + toUnload.length + ") Unloading " + moduleToUnload.getName() + "...");
            moduleToUnload.unload();
        }

        modules.clear();
        packages.clear();
    }

    private static boolean isDependencyMet(Class<? extends Module> dependency, boolean optional) {
        // Ignore optional dependencies if they're not enabled.
        if (optional && !modules.get(dependency).isEnabled) return true;

        // If the dependency is enabled, but not yet loaded, then dependencies aren't met
        if (!modules.get(dependency).isLoaded)
            return false;

        return true;
    }

    public static boolean isLoaded(Class<? extends Module> clazz) {
        return modules.get(clazz).isLoaded;
    }

    public static Module[] getLoadedModules() {
        return modules.values().stream()
                .filter(InternalModule::isLoaded)
                .map(InternalModule::getInstance)
                .toArray(length -> new Module[length]);
    }

    public static InternalScheduler[] getSchedulers() {
        return modules.values().stream()
                .map(InternalModule::getScheduler)
                .toArray(length -> new InternalScheduler[length]);
    }

    public static InternalScheduler getScheduler(Class<? extends Module> clazz) {
        return modules.get(clazz).scheduler;
    }

    public static InternalScheduler getScheduler(Module module) {
        return modules.get(module.getClass()).scheduler;
    }

    public static String getModulePackage(Class<?> clazz) {
        String pack = clazz.getName();
        String[] ss = pack.split("com\\.legendsofvaleros\\.modules\\.", 2);
        if (ss.length < 2) return null;
        return "com.legendsofvaleros.modules." + ss[1].split("\\.", 2)[0];
    }

    public static Class<? extends Module> getModuleClass(Class<?> clazz) {
        String pack = getModulePackage(clazz);
        if (pack != null && packages.containsKey(pack))
            return packages.get(pack);
        return null;
    }

    public static Module getModule(Class<?> clazz) {
        Class<? extends Module> c = getModuleClass(clazz);
        return c != null ? modules.get(c).instance : null;
    }

    private static class InternalModule {
        Class<? extends Module> moduleClass;

        boolean isEnabled = true; // If the module should be loaded
        boolean isLoaded = false; // If the module was successfully loaded
        Map<Class<? extends Module>, Boolean> dependencies = new HashMap<>();
        Map<Class<? extends Module>, Class<? extends Integration>> integrationClasses = new HashMap<>();
        Map<Class<? extends Module>, Method> integrationMethods = new HashMap<>();
        Module instance;
        InternalScheduler scheduler;

        private InternalModule(Class<? extends Module> clazz) {
            this.moduleClass = clazz;

            DependsOn[] depends = clazz.getAnnotationsByType(DependsOn.class);
            for (DependsOn dep : depends)
                this.dependencies.put(dep.value(), dep.optional());

            for (IntegratesWith integrate : clazz.getAnnotationsByType(IntegratesWith.class)) {
                if (integrate.integration() == Integration.class) {
                    getLogger().severe("Class-level @IntegratesWith must reference an integration class!");
                    continue;
                }

                this.integrationClasses.put(integrate.module(), integrate.integration());
            }

            for (Method method : clazz.getDeclaredMethods()) {
                IntegratesWith integrate = method.getAnnotation(IntegratesWith.class);
                if (integrate == null) continue;

                if (integrate.integration() != Integration.class) {
                    getLogger().severe("Method-level @IntegratesWith must not reference an integration class!");
                    continue;
                }

                method.setAccessible(true);

                this.integrationMethods.put(integrate.module(), method);
            }
        }

        public String getName() {
            return moduleClass.getSimpleName();
        }

        /**
         * Verify that a dependency is met for the dependency pair.
         */
        private boolean areDependenciesMet() {
            for (Map.Entry<Class<? extends Module>, Boolean> entry : dependencies.entrySet()) {
                if (!isDependencyMet(entry.getKey(), entry.getValue()))
                    return false;
            }

            return true;
        }

        public void load() throws IllegalAccessException, InstantiationException {
            if (isLoaded)
                throw new IllegalStateException("Attempt to load a module that is already loaded!");

            this.instance = moduleClass.newInstance();
            this.scheduler = new InternalScheduler(getName()).startup();
            this.instance.onLoad();

            isLoaded = true;
        }

        public void unload() {
            if (!isLoaded)
                throw new IllegalStateException("Attempt to unload a module that is not loaded!");

            this.instance.onUnload();

            // stopping the modules thread
            try {
                if (this.scheduler.isAlive())
                    this.scheduler.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            isLoaded = false;
        }

        public int loadIntegrations() {
            if (!isLoaded)
                throw new IllegalStateException("Attempt to load integrations from a module that is not loaded!");

            int i = 0;

            // Loop through each of their integrationClasses
            for (Map.Entry<Class<? extends Module>, Class<? extends Integration>> integratesWith
                    : integrationClasses.entrySet()) {

                // If the integration is satisfied, load the class
                if (modules.containsKey(integratesWith.getKey())) {
                    Class<? extends Integration> integrate = integratesWith.getValue();

                    // Verify that the integration class is inside of the module's package.
                    Class<? extends Module> integrateModClass = getModuleClass(integrate);
                    if (integrateModClass != moduleClass)
                        getLogger().warning(getName() + " is loading an integration from " + modules.get(integratesWith.getKey()).getName() + "'s package!");

                    try {
                        // Load the integration
                        integrate.newInstance();
                        i++;
                    } catch (Exception e) {
                        getLogger().severe("Failed to load " + getName() + "'s integration class for " + modules.get(integratesWith.getKey()).getName() + "!");
                        e.printStackTrace();
                    }
                }
            }

            // Loop through each of their integrationMethods
            for (Map.Entry<Class<? extends Module>, Method> integratesWith
                    : integrationMethods.entrySet()) {

                // If the integration is satisfied, invoke the function
                if (modules.containsKey(integratesWith.getKey())) {
                    Method method = integratesWith.getValue();

                    try {
                        // Load the integration
                        method.invoke(instance);
                        i++;
                    } catch (Exception e) {
                        getLogger().severe("Failed to invoke " + getName() + "'s integration function for " + modules.get(integratesWith.getKey()).getName() + "!");
                        e.printStackTrace();
                    }
                }
            }

            return i;
        }

        public boolean isEnabled() {
            return isEnabled;
        }


        public boolean isLoaded() {
            return isLoaded;
        }

        public Module getInstance() {
            return instance;
        }

        public InternalScheduler getScheduler() {
            return scheduler;
        }
    }
}