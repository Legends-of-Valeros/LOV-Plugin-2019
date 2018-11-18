package com.legendsofvaleros.modules.characters.testing;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.config.ClassConfig;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerInformationBookEvent;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerBookListener implements Listener {
    private static final DecimalFormat DF = new DecimalFormat("#.00");

    private Map<Stat, IStatDisplay> display = new HashMap<>();

    public PlayerBookListener() {
        display.put(Stat.HEALTH_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getRegenPercentagePerPoint(RegeneratingStat.HEALTH) * 100) + "% rate"});
        display.put(Stat.MANA_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getRegenPercentagePerPoint(RegeneratingStat.MANA) * 100) + "% rate"});
        display.put(Stat.ENERGY_REGEN, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getRegenPercentagePerPoint(RegeneratingStat.ENERGY) * 100) + "% rate"});
        display.put(Stat.SPEED, () -> new String[]{"+" + DF.format((1 / CombatEngine.getConfig().getSpeedPointsPerPotionLevel())) + " speed level"});
        display.put(Stat.PHYSICAL_ATTACK, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getPhysicalDamageIncrease() * 100) + "% damage"});
        display.put(Stat.MAGIC_ATTACK, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getMagicDamageIncrease() * 100) + "% damage"});
        display.put(Stat.ARMOR, () -> new String[]{
                "+" + DF.format(CombatEngine.getConfig().getArmorPhysicalDamageReduction() * 100) + "% physical reduction",
                "+" + DF.format(CombatEngine.getConfig().getArmorSpellDamageReduction() * 100) + "% magical reduction"
        });
        display.put(Stat.FIRE_RESISTANCE, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getResistanceSpellDamageReduction() * 100) + "% reduction"});
        display.put(Stat.ICE_RESISTANCE, () -> new String[]{"+" + DF.format(CombatEngine.getConfig().getResistanceSpellDamageReduction() * 100) + "% reduction"});
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBookOpen(PlayerInformationBookEvent event) {
        PlayerCharacter pc = event.getPlayerCharacter();
        CombatEntity ce = CombatEngine.getEntity(event.getPlayer());

        FancyMessage fm =
                new FancyMessage(StringUtil.center(Book.WIDTH, event.getPlayer().getName()) + "\n").color(ChatColor.GOLD).style(ChatColor.UNDERLINE)
                        .then(StringUtil.center(Book.WIDTH, pc.getPlayerRace().getUserFriendlyName() + " " + pc.getPlayerClass().getUserFriendlyName()) + "\n").color(ChatColor.DARK_PURPLE)
                        .then(StringUtil.center(Book.WIDTH, "Level " + pc.getExperience().getLevel()) + "\n\n").color(ChatColor.DARK_GRAY);

        StringBuilder tooltip = new StringBuilder();

        ClassConfig cc = Characters.inst().getCharacterConfig().getClassConfig(pc.getPlayerClass());
        boolean hasMods = false;
        for (AbilityStat as : AbilityStat.values()) {
            tooltip.append(ChatColor.GRAY);
            tooltip.append(String.join("\n", StringUtil.splitForStackLore(Characters.inst().getCharacterConfig().getStatDescription(as))));
            tooltip.append("\n\n");
            for (StatModifierModel mod : cc.getModifiers(as)) {
                if (!hasMods) {
                    hasMods = true;
                    tooltip.append(ChatColor.GOLD);
                    tooltip.append(ChatColor.BOLD);
                    tooltip.append("Per Level:");
                    tooltip.append(ChatColor.WHITE);
                }

                tooltip.append("\n ");
                tooltip.append(mod.getStat().getUserFriendlyName());
                switch (mod.getModifierType()) {
                    case FLAT_EDIT:
                    case FLAT_EDIT_IGNORES_MULTIPLIERS:
                        tooltip.append(" + ");
                        tooltip.append(DF.format(mod.getValue()));
                        break;
                    case MULTIPLIER:
                        tooltip.append(" * ");
                        tooltip.append(DF.format(mod.getValue() * 100));
                        tooltip.append("%");
                        break;
                }
            }

            if (!hasMods) {
                tooltip.append(ChatColor.RED);
                tooltip.append(ChatColor.ITALIC);
                tooltip.append("* Does not affect your character.");
            } else
                hasMods = false;

            fm.then(as.getUserFriendlyName() + ":").color(ChatColor.BLACK).style(ChatColor.BOLD)
                    .tooltip(as.getUserFriendlyName(),
                            tooltip.toString().trim())
                    .then(StringUtil.right(Book.WIDTH, ChatColor.BOLD + as.getUserFriendlyName() + ":", as.formatForUserInterface(pc.getAbilityStats().getAbilityStat(as))) + "\n").color(ChatColor.DARK_GRAY);

            tooltip.setLength(0);
        }

        event.getPages().add(fm);

        Set<Stat> filter = new HashSet<>();
        for (EntityClass clazz : EntityClass.values()) {
            if (clazz == pc.getPlayerClass()) continue;
            filter.add(clazz.getSkillCostType().getMaxStat());
            filter.add(clazz.getSkillCostType().getRegenStat());
        }

        IStatDisplay disp;
        for (Stat.Category cat : Stat.Category.values()) {
            fm = new FancyMessage(StringUtil.center(Book.WIDTH, cat.getUserFriendlyName() + " Stats") + "\n\n").color(ChatColor.BLACK).style(ChatColor.UNDERLINE);
            for (Stat s : Stat.values()) {
                if (s.getCategory() != cat) continue;

                tooltip.append(ChatColor.GRAY);
                tooltip.append(String.join("\n", StringUtil.splitForStackLore(CombatEngine.getConfig().getStatDescription(s))));

                if ((disp = display.get(s)) != null) {
                    tooltip.append("\n\n");
                    tooltip.append(ChatColor.GOLD);
                    tooltip.append(ChatColor.BOLD);
                    tooltip.append("Per Point:");
                    tooltip.append(ChatColor.WHITE);

                    for (String str : disp.getPerPoint()) {
                        tooltip.append("\n ");
                        tooltip.append(str);
                    }
                }


                fm.then(s.getShortName() + ":").color(ChatColor.BLACK)
                        .tooltip(s.getUserFriendlyName(),
                                tooltip.toString().trim())
                        .then(StringUtil.right(Book.WIDTH, s.getShortName(), s.formatForUserInterface(ce.getStats().getStat(s))) + "\n").color(ChatColor.DARK_GRAY);

                tooltip.setLength(0);
            }
            event.getPages().add(fm);
        }
		
		/*fm = new FancyMessage(StringUtil.center(Book.WIDTH, "Your Deity") + "\n\n").color(ChatColor.BLACK)
					.then(StringUtil.center(Book.WIDTH, "Alphax") + "\n\n").color(ChatColor.RED)
					.then("Born among the waters of death, thrive in the blood of thy enemies.\n\n").color(ChatColor.BLACK)
					.then(" +10% XP").color(ChatColor.DARK_AQUA);*/
    }

    @FunctionalInterface
    private interface IStatDisplay {
        String[] getPerPoint();
    }
}