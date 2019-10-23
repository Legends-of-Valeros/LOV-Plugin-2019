package com.legendsofvaleros.util;

public class RomanNumeral {
    final static char symbol[] = {'M', 'D', 'C', 'L', 'X', 'V', 'I'};
    final static int value[] = {1000, 500, 100, 50, 10, 5, 1};

    private static int[] numbers = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    private static String[] letters = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    public static int valueOf(String roman) {
        roman = roman.toUpperCase();
        if (roman.length() == 0) {
            return 0;
        }
        for (int i = 0; i < symbol.length; i++) {
            int pos = roman.indexOf(symbol[i]);
            if (pos >= 0) {
                return value[i] - valueOf(roman.substring(0, pos)) + valueOf(roman.substring(pos + 1));
            }
        }
        throw new IllegalArgumentException("Invalid Roman Symbol.");
    }

    public static String convertToRoman(int n) {
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            while (n >= numbers[i]) {
                roman.append(letters[i]);
                n -= numbers[i];
            }
        }
        return roman.toString();
    }

}