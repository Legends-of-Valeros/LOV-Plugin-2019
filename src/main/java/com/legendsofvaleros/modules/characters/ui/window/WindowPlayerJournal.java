package com.legendsofvaleros.modules.characters.ui.window;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WindowPlayerJournal extends GUI {
	public WindowPlayerJournal(Player p) {
		super("Personal Journal");
		
		if(!Characters.isPlayerCharacterLoaded(p)) return;
		
		type(6);

		PlayerCharacter pc = Characters.getInstance().getCharacters(p).getCurrentCharacter();
		
		slot(1, 1, new ItemBuilder(Material.LEATHER_HELMET)
				.create(), null);
		slot(1, 2, new ItemBuilder(Material.LEATHER_CHESTPLATE)
				.create(), null);
		slot(1, 3, new ItemBuilder(Material.LEATHER_LEGGINGS)
				.create(), null);
		slot(1, 4, new ItemBuilder(Material.LEATHER_BOOTS)
				.create(), null);
		
		slot(4, 2, new ItemBuilder(Material.SKULL_ITEM)
				.setName(pc.getPlayerRace().getUserFriendlyName() + " " + pc.getPlayerClass().getUserFriendlyName())
				.addLore("",
						ChatColor.YELLOW + AbilityStat.STRENGTH.getUserFriendlyName() + ChatColor.GRAY + ": " + pc.getAbilityStats().getAbilityStat(AbilityStat.STRENGTH),
						ChatColor.YELLOW + AbilityStat.AGILITY.getUserFriendlyName() + ChatColor.GRAY + ": " + pc.getAbilityStats().getAbilityStat(AbilityStat.AGILITY),
						ChatColor.YELLOW + AbilityStat.STAMINA.getUserFriendlyName() + ChatColor.GRAY + ": " + pc.getAbilityStats().getAbilityStat(AbilityStat.STAMINA),
						ChatColor.YELLOW + AbilityStat.ENDURANCE.getUserFriendlyName() + ChatColor.GRAY + ": " + pc.getAbilityStats().getAbilityStat(AbilityStat.ENDURANCE),
						ChatColor.YELLOW + AbilityStat.INTELLIGENCE.getUserFriendlyName() + ChatColor.GRAY + ": " + pc.getAbilityStats().getAbilityStat(AbilityStat.INTELLIGENCE))
				.create(), null);
		slot(4, 3, new ItemBuilder(Material.EYE_OF_ENDER)
				.addLore("You follow " + ChatColor.GOLD + "" + ChatColor.BOLD + "Robert",
						" +10% XP",
						" +1 Instant heal per day",
						"",
						ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left click" + ChatColor.DARK_GRAY + " for more information]")
				.create(), (gui, p1, event) -> {
                    // TODO: GOD STUFF
                });
		slot(7, 2, new ItemBuilder(Material.EXP_BOTTLE)
				.addLore(ChatColor.YELLOW + "Level: " + ChatColor.GRAY + pc.getExperience().getLevel(),
						ChatColor.YELLOW + "XP: " + ChatColor.GRAY + pc.getExperience().getExperienceTowardsNextLevel())
				.create(), null);
	}
}