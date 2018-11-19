package com.legendsofvaleros.modules;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    private final String moduleName;

    public ModuleLogger(Module module) {
        super(module.getClass().getCanonicalName(), null);

        this.moduleName = "[" + module.getName() + "] ";
        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.moduleName + logRecord.getMessage());

        super.log(logRecord);
    }
}
