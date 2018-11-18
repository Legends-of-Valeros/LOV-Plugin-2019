package com.legendsofvaleros.modules;

import com.legendsofvaleros.scheduler.InternalScheduler;

public abstract class Module {

    public void onLoad() {
    }

    public void onUnload() {
    }

    /**
     * Gets the scheduler for the Module
     */
    public InternalScheduler getScheduler() {
        return ModuleManager.schedulers.get(this.getClass().getSimpleName());
    }
}