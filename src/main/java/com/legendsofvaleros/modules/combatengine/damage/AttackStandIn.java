package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * A wrapper for a non-existent entity who can be used to deal damage on behalf of another entity
 * but maintains its own identifiers so it can distinguish between things like different types of
 * attacks, for example.
 * <p>
 * Some of the entities attributes can be edited to serve better as a stand in for the real damager.
 */
public class AttackStandIn {

  private static final String GET_NMS_METHOD = "getHandle";
  private static final String SET_LOCATION_METHOD = "setLocation";

  private final EntityType type;
  private Entity entity;

  // reflection fields
  private Object nmsEntity;
  private Method setLocation;

  public AttackStandIn(EntityType standInType) throws IllegalArgumentException {
    if (standInType == null) {
      throw new IllegalArgumentException("entity type cannot be null");
    }
    this.type = standInType;
    World world = Bukkit.getWorlds().get(0);
    if (world == null) {
      throw new IllegalStateException("Could not get a world to spawn the entity in");
    }
    entity = world.spawnEntity(world.getSpawnLocation(), type);
    entity.remove();
  }

  /**
   * Causes damage from this entity.
   * <p>
   * If this damager is set to automatically attribute to another entity, the damage event caused by
   * this will be attributed to that other entity.
   * 
   * @param target The entity to damage.
   * @param baseDamage The amount of damage to do. Will thrown an entity damage event and be
   *        subsequently affected by things like armor and possibly event listeners.
   */
  public final void causeDamage(LivingEntity target, double baseDamage) {
    if (target == null) {
      return;
    }
    
    if(entity instanceof LivingEntity)
    	target.damage(baseDamage, entity);
    else{
    	target.damage(baseDamage);
    	
    	target.playEffect(EntityEffect.HURT);
    }
  }

  /**
   * Gets this stand-in damager's entity object.
   * 
   * @return The entity.
   */
  public final Entity getEntity() {
    return entity;
  }

  /**
   * Gets this entity's name.
   * 
   * @return This entity's name.
   */
  public final int getEntityId() {
    return entity.getEntityId();
  }

  /**
   * Gets this entity's unique name.
   * 
   * @return This entity's unique name.
   */
  public final UUID getUniqueId() {
    return entity.getUniqueId();
  }

  /**
   * Set's the entity's coordinates.
   * <p>
   * By setting the entity's coordinates, you set the direction from which the player will be
   * knocked back upon taking damage from this entity. For example, if you had an explosion spell
   * and you set the coordinates to the explosion's epicentre, upon taking damage from this stand-in
   * entity, they would be thrown back slightly from the epicentre, rather than from an arbitrary
   * direction or from the player who cast the spell.
   * <p>
   * The entity's world does not matter. It can be used across worlds, and these coordinates will be
   * used in whatever world it is used in.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   */
  public final void setCoordinates(double x, double y, double z) {
    // Uses reflection to set the dummy, removed/dead stand-in entity's location in order to set
    // from where the attacked entity is knocked back upon taking damage.
    //
    // Because this uses reflection into MC internals, may need to be updated with new MC versions
    try {
      if (setLocation == null || nmsEntity == null) {
        Class<? extends Entity> clazz = entity.getClass();
        Method getEntity = clazz.getMethod(GET_NMS_METHOD);
        nmsEntity = getEntity.invoke(entity);
        setLocation =
            nmsEntity.getClass().getMethod(SET_LOCATION_METHOD, double.class, double.class,
                double.class, float.class, float.class);
      }
      setLocation.invoke(nmsEntity, x, y, z, 0, 0);

    } catch (Exception e) {
      System.out.println("[CombatEngine] Could not set a fake entity's location through reflection");
      MessageUtil.sendException(LegendsOfValeros.getInstance(), entity instanceof Player ? (Player)entity : null, e, true);
    }
  }

}
