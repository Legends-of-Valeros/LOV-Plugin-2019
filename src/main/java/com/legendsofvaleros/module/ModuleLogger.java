package com.legendsofvaleros.module;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

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

        logRecord.setMessage(this.prefix + (color != null ? color : "") + logRecord.getMessage());

        if(Bukkit.getConsoleSender() != null) {
            Bukkit.getConsoleSender().sendMessage(logRecord.getMessage());
            return;
        }

        super.log(logRecord);
    }
}
