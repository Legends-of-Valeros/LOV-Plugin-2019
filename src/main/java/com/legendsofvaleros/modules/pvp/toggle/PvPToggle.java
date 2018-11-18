package com.legendsofvaleros.modules.pvp.toggle;

public class PvPToggle {

    private byte priority;
    private boolean enabled;
    private int honorPoints;

    public PvPToggle(byte priority, boolean enabled, int givesHonor) {
        this.priority = priority;
        this.enabled = enabled;
        this.honorPoints = givesHonor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte getPriority() {
        return priority;
    }

    public int getHonorPoints() {
        return honorPoints;
    }
}
