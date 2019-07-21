package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class InternalLogger extends Logger {
    private String prefix = "";

    public InternalLogger() {
        super("LOV", null);
        this.setParent(LegendsOfValeros.getInstance().getServer().getLogger());
        this.setLevel(Level.ALL);
    }

    public InternalLogger(Module module) {
        this();
        prefix += "[" + module.getName() + "] ";
        this.setLevel(Level.ALL);
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        ChatColor color;
        Level level = logRecord.getLevel();

        if (level == Level.INFO) {
            color = ChatColor.WHITE;
        } else if (level == Level.FINEST) {
            color = ChatColor.AQUA;
        } else if (level == Level.FINER) {
            color = ChatColor.BLUE;
        } else if (level == Level.FINE) {
            color = ChatColor.GREEN;
        } else if (level == Level.WARNING) {
            color = ChatColor.RED;
        } else if (level == Level.SEVERE) {
            color = ChatColor.DARK_RED;
        } else {
            color = ChatColor.WHITE;
        }

        logRecord.setMessage(prefix + color + logRecord.getMessage());
        super.log(logRecord);
    }
}
