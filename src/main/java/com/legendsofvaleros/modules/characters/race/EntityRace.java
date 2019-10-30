package com.legendsofvaleros.modules.characters.race;

/**
 * The races players can play as.
 */
public enum EntityRace {

  NORD("Nordan"),
  WOOD_ELF("Sylfen"),
  HIGH_ELF("Fayfen"),
  DWARF("Dwarfen"),
  HUMAN("Arkaska"),
  ORC("Orcfen");

  private final String uiName;

  EntityRace(String uiName) {
    this.uiName = uiName;
  }

  /**
   * Gets a user-friendly name of this player race.
   * 
   * @return A user friendly name for this that can be used in user interfaces.
   */
  public String getUserFriendlyName() {
    return uiName;
  }

  public static EntityRace getRaceByName(String name) {
      EntityRace[] races = EntityRace.values();
      for (EntityRace race : races) {
          if (race.name().equalsIgnoreCase(name)) {
              return race;
          }
      }
      return null;
  }
}
