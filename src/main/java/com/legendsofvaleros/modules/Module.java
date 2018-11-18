package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.config.YamlConfigAccessor;
import com.legendsofvaleros.scheduler.InternalScheduler;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public abstract class Module {
    private Logger logger;

    private File dataFolder;
    private YamlConfigAccessor configAccessor;

    public void onLoad() {
    }

    public void onUnload() {
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

    /**
     * Gets the scheduler for the Module
     */
    public InternalScheduler getScheduler() {
        return ModuleManager.schedulers.get(this.getName());
    }

    public void reloadConfig() {
        configAccessor.reloadConfig();
    }

    public Logger getLogger() {
        if(logger == null) {
            logger = Logger.getLogger(this.getName());
            logger.setParent(LegendsOfValeros.getInstance().getLogger());
        }

        return logger;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}