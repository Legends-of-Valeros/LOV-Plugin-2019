package com.legendsofvaleros.modules.characters.events;

import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class PlayerCharacterInventoryFillEvent extends PlayerCharacterEvent {

  private static final HandlerList handlers = new HandlerList();

  private final boolean firstLoad;

  public PlayerCharacterInventoryFillEvent(PlayerCharacter playerCharacter, boolean firstLoad) {
    super(playerCharacter);
    this.firstLoad = firstLoad;
  }

  public boolean isFirstLoad() {
    return firstLoad;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
