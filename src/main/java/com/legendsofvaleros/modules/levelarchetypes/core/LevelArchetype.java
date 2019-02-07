package com.legendsofvaleros.modules.levelarchetypes.core;

import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * An implementation of an archetype that defines broad, baseline information for players and/or
 * mobs.
 */
public class LevelArchetype implements Archetype {

    private final String id;
    private Map<String, LevelingValue> levelingStats;
    private CombatProfileGenerator cpGen;

    public LevelArchetype(String id, ConfigurationSection config) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.id = id;

        levelingStats = new HashMap<>();
        Map<Stat, LevelingValue> combatEngineStats = new HashMap<>();

        Logger lg = LevelArchetypes.getInstance().getLogger();

        // loads base stats
        ConfigurationSection baseSec = config.getConfigurationSection("base-stats");
        if (baseSec != null) {
            for (String key : baseSec.getKeys(false)) {

                double baseValue = baseSec.getDouble(key);
                LevelingValue val = new LevelingValue(baseValue);
                levelingStats.put(key, val);

                try {
                    Stat stat = Stat.valueOf(key.toUpperCase().replace(" ", "_"));
                    combatEngineStats.put(stat, val);
                } catch (Exception e) {
                    // not a CombatEngine stat, does nothing in order to treat it as an arbitrary stat value
                }
            }
        }

        // loads default percentage change per level
        ConfigurationSection defSec =
                config.getConfigurationSection("default-change-per-level");
        if (defSec != null) {
            for (String key : defSec.getKeys(false)) {

                double baseValue = defSec.getDouble(key);

                LevelingValue val = levelingStats.get(key);

                if (val == null) {
                    lg.severe("A growth rate was found for the stat '" + key
                            + "' but no base stat is defined. It cannot be used until a base stat is defined.");

                } else {
                    val.setDefaultIncrease(baseValue);
                }
            }
        }

        // loads default percentage change per level
        ConfigurationSection defPercSec =
                config.getConfigurationSection("default-percentage-change-per-level");
        if (defPercSec != null) {
            for (String key : defPercSec.getKeys(false)) {

                double baseValue = defPercSec.getDouble(key);
                double multiplier = 1 + baseValue;

                LevelingValue val = levelingStats.get(key);

                if (val == null) {
                    lg.severe("A growth rate was found for the stat '" + key
                            + "' but no base stat is defined. It cannot be used until a base stat is defined.");

                } else {
                    val.setDefaultMultiplier(multiplier);
                }
            }
        }

        // loads exceptions to the default change rates
        ConfigurationSection exceptionsSec = config.getConfigurationSection("exceptions");
        if (exceptionsSec != null) {
            for (String exceptionId : exceptionsSec.getKeys(false)) {
                try {

                    ConfigurationSection exceptSec = exceptionsSec.getConfigurationSection(exceptionId);
                    int startLevel = exceptSec.getInt("start-level");
                    int endLevel = exceptSec.getInt("end-level");

                    // if start and end levels are reversed, swaps them into the correct order.
                    if (endLevel < startLevel) {
                        int num = endLevel;
                        endLevel = startLevel;
                        startLevel = num;
                    }

                    ConfigurationSection incSec =
                            exceptSec.getConfigurationSection("change-per-level");
                    if (incSec != null) {
                        for (String key : incSec.getKeys(false)) {
                            double baseValue = incSec.getDouble(key);

                            LevelingValue val = levelingStats.get(key);

                            if (val == null) {
                                lg.severe("An exception growth rate was found for the stat '"
                                        + key
                                        + "' but no base stat is defined. It cannot be used until a base stat is defined.");

                            } else {
                                for (int i = startLevel; i <= endLevel; i++) {
                                    val.addExceptionIncrease(i, baseValue);
                                }
                            }
                        }
                    }

                    ConfigurationSection multSec =
                            exceptSec.getConfigurationSection("percentage-change-per-level");
                    if (multSec != null) {
                        for (String key : multSec.getKeys(false)) {
                            double baseValue = multSec.getDouble(key);
                            double multiplier = 1 + baseValue;

                            LevelingValue val = levelingStats.get(key);

                            if (val == null) {
                                lg.severe("An exception growth rate was found for the stat '"
                                        + key
                                        + "' but no base stat is defined. It cannot be used until a base stat is defined.");

                            } else {
                                for (int i = startLevel; i <= endLevel; i++) {
                                    val.addExceptionMultiplier(i, multiplier);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    lg.severe("Could not load the exception '" + exceptionId + "' in archetype '" + id + "'.");
                    MessageUtil.sendSevereException(LevelArchetypes.getInstance(), e);
                }
            }
        }

        List<String> notIncluded = new ArrayList<>();
        for (Stat stat : Stat.values()) {
            if (!combatEngineStats.containsKey(stat)) {
                notIncluded.add(stat.name());
            }
        }
        if (!notIncluded.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            sb.append("The following CombatEngine stats have not been defined for the archetype '").append(id).append("': [");
            int index = 0;
            for (String str : notIncluded) {
                sb.append(str);
                if (index++ < notIncluded.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("].");
            lg.warning(sb.toString());
        }

        cpGen = new CombatProfileGenerator(combatEngineStats);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CombatProfile getCombatProfile(int level) {
        return cpGen.getCombatProfile(level);
    }

    @Override
    public double getStatValue(String statName, int level) {
        LevelingValue val = levelingStats.get(statName);
        if (val == null) {
            return 0;
        }
        return val.getValue(level);
    }

}
