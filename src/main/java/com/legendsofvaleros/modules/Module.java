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
        return dataFolder;
    }

    public YamlConfiguration getConfig() {
        if(config == null) {
            config = new YamlConfiguration();

            try {
                config.load(new File(getDataFolder(), "config.yml"));
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