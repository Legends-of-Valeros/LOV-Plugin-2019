package com.legendsofvaleros.modules.chat;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 11/24/2018
 * Represents a chatchannel a player can talk in
 */
public enum ChatChannel {
    WORLD('W', "", ChatColor.GRAY, ChatColor.GRAY, "World", false, true),
    TRADE('T', "", ChatColor.GREEN, ChatColor.GREEN, "Trade", false, true),
    PARTY('P', "", ChatColor.YELLOW, ChatColor.YELLOW, "Party", true, false),
    ZONE('Z', "", ChatColor.DARK_AQUA, ChatColor.WHITE, "Zone", true, false),
    LOCAL('L', "", ChatColor.WHITE, ChatColor.WHITE, "Local", true, false);

    @Getter
    Character prefix;

    @Getter
    String suffix;

    @Getter
    ChatColor tagColor;

    @Getter
    ChatColor chatColor;

    @Getter
    String name;

    @Getter
    boolean canSetDefault;

    @Getter
    boolean canDisable;

    ChatChannel(Character prefix, String suffix, ChatColor tagColor, ChatColor chatColor, String name, boolean canSetDefault, boolean canDisable) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.tagColor = tagColor;
        this.chatColor = chatColor;
        this.name = name;
        this.canSetDefault = canSetDefault;
        this.canDisable = canDisable;
    }

}
