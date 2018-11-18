package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An event thrown when an entity dies.
 */
public class CombatEngineDeathEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity died;
  private final CombatEntity killer;
  public List<ItemStack> drops;

  /**
   * Class constructor.
   * 
   * @param died The entity that died.
   * @param killer The entity responsible for the death. Can be <code>null</code> if the death's
   *        cause was ambiguous or not caused by an entity.
   */
  public CombatEngineDeathEvent(CombatEntity died, CombatEntity killer) {
    if (died == null) {
      throw new IllegalArgumentException("dying entity cannot be null");
    }
    this.died = died;
    this.killer = killer;
    this.drops = new ArrayList<>();
  }

  /**
   * Gets the entity that died.
   * 
   * @return The dead entity.
   */
  public CombatEntity getDied() {
    return died;
  }

  /**
   * Gets the killer of the dead entity, if there is one.
   * 
   * @return The killer. <code>null</code> if the death's cause was ambiguous or not caused by an
   *         entity.
   */
  public CombatEntity getKiller() {
    return killer;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
