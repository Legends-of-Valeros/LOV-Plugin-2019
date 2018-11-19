package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.combatengine.api.CombatEngineAPI;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.UnsafePlayerInitializer;
import com.legendsofvaleros.modules.combatengine.config.BukkitConfig;
import com.legendsofvaleros.modules.combatengine.config.CombatEngineConfig;
import com.legendsofvaleros.modules.combatengine.damage.DamageEngine;
import com.legendsofvaleros.modules.combatengine.damage.DamageHistory;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.ui.CombatEngineUiManager;
import com.legendsofvaleros.modules.combatengine.ui.PlayerCombatInterface;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * CombatEngine creates an MMO-style combat system on a Minecraft server by overriding and hiding
 * Bukkit's normal combat system.
 */
public class CombatEngine extends ListenerModule implements CombatEngineAPI {
    private static CombatEngineConfig config;
    private static CombatEngine singleton;

    private EntityTracker entities;
    private MinecraftHealthHandler mcHealthHandler;
    private SpeedEngine speedEngine;
    private DamageEngine damageEngine;
    private CombatEngineUiManager playerInterfaces;

    private Unsafe unsafe;

    /**
     * Gets the singleton instance of CombatEngine, which implements its main API.
     * @return The CombatEngine instance.
     */
    public static CombatEngine getInstance() {
        return singleton;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!LegendsOfValeros.getInstance().getServer().getPluginManager().isPluginEnabled("LibsDisguises")) {
            getLogger().warning("LibsDisguises is not enabled on this server. Disguises will not work without it!");
        }

        singleton = this;

        config = new BukkitConfig();

        this.mcHealthHandler = new MinecraftHealthHandler();
        this.speedEngine = new SpeedEngine(config);
        this.damageEngine = new DamageEngine(config, mcHealthHandler);
        new StatRegenerator(config);
        new RespawnListener(config);
        EntityThreatLevels.onEnable(config);

        this.entities = new EntityTracker(config.getDefaultProfile());

        this.unsafe = new Unsafe();

        new AttackForEffectsListener();
    }

    @Override
    public void onUnload() {
        super.onUnload();

        EntityThreatLevels.onDisable();
    }

    public static CombatEngineConfig getEngineConfig() {
        return config;
    }

    public static CombatEntity getEntity(LivingEntity getFor) {
        return singleton.getCombatEntity(getFor);
    }

    @Override
    public CombatEntity getCombatEntity(LivingEntity getFor) {
        if (getFor == null) {
            return null;
        }
        return entities.getCombatEntity(getFor);
    }

    public DamageHistory getDamageHistory(LivingEntity entity) {
        return damageEngine.getAttributer().getDamageHistory(entity);
    }

    @Override
    public boolean causeSpellDamage(LivingEntity target, LivingEntity attacker, SpellType type,
                                    double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit) {
        return damageEngine.causeSpellDamage(target, attacker, type, baseDamage, damageOrigin, canMiss,
                canCrit);
    }

    @Override
    public boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
                                       double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit) {
        return damageEngine.causePhysicalDamage(target, attacker, type, baseDamage, 1, damageOrigin,
                canMiss, canCrit);
    }

    @Override
    public boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
                                       double baseDamage, double swingMultiplier, Location damageOrigin, boolean canMiss, boolean canCrit) {
        return damageEngine.causePhysicalDamage(target, attacker, type, baseDamage, swingMultiplier, damageOrigin,
                canMiss, canCrit);
    }

    @Override
    public boolean causeTrueDamage(LivingEntity target, LivingEntity attacker, double damage,
                                   Location damageOrigin) {
        return damageEngine.causeTrueDamage(target, attacker, damage, damageOrigin);
    }

    @Override
    public void killEntity(LivingEntity target) {
        damageEngine.killEntity(target);
    }

    /**
     * Sets the player interface manager that CombatEngine will get players' user interfaces from.
     * @param uiManager The provider and manager of player interfaces that will be updated with
     *                  player's combat data as it changes.
     */
    public void setUserInterfaceManager(CombatEngineUiManager uiManager) {
        this.playerInterfaces = uiManager;
    }

    /**
     * Gets unsafe methods for interacting with and editing CombatEngine.
     * <p>
     * These methods should not be used without a detailed knowledge of CombatEngine's internal
     * mechanics.
     * @return Unsafe methods for editing CombatEngine's functionality.
     */
    public Unsafe unsafe() {
        return unsafe;
    }

    /**
     * Gets the CombatEngine user interface object for a player, if one exists.
     * @param player The player to get a user interface for.
     * @return The player's user interface to inform of changes in their combat data.
     * <code>null</code> if none was found for the given player.
     */
    PlayerCombatInterface getPlayerInterface(Player player) {
        if (player == null || playerInterfaces == null) {
            return null;
        }
        return playerInterfaces.getPlayerInterface(player);
    }

    /**
     * Gets the handler for translating between CombatEngine health and vanilla Minecraft health.
     * @return The vanilla Minecraft health handler.
     */
    MinecraftHealthHandler getMinecraftHealthHandler() {
        return mcHealthHandler;
    }

    /**
     * Gets the handler for translating from speed stats to entity's in-game movement speed.
     * @return The speed engine.
     */
    SpeedEngine getSpeedEngine() {
        return speedEngine;
    }

    /**
     * Unsafe methods for editing CombatEngine functionality.
     */
    public class Unsafe {

        /**
         * Sets initializing/invalidating player combat data to be done manually through the returned
         * object.
         * @return An object through which to manually manage initializing/invalidating players' combat
         * entity objects.
         */
        public UnsafePlayerInitializer manuallyManagePlayerInitialization() {
            entities.usePlayerInitializer();
            return entities;
        }
    }

}
