package com.legendsofvaleros.modules.characters.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * A general-purpose configuration, intended to be used for secondary configurations, beyond the
 * main <code>config.yml</code> file for a plugin. Allows for a user-defined location and name of
 * the file within the plugin's data folder or a subdirectory therein.
 * <p>
 * Mostly wraps a mirror of Bukkit's implementation for YAML configuration files. Intended for
 * convenience, to exclude the complexity of bukkit's system and make a very easy-to-extend and
 * easy-to-use alternative for a config that is loaded once at startup, stored in memory, parsed in
 * a using private methods in the config class, and accessed per-field by a specific API.
 * 
 */
public abstract class Configuration {

  private final YamlConfigAccessor fileAccessor;

  private FileConfiguration config;

  /**
   * Class constructor that also loads the configuration into memory.
   * 
   * @param fileAccessor The accessor for the file, both within the plugin's home directory and the
   *        data folder of the plugin. Not <code>null</code>.
   * 
   * @throws IllegalArgumentException on a <code>null</code> parameter.
   * @throws IllegalStateException if the source config is incorrectly formatted.
   */
  public Configuration(YamlConfigAccessor fileAccessor) throws IllegalArgumentException,
      IllegalStateException {

    if (fileAccessor == null)
      throw new IllegalArgumentException("params cannot be null");

    this.fileAccessor = fileAccessor;
    init();
  }

  /**
   * Gets the accessor of the source config file for this configuration.
   * 
   * @return The configuration's file accessor.
   */
  protected final YamlConfigAccessor getAccessor() {
    return this.fileAccessor;
  }

  /**
   * Gets the source for the configuration.
   * 
   * @return The configuration's unparsed source.
   */
  protected final FileConfiguration getConfig() {
    return this.config;
  }


  /**
   * Refreshes the unparsed configuration source from file.
   * <p>
   * Looks for a default version of the file in the plugin jar's home directory or subdirectory
   * (where applicable) and saves it if a copy of the file does not already exist in the target
   * directory.
   * 
   * @throws IllegalStateException if the source config file is incorrectly formatted.
   */
  protected final void refresh() throws IllegalStateException {
    init();
  }

  /**
   * Readies the config file for parsing.
   * <p>
   * Looks for a default version of the file in the plugin jar's home directory or subdirectory
   * (where applicable) and saves it if a copy of the file does not already exist in the target
   * directory.
   * 
   * @throws IllegalStateException if the source config file is incorrectly formatted.
   */
  private void init() throws IllegalStateException {
    fileAccessor.saveDefaultConfig();
    fileAccessor.reloadConfig();
    this.config = fileAccessor.getConfig();
  }

}
