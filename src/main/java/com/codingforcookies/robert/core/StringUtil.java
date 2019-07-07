package com.codingforcookies.robert.core;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StringUtil {
    private static final char PADDING_CHAR = ' ';
    private static final int PADDING_WIDTH = 4;

    private static int pxLen(char ch) {
        return MinecraftFont.Font.getChar(ch).getWidth() + 1;
    }

    public static int getStringWidth(String str) {
        if (str.length() == 0)
            return 0;

        ChatColor test;
        int add = 0;

        int result = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ChatColor.COLOR_CHAR) {
                test = ChatColor.getByChar(str.charAt(++i));
                if (test.isColor())
                    add = 0;
                else if (test == ChatColor.BOLD)
                    add = 1;
            } else
                result += pxLen(str.charAt(i)) + add;
        }

        return result;
    }

    public static String[] splitForStackLore(String[] arr) {
        return splitString(40, StringUtils.join(arr, '\n'));
    }

    public static String[] splitForStackLore(List<String> arr) {
        return splitString(40, StringUtils.join(arr, '\n'));
    }

    public static String[] splitForStackLore(String str) {
        return splitString(40, str);
    }

    public static String[] splitString(int chWidth, String str) {
        List<String> stuff = new ArrayList<>();
        for (String s : str.split("\n")) {
            String[] lines = WordUtils.wrap(s, chWidth).split("\n");

            String color = "";
            for (int i = 0; i < lines.length; i++) {
                lines[i] = color + lines[i];
                color = ChatColor.getLastColors(lines[i]);
            }

            stuff.addAll(Arrays.asList(lines));
        }
        return stuff.toArray(new String[0]);
    }

    public static String center(int maxWidth, String str) {
        int width = getStringWidth(str);
        StringBuilder strBuilder = new StringBuilder(str);
        while ((width += PADDING_WIDTH) <= maxWidth) {
            strBuilder.insert(0, PADDING_CHAR);
            if ((width += PADDING_WIDTH) <= maxWidth)
                strBuilder.append(PADDING_CHAR);
        }
        str = strBuilder.toString();
        return str;
    }

    public static String sides(int maxWidth, String left, String right) {
        String center;

        int width = getStringWidth(left) + getStringWidth(right);
        StringBuilder centerBuilder = new StringBuilder();
        while ((width += PADDING_WIDTH) <= maxWidth)
            centerBuilder.append(PADDING_CHAR);
        center = centerBuilder.toString();

        width -= PADDING_WIDTH;
        int remaining = maxWidth - (width - PADDING_WIDTH);
        if (remaining <= 2) {
            String add = remaining == 3 ? "." : ChatColor.BOLD + ".";
            if (center.length() > 3) {
                center = center.substring(0, center.length() / 2) + add + center.substring(center.length() / 2);
            } else
                center += add;
        }

        return left + center + right;
    }

    public static String right(int maxWidth, String prefix, String right) {
        String center;

        int width = getStringWidth(prefix) + getStringWidth(right);
        StringBuilder centerBuilder = new StringBuilder();
        while ((width += PADDING_WIDTH) <= maxWidth)
            centerBuilder.append(PADDING_CHAR);
        center = centerBuilder.toString();

        width -= PADDING_WIDTH;
        if (maxWidth - (width - PADDING_WIDTH) >= 6) {
            if (center.length() > 3) {
                center = center.substring(0, center.length() / 2) + "." + center.substring(center.length() / 2);
            } else
                center += ".";
        }

        return center + right;
    }

    /**
     * Converts a number of millseconds into human-readable time in days, hours, minutes, and seconds.
     * <p>
     * The result is a comma-separated list of time elements. For example:
     * <code>"2 days, 5 hours, 10 minutes, 32 seconds"</code>.
     * <p>
     * Numbers are all printed as digits and this does not capitalize words.
     * @param millis      The amount of time to convert into a readable string.
     * @param maxElements The maximum number of time elements (hours, days, etc.) to include. Smaller
     *                    elements will be ignored first, reducing precision for a shorter string. Elements with a
     *                    value of <code>0</code> are always ignored. For example, if <code>2</code> and this is
     *                    given a large number, it would only return the number of days and the number of hours.
     *                    For a smaller number, only on the scale of hours, a value of <code>2</code> would make
     *                    this only return the hours and minutes.
     * @param capitalize  <code>true</code> if the first letters of each time unit should be
     *                    capitalized (ex: <code>Minutes</code>). Else <code>false</code> (ex:
     *                    <code>minutes</code>).
     * @return A human-readable string version of the given number of milliseconds.
     */
    public static String getTimeFromMilliseconds(long millis, int maxElements, boolean capitalize) {
        if (millis < 1) {
            return "0 seconds";
        }
        if (maxElements > 4) {
            maxElements = 4;
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        int numElements = 0;
        StringBuilder sb = new StringBuilder(maxElements * 8);
        if (days > 0) {
            numElements =
                    appendIfNeeded(sb, numElements, maxElements, String.valueOf(days), " "
                            + (capitalize ? "D" : "d") + "ay", (days == 1 ? "" : "s"), ", ");
        }
        if (hours > 0) {
            numElements =
                    appendIfNeeded(sb, numElements, maxElements, String.valueOf(hours), " "
                            + (capitalize ? "H" : "h") + "our", (hours == 1 ? "" : "s"), ", ");
        }
        if (minutes > 0 && numElements < 2) {
            numElements =
                    appendIfNeeded(sb, numElements, maxElements, String.valueOf(minutes), " "
                            + (capitalize ? "M" : "m") + "inute", (minutes == 1 ? "" : "s"), ", ");
        }

        // "0 seconds" if there is less than a second left.
        if ((seconds > 0 || numElements == 0) && numElements < 2) {
            appendIfNeeded(sb, numElements, maxElements, String.valueOf(seconds), " "
                    + (capitalize ? "S" : "s") + "econd", (seconds == 1 ? "" : "s"));
        }

        String ret = sb.toString();
        if (ret.endsWith(", ")) {
            return ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    private static int appendIfNeeded(StringBuilder sb, int numElements, int maxElements,
                                      String... appendWith) {
        if (numElements >= maxElements) {
            return maxElements;
        }
        for (String anAppendWith : appendWith) {
            sb.append(anAppendWith);
        }
        return ++numElements;
    }
}