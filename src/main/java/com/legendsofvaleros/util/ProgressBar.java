package com.legendsofvaleros.util;

import org.bukkit.ChatColor;

/**
 * Created by Crystall on 10/10/2018
 * Helper class for indicating a progress bar
 */
public class ProgressBar {

    /**
     * Builds a bar for the given percentage
     * @param percentage
     * @param length
     * @param content1
     * @param content2
     * @param wrap
     * @return
     */
    public static String getBar(float percentage, int length, ChatColor content1, ChatColor content2, ChatColor wrap) {
        StringBuilder builder = new StringBuilder(wrap + "[" + content1);
        for (int i = 0; i < percentage * length; i++) {
            builder.append("▬");
        }

        builder.append(content2);

        for (int i = 0; i <= length; i++) {
            builder.append("▬");
        }

        builder.append(wrap).append("]");

        return builder.toString();
    }
}

