package com.legendsofvaleros.util.cmd;

import java.util.List;

public abstract class CommandArgument<T> {
    public String getArgumentLabel() { return null; }

    public abstract String getDescription();

    public abstract T doParse(String arg);

    public List<String> onTabComplete() { return null; }

    public static class StringType extends CommandArgument<String> {
        private final String label, description;
        public StringType(String label, String description) { this.label = label; this.description = description; }
        public String getArgumentLabel() { return label; }
        @Override public String getDescription() { return description; }
        @Override public String doParse(String arg) { return arg; }
    }

    public static class ByteType extends CommandArgument<Byte> {
        private final String description;
        public ByteType(String description) { this.description = description; }
        public String getArgumentLabel() { return Byte.MIN_VALUE + "-" + Byte.MAX_VALUE; }
        @Override public String getDescription() { return description; }
        @Override public Byte doParse(String arg) { return Byte.parseByte(arg); }
    }

    public static class ShortType extends CommandArgument<Short> {
        private final String description;
        public ShortType(String description) { this.description = description; }
        public String getArgumentLabel() { return Short.MIN_VALUE + "-" + Short.MAX_VALUE; }
        @Override public String getDescription() { return description; }
        @Override public Short doParse(String arg) { return Short.parseShort(arg); }
    }

    public static class IntegerType extends CommandArgument<Integer> {
        private final String description;
        public IntegerType(String description) { this.description = description; }
        public String getArgumentLabel() { return Integer.MIN_VALUE + "-" + Integer.MAX_VALUE; }
        @Override public String getDescription() { return description; }
        @Override public Integer doParse(String arg) { return Integer.parseInt(arg); }
    }

    public static class LongType extends CommandArgument<Long> {
        private final String description;
        public LongType(String description) { this.description = description; }
        public String getArgumentLabel() { return Long.MIN_VALUE + "-" + Long.MAX_VALUE; }
        @Override public String getDescription() { return description; }
        @Override public Long doParse(String arg) { return Long.parseLong(arg); }
    }
}