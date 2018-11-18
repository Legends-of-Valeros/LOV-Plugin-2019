package com.legendsofvaleros.util;

import org.bukkit.ChatColor;

/**
 * Created by Crystall on 10/10/2018
 * Helper class for indicating a progress bar
 */
public class ProgressBar {
    public static String getBar(float percentage, int length, ChatColor content1, ChatColor content2, ChatColor wrap) {
        String ret = wrap + "[" + content1;
        int i = 0;
        for (; i < percentage * length; i++) {
            ret += "|";
        }
        ret += content2;
        for (; i < length; i++) {
            ret += "|";
        }
        ret += wrap + "]";
        return ret;
    }
}

