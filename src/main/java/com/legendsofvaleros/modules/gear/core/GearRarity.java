package com.legendsofvaleros.modules.gear.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public enum GearRarity {
    POOR(ChatColor.GRAY, "✘ Poor"),
    COMMON(ChatColor.WHITE, "Common"),
    UNCOMMON(ChatColor.GREEN, "▲ Uncommon"),
    RARE(ChatColor.BLUE, "★ Rare"),
    EPIC(ChatColor.DARK_PURPLE, "☼ Epic"),
    LEGENDARY(ChatColor.GOLD, "♚ Legendary"),
    MYTHICAL(ChatColor.AQUA, "☯ Mythical"),
    UNIQUE(ChatColor.LIGHT_PURPLE, "◆ Unique");

	private static Scoreboard SB;
	
    private final ChatColor chatColor;
    public ChatColor getChatColor() { return chatColor; }

	private final String name;
	public String getUserFriendlyName() { return name; }
	
	private Team team;
	
    GearRarity(ChatColor chatColor, String name) {
        this.chatColor = chatColor;
    	this.name = name;
    }

    public static GearRarity getRarityLevel(String name) {
        for(GearRarity rarity : values())
            if(rarity.name().equalsIgnoreCase(name))
                return rarity;
        return null;
    }
    
    public Team getTeam() {
    	if(SB == null) SB = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
    	if(team == null) {
    	    if((team = SB.getTeam("gear-" + name())) == null) {
                team = SB.registerNewTeam("gear-" + name());
                team.setPrefix(chatColor + "");
            }
    	}
    	return team;
    }
}