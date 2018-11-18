package com.legendsofvaleros.modules.characters.config;

/**
 * Database configuration options.
 */
public interface DatabaseConfig {

  /**
   * Gets the name of the database pool in DBPools to use.
   * 
   * @return The database to use.
   */
  String getDbPoolsId();

}
