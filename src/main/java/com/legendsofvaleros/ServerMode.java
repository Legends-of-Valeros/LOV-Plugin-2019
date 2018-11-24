package com.legendsofvaleros;

public enum ServerMode {
    DEV(true, false, true),
    TESTING(true, true, true),
    LIVE(false, true, false);

    boolean verbose;
    boolean logging;
    boolean lenient;

    ServerMode(boolean verbose, boolean logging, boolean lenient) {
        this.verbose = verbose;
        this.logging = logging;
        this.lenient = lenient;
    }

    /**
     * If the server should spit out additional logging information.
     */
    public boolean doVerboseLogging() {
        return verbose;
    }

    /**
     * If the server should be logging exceptions to the database.
     */
    public boolean doLogging() {
        return logging;
    }

    /**
     * A lenient server mode should enable commands that alter the game, such as
     * editing a user's stats. This should be disabled on a LIVE server to prevent
     * staff from abusing their powers to give people free stuff.
     */
    public boolean isLenient() {
        return lenient;
    }
}