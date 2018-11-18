package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.scheduler.InternalScheduler;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class Module {
    private File dataFolder;
    private YamlConfiguration config;

    public void onLoad() {
    }

    public void onUnload() {
    }

    public File getDataFolder() {
        if(dataFolder == null)
            dataFolder = new File(LegendsOfValeros.getInstance().getDataFolder(), this.getClass().getSimpleName());

        dataFolder.mkdirs();

        return dataFolder;
    }

    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    public YamlConfiguration getConfig() {
        if(config == null) {
            config = new YamlConfiguration();

            File configFile = getConfigFile();
            if(!configFile.exists()) {
                // TODO: Create default config file.
            }

            try {
                config.load(getConfigFile());
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        return config;
    }

    /**
     * Gets the scheduler for the Module
     */
    public InternalScheduler getScheduler() {
        return ModuleManager.schedulers.get(this.getClass().getSimpleName());
    }
}