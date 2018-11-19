package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    private String prefix;

    public ModuleLogger(Module module) {
        super(module.getClass().getCanonicalName(), null);

        prefix = LegendsOfValeros.getInstance().getDescription().getPrefix();
        prefix = prefix != null ? "[" + prefix + "] " : "[" + LegendsOfValeros.getInstance().getDescription().getName() + "] ";
        prefix += "[" + module.getName() + "] ";

        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.prefix + logRecord.getMessage());

        super.log(logRecord);
    }
}
