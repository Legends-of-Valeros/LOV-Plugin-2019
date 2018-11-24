package com.legendsofvaleros;

public enum ServerMode {
    DEV(true, false, true, true),
    TESTING(true, true, true, true),
    LIVE(false, true, false, false);

    boolean verbose;
    boolean logSaving;
    boolean editing;
    boolean lenient;

    ServerMode(boolean verbose, boolean logSaving, boolean editing, boolean lenient) {
        this.verbose = verbose;
        this.logSaving = logSaving;
        this.editing = editing;
        this.lenient = lenient;
    }

    /**
     * If the server should spit out additional information.
     *
     * This involves everything, not just logs.
     */
    public boolean isVerbose() { return verbose; }

    /**
     * If the server should be logSaving exceptions to the database.
     */
    public boolean doLogSaving() { return logSaving; }

    /**
     * If the server should allow editing things.
     */
    public boolean allowEditing() { return editing; }

    /**
     * A lenient server mode should enable commands that alter the game, such as
     * editing a user's stats. This should be disabled on a LIVE server to prevent
     * staff from abusing their powers to give people free stuff.
     */
    public boolean isLenient() { return lenient; }
}