package com.legendsofvaleros.util;

import org.bukkit.ChatColor;

import java.text.DecimalFormat;

/**
 * Created by Crystall on 10/10/2018
 * Helper class to determine the possible lag on the server
 */
public class Lag {

    private Lag() {
    }

    public static String readableByteSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

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

    /**
     * Creates a progressbar for the given tps
     * @param tps
     * @return
     */
    public static String createTPSBar(double tps) {
        ChatColor tpsc = ChatColor.GREEN;
        if (tps < 14.5) tpsc = ChatColor.YELLOW;
        if (tps < 9) tpsc = ChatColor.GOLD;
        if (tps < 5.5) tpsc = ChatColor.RED;
        if (tps < 2.7) tpsc = ChatColor.DARK_RED;
        return getBar((float) ((tps + 0.5) / 20F), 40, tpsc, ChatColor.GRAY, ChatColor.DARK_GREEN);
    }
}