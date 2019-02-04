package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * A mirrored implementation of Bukkit's config.yml loading, defaults, and saving system.
 * <p>
 * Uses a plugin's file location to create and edit YAML configuration files, with the ability to
 * store defaults in the plugin jar's home directory.
 * <p>
 * When using a subdirectory, that subdirectory will apply both for finding the source file in the
 * jar as well as where it is stored in the plugin's data folder.
 */

public class YamlConfigAccessor {

  private final String subdirectory;
  private final String fileName;
  private final Module module;
  private File configFile;
  private FileConfiguration fileConfiguration;

  /**
   * Class constructor.
   *
   * @param module The module this file is for.
   * @param fileName The simple name of the file.
   * @param subdirectory The path to the sub-directory within the plugin's default data folder to
   *        store the file in, if any. Can be <code>null</code> or an empty string to create the
   *        file directly within the plugin's data folder. Ex:
   *        <code>"/buildings/lobbies/spawn"</code>.
   *
   * @throws IllegalArgumentException if the plugin is <code>null</code> or not yet initialized.
   * @throws IllegalStateException if unable to get the data folder from the plugin.
   */
  public YamlConfigAccessor(Module module, String fileName, String subdirectory)
          throws IllegalArgumentException, IllegalStateException {

    if (module == null)
      throw new IllegalArgumentException("module cannot be null");

    if (fileName == null || fileName.equals(""))
      throw new IllegalArgumentException("fileName cannot be null or" + " empty");

    this.module = module;
    this.fileName = fileName;

    File dataFolder = module.getDataFolder();
    if (dataFolder == null)
      throw new IllegalStateException();

    if (subdirectory != null && !subdirectory.equals("")) {
      subdirectory.replace("/", "");
      subdirectory.replace("\\", "");
      subdirectory = subdirectory + "/";
      this.subdirectory = subdirectory;

      dataFolder = new File(dataFolder, subdirectory);
      if (!dataFolder.exists())
        dataFolder.mkdirs();
    } else
      this.subdirectory = null;

    this.configFile = new File(dataFolder, fileName);

    saveDefaultConfig();
  }

  /**
   * Discards any data in memory and reloads the configuration from disk.
   */
  public void reloadConfig() {
    fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

    // Looks for defaults in the jar
    File f = new File("modules", getFileName());

    if (f.exists()) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(f);
      fileConfiguration.setDefaults(defConfig);
    }
  }

  /**
   * Gets a <code>FileConfiguration</code> for this file.
   * <p>
   * If there is a default yml configuration file embedded in this plugin, it will be provided as a
   * default for this Configuration.
   *
   * @return The configuration.
   */
  public FileConfiguration getConfig() {
    if (fileConfiguration == null) {
      this.reloadConfig();
    }
    return fileConfiguration;
  }

  /**
   * Saves the <code>FileConfiguration</code> retrievable by {@link #getConfig()}.
   */
  public void saveConfig() {
    if (fileConfiguration == null || configFile == null) {
      return;
    } else {
      try {
        if(configFile.exists())
          configFile.delete();
        getConfig().save(configFile);
      } catch (IOException ex) {
        Characters.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
      }
    }
  }

  /**
   * Saves the raw contents of the default yml configuration file to the location retrievable by
   * {@link #getConfig()}. If there is no matching default yml configuration file embedded in the
   * plugin, an empty file is saved. This should fail silently if the file already exists.
   */
  public void saveDefaultConfig() {
    if (!configFile.exists()) {
      try {
        Files.copy(
                getClass().getResourceAsStream("/modules/" + getFileName()),
                Paths.get(configFile.toURI()),
                StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception e) { }
    }
  }

  /**
   * Gets the name of the file this is accessing.
   *
   * @return The simple file name, useful for logging and other purposes.
   */
  public final String getFileName() {
    if(subdirectory == null)
      return module.getName() + "/" + fileName;
    return module.getName() + "/" + subdirectory + fileName;
  }
}