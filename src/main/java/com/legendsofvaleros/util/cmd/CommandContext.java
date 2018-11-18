package com.legendsofvaleros.util.cmd;

import org.bukkit.command.CommandSender;

import java.util.Map;

public class CommandContext {
    private CommandSender sender;
    public CommandSender getSender() { return sender; }

    private Map<String, Object> args;
    public Object getArgument(String key) { return args.get(key); }
    public <T> T getArgument(Class<T> type, String key) { return type.cast(args.get(key)); }

    public CommandContext(CommandSender sender, Map<String, Object> args) {
        this.sender = sender;
        this.args = args;
    }
}