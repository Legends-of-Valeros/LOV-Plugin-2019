package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class StatDisplay {
    @FunctionalInterface
    public interface IStatDisplay {
        String[] getPerPoint();
    }

    private static final DecimalFormat DF = new DecimalFormat("#.00");

    private static Map<Stat, IStatDisplay> display = new HashMap<>();
    static {
        display.put(Stat.HEALTH_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getRegenPercentagePerPoint(RegeneratingStat.HEALTH) * 100) + "% rate"});
        display.put(Stat.MANA_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getRegenPercentagePerPoint(RegeneratingStat.MANA) * 100) + "% rate"});
        display.put(Stat.ENERGY_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getRegenPercentagePerPoint(RegeneratingStat.ENERGY) * 100) + "% rate"});
        display.put(Stat.SPEED, () -> new String[]{"+" + DF.format((1 / CombatEngine.getEngineConfig().getSpeedPointsPerPotionLevel())) + " speed level"});
        display.put(Stat.PHYSICAL_ATTACK, () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getPhysicalDamageIncrease() * 100) + " damage"});
        display.put(Stat.MAGIC_ATTACK, () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getMagicDamageIncrease() * 100) + " damage"});
        display.put(Stat.ARMOR, () -> new String[]{
                "+" + DF.format(CombatEngine.getEngineConfig().getArmorPhysicalDamageReduction() * 100) + "% physical reduction",
                "+" + DF.format(CombatEngine.getEngineConfig().getArmorSpellDamageReduction() * 100) + "% magical reduction"
        });

        IStatDisplay iStatDisplay = () -> new String[]{"+" + DF.format(CombatEngine.getEngineConfig().getResistanceSpellDamageReduction() * 100) + "% reduction"};
        display.put(Stat.FIRE_RESISTANCE, iStatDisplay);
        display.put(Stat.ICE_RESISTANCE, iStatDisplay);
    }

    public static IStatDisplay getFor(Stat stat) {
        return display.get(stat);
    }
}
