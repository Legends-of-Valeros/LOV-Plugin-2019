package com.legendsofvaleros;

public enum ServerMode {
    LOCAL(true, false, true, true),
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
     * <p>
     * This involves everything, not just logs.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * If the server should be saving exceptions to the database.
     */
    public boolean doLogSaving() {
        return logSaving;
    }

    /**
     * If the server should allow editing things.
     */
    public boolean allowEditing() {
        return editing;
    }

    /**
     * Enables other things such as accessing server stats
     * and other information.
     */
    public boolean isLenient() {
        return lenient;
    }
}