package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.config.YamlConfigAccessor;
import com.legendsofvaleros.scheduler.InternalScheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.logging.Logger;

public abstract class Module {
    public String moduleName;
    public String getName() {
        return moduleName;
    }

    private Logger logger;

    private File dataFolder;
    private YamlConfigAccessor configAccessor;

    private ModuleEventTimings timings;
    public ModuleEventTimings getTimings() { return timings; }

    public void onLoad() {
        timings = new ModuleEventTimings(this);
    }

    public void onUnload() {
        timings.onUnload();
    }

    public void registerEvents(Listener listener) {
        LegendsOfValeros.getInstance().registerEvents(listener, this);
    }

    public File getDataFolder() {
        if(dataFolder == null)
            dataFolder = new File(LegendsOfValeros.getInstance().getDataFolder(), this.getName());

        dataFolder.mkdirs();

        return dataFolder;
    }

    public FileConfiguration getConfig() {
        if(configAccessor == null)
            configAccessor = new YamlConfigAccessor(this, "config.yml", null);
        return configAccessor.getConfig();
    }

    public void reloadConfig() {
        configAccessor.reloadConfig();
    }

    public Logger getLogger() {
        if(logger == null) {
            logger = new InternalLogger(this);
            logger.setParent(LegendsOfValeros.getInstance().getLogger());
        }

        return logger;
    }

    /**
     * Gets the scheduler for the Module
     */
    public InternalScheduler getScheduler() {
        return Modules.getScheduler(this);
    }
}