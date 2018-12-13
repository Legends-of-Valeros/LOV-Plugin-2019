package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginLogger;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class InternalLogger extends PluginLogger {
    Field superField;

    String prefix = "";

    public InternalLogger() {
        super(LegendsOfValeros.getInstance());

        try {
            superField = PluginLogger.class.getDeclaredField("pluginName");
            superField.setAccessible(true);
            prefix += superField.get(this);
        } catch(Exception e) { e.printStackTrace(); }

        this.setLevel(Level.ALL);
    }

    public InternalLogger(Module module) {
        this();

        try {
            prefix += "[" + module.getName() + "] ";
            superField.set(this, prefix);
        } catch(Exception e) { e.printStackTrace(); }

        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        ChatColor color = null;

        if(Bukkit.getConsoleSender() != null) {
            Level level = logRecord.getLevel();

            if(level == Level.INFO) {
                color = ChatColor.WHITE;
            }else if(level == Level.FINEST) {
                color = ChatColor.AQUA;
            }else if(level == Level.FINER) {
                color = ChatColor.BLUE;
            }else if(level == Level.FINE) {
                color = ChatColor.GREEN;
            }else if(level == Level.WARNING) {
                color = ChatColor.RED;
            }else if(level == Level.SEVERE) {
                color = ChatColor.DARK_RED;
            }else{
                color = ChatColor.WHITE;
            }
        }

        logRecord.setMessage((color != null ? color : "") + logRecord.getMessage());

        if(Bukkit.getConsoleSender() != null) {
            logRecord.setMessage(prefix + logRecord.getMessage());

            Bukkit.getConsoleSender().sendMessage(logRecord.getMessage());
            return;
        }

        super.log(logRecord);
    }
}
