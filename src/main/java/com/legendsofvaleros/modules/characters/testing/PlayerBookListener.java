package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.util.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.config.ClassConfig;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.characters.events.PlayerInformationBookEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class PlayerBookListener implements Listener {
    private static final DecimalFormat DF = new DecimalFormat("#.00");

    public PlayerBookListener() {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBookOpen(PlayerInformationBookEvent event) {
        PlayerCharacter pc = event.getPlayerCharacter();
        CombatEntity ce = CombatEngine.getEntity(event.getPlayer());

        TextBuilder tb =
                new TextBuilder(StringUtil.center(Book.WIDTH, event.getPlayer().getName()) + "\n").color(ChatColor.GOLD).underlined(true)
                        .append(StringUtil.center(Book.WIDTH, pc.getPlayerRace().getUserFriendlyName() + " " + pc.getPlayerClass().getUserFriendlyName()) + "\n").color(ChatColor.DARK_PURPLE)
                        .append(StringUtil.center(Book.WIDTH, "Level " + pc.getExperience().getLevel()) + "\n\n").color(ChatColor.DARK_GRAY);

        StringBuilder tooltip = new StringBuilder();

        ClassConfig cc = Characters.getInstance().getCharacterConfig().getClassConfig(pc.getPlayerClass());
        boolean hasMods = false;
        for (AbilityStat as : AbilityStat.values()) {
            tooltip.append(ChatColor.GRAY);
            tooltip.append(String.join("\n", StringUtil.splitForStackLore(Characters.getInstance().getCharacterConfig().getStatDescription(as))));
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

            tb.append(as.getUserFriendlyName() + ":").color(ChatColor.BLACK).bold(true)
                    .hover(as.getUserFriendlyName(), "", tooltip.toString().trim());

            tb.append(StringUtil.right(Book.WIDTH, ChatColor.BOLD + as.getUserFriendlyName() + ":", as.formatForUserInterface(pc.getAbilityStats().getAbilityStat(as))) + "\n").color(ChatColor.DARK_GRAY);

            tooltip.setLength(0);
        }

        event.getPages().add(tb.create());

        Set<Stat> filter = new HashSet<>();
        filter.add(Stat.HEALTH_REGEN);
        filter.add(Stat.MAX_HEALTH);

        for (EntityClass clazz : EntityClass.values()) {
            if (clazz == pc.getPlayerClass()) continue;
            filter.add(clazz.getSkillCostType().getMaxStat());
            filter.add(clazz.getSkillCostType().getRegenStat());
        }

        StatDisplay.IStatDisplay disp;
        for (Stat.Category cat : Stat.Category.values()) {
            tb = new TextBuilder(StringUtil.center(Book.WIDTH, cat.getUserFriendlyName() + " Stats") + "\n\n").color(ChatColor.BLACK).underlined(true);
            for (Stat s : Stat.values()) {
                if(s.getCategory() != cat) continue;
                if(!filter.contains(s)) continue;

                tooltip.append(ChatColor.GRAY);
                tooltip.append(String.join("\n", StringUtil.splitForStackLore(CombatEngine.getEngineConfig().getStatDescription(s))));

                if ((disp = StatDisplay.getFor(s)) != null) {
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

                tb.append(s.getShortName() + ":").color(ChatColor.BLACK)
                        .hover(s.getUserFriendlyName(), "", tooltip.toString().trim())
                        .append(StringUtil.right(Book.WIDTH, s.getShortName(), s.formatForUserInterface(ce.getStats().getStat(s))) + "\n").color(ChatColor.DARK_GRAY);

                tooltip.setLength(0);
            }

            event.getPages().add(tb.create());
        }
		
		/*fm = new FancyMessage(StringUtil.center(Book.WIDTH, "Your Deity") + "\n\n").color(ChatColor.BLACK)
					.then(StringUtil.center(Book.WIDTH, "Alphax") + "\n\n").color(ChatColor.RED)
					.then("Born among the waters of death, thrive in the blood of thy enemies.\n\n").color(ChatColor.BLACK)
					.then(" +10% XP").color(ChatColor.DARK_AQUA);*/
    }

}