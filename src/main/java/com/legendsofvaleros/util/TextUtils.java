package com.legendsofvaleros.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 02/13/2019
 */
public class TextUtils {

    /**
     * Capitalize every letter after a space.
     *
     * @param sentence
     * @return capitalized
     */
    public static String capitalize(String sentence) {
        String[] split = sentence.replaceAll("_", " ").split(" ");
        List<String> out = new ArrayList<>();
        for (String s : split)
            out.add(s.length() > 0 ? s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() : "");
        return String.join(" ", out);
    }
}
