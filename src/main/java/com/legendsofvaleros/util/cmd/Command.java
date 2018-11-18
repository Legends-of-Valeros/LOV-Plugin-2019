package com.legendsofvaleros.util.cmd;

import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command implements CommandExecutor, TabCompleter {
    public static final Pattern PATTERN = Pattern.compile("(?:\"(.+?)\"|(.+?))(?:\\s|$)");

    private Set<String> permissions;
    private int senders = Sender.ALL;

    private List<String> aliases = new ArrayList<>();
    public List<String> getAliases() { return aliases; }

    private Set<Command> children;
    public Set<Command> getChildren() { return children; }

    private Map<String, CommandArgument> arguments = new LinkedHashMap<>();
    public Set<Map.Entry<String, CommandArgument>> getRequiredArguments() { return arguments.entrySet(); }

    private Map<String, CommandArgument> argumentsOptional = new LinkedHashMap<>();
    public Set<Map.Entry<String, CommandArgument>> getOptionalArguments() { return argumentsOptional.entrySet(); }

    private Map<String, CommandSwitch> switches = new LinkedHashMap<>();
    public Set<Map.Entry<String, CommandSwitch>> getSwitches() { return switches.entrySet(); }

    public Command(String command, String...aliases) {
        command = command.toLowerCase();
        this.aliases.add(command);
        this.aliases.addAll(Arrays.asList(aliases));

        if (!command.equals("help")) {
            children = new HashSet<>();
            children.add(new CommandHelp(this));
        }
    }

    /**
     * If any permission is matched, the command can be run
     *
     * @param permissions The list of possible permissions to allow this command to be run
     */
    public void setPermission(String...permissions) {
        if(permissions == null || permissions.length == 0)
            throw new IllegalArgumentException("Must supply at least one permission.");
        this.permissions = new HashSet<>();
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public void setAllowedSender(int senders) {
        if(senders == 0)
            throw new IllegalArgumentException("A command must be able to be run by at least one sender.");
        this.senders = senders;
    }

    public void addArgument(String name, CommandArgument arg) {
        name = name.toLowerCase();
        arguments.put(name, arg);
    }

    public void addOptionalArgument(String name, CommandArgument arg) {
        name = name.toLowerCase();
        argumentsOptional.put(name, arg);
    }

    public <T> void addSwitch(String name, String description, T def, T set) {
        name = name.toLowerCase();
        switches.put(name, new CommandSwitch<T>() {
            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public T getDefault() { return def; }

            @Override
            public T doParse(String arg) { return set; }
        });
    }

    public void addSwitch(String name, CommandSwitch arg) {
        name = name.toLowerCase();
        switches.put(name, arg);
    }

    public void addChild(Command command) {
        if(command.aliases.contains("help"))
            throw new IllegalArgumentException("Help is a reserved child command!");
        if(children == null)
            children = new HashSet<>();
        children.add(command);
    }

    public String getCommandUsage() {
        StringBuilder str = new StringBuilder("/");

        str.append(aliases.get(0));

        for(Map.Entry<String, CommandArgument> entry : arguments.entrySet()) {
            str.append(" <");

            String label = entry.getValue().getArgumentLabel();
            str.append(label == null ? entry.getKey() : label);

            str.append(">");
        }

        for(Map.Entry<String, CommandArgument> entry : argumentsOptional.entrySet()) {
            str.append(" [");

            String label = entry.getValue().getArgumentLabel();
            str.append(label == null ? entry.getKey() : label);

            str.append("]");
        }

        for(Map.Entry<String, CommandSwitch> entry : switches.entrySet()) {
            str.append(" [");
            str.append(entry.getKey().length() == 1 ? "-" : "--");
            str.append(entry.getKey());

            if(entry.getValue().getArgumentLabel() != null) {
                str.append(" <");
                str.append(entry.getValue().getArgumentLabel());
                str.append(">");
            }

            str.append("]");
        }

        return str.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        onCommandExecuted(sender, String.join(" ", args));
        return true;
    }

    public void onCommandExecuted(CommandSender sender, String line) {
        if(senders != Sender.ALL) {
            int senderMask = 0;

            if (sender instanceof ConsoleCommandSender)
                senderMask |= Sender.CONSOLE;

            if (sender instanceof BlockCommandSender)
                senderMask |= Sender.BLOCK;

            if (sender instanceof ProxiedCommandSender)
                senderMask |= Sender.PROXIED;

            if (sender instanceof RemoteConsoleCommandSender)
                senderMask |= Sender.RCON;

            if (sender instanceof Player) {
                senderMask |= Sender.PLAYER;
            } else
                senderMask |= Sender.NON_PLAYER;

            if((senders | senderMask) != senderMask) {
                MessageUtil.sendError(sender, "You cannot run this command as a " + sender.getName() + ".");
                return;
            }
        }

        if (permissions != null && permissions.size() > 0) {
            boolean has = false;

            for (String perm : permissions)
                if (sender.hasPermission(perm)) {
                    has = true;
                    break;
                }

            if (!has)
                MessageUtil.sendError(sender, "You do not have sufficient permissions to run that command.");
        }

        Queue<String> argStack = new LinkedList<>();

        Matcher m = PATTERN.matcher(line);
        while(m.find())
            argStack.add(m.group(1) != null ? m.group(1) : m.group(2));

        onCommandExecuted(sender, argStack);
    }

    public void onCommandExecuted(CommandSender sender, Queue<String> args) {
        try {
            if (args.isEmpty()) {
                if (arguments.size() != 0)
                    MessageUtil.sendError(sender, "Usage: " + getCommandUsage());
                else
                    onCommand(new CommandContext(sender, null));
                return;
            }

            if(children != null) {
                String sub = args.peek();
                for (Command child : children) {
                    if (child.aliases.contains(sub)) {
                        args.remove();
                        child.onCommandExecuted(sender, args);
                        return;
                    }
                }
            }

            Map<String, Object> values = new HashMap<>();

            Queue<Map.Entry<String, CommandArgument>> required = new LinkedList<>(arguments.entrySet());

            Queue<Map.Entry<String, CommandArgument>> optional = new LinkedList<>(argumentsOptional.entrySet());

            switches.entrySet().forEach(entry -> values.put(entry.getKey(), entry.getValue().getDefault()));

            List<Map.Entry<String, CommandArgument>> matchedArgs = new ArrayList<>();

            String arg;
            while (!args.isEmpty() && (arg = args.peek()) != null) {
                int argSize = args.size();

                if (arg.startsWith("--")) {
                    arg = args.remove().substring(2).toLowerCase();

                    if (switches.containsKey(arg)) {
                        CommandSwitch entry = switches.get(arg);

                        values.put(arg, entry.doParse(args.remove()));

                        continue;
                    }

                    throw new IllegalArgumentException("Unknown switch: " + arg);
                } else if (arg.startsWith("-")) {
                    arg = args.remove().substring(1).toLowerCase();

                    String[] chars = arg.split("");
                    search:
                    for (String c : chars) {
                        for (Map.Entry<String, CommandSwitch> entry : switches.entrySet()) {
                            if (entry.getKey().startsWith(c)) {
                                if (entry.getValue().getArgumentLabel() != null)
                                    throw new IllegalArgumentException("The switch '" + c + "' requires an argument.");

                                values.put(entry.getKey(), entry.getValue().doParse(args.remove()));

                                continue search;
                            }
                        }

                        throw new IllegalArgumentException("Unknown switch: " + c);
                    }
                } else {
                    final Map.Entry<String, CommandArgument> entry = (
                            !required.isEmpty() ? required.remove()
                                    : !optional.isEmpty() ? optional.remove()
                                    : null
                    );

                    if (entry == null)
                        throw new IllegalArgumentException("Unexpected argument: " + arg);

                    values.put(entry.getKey(), entry.getValue().doParse(args.remove()));
                }

                if (argSize == args.size())
                    throw new IllegalStateException("Argument '" + arg + "' didn't remove anything off the queue!");
            }

            if (!required.isEmpty())
                throw new IllegalArgumentException("Did not supply enough arguments");

            onCommand(new CommandContext(sender, values));
        } catch(Exception e) {
            MessageUtil.sendError(sender, e.getMessage());
            MessageUtil.sendError(sender, "Usage: " + getCommandUsage());
        }
    }

    public abstract String getDescription();
    public abstract void onCommand(CommandContext context) throws Exception;

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }

    public void register(JavaPlugin plugin) {
        register(plugin, this);
    }

    public static void register(JavaPlugin plugin, Command command) {
        // Used to inject the command without using plugin.yml
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap)bukkitCommandMap.get(Bukkit.getServer());

            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            PluginCommand pluginCommand = c.newInstance(command.aliases.get(0), plugin);
            pluginCommand.setTabCompleter(command);
            pluginCommand.setExecutor(command);

            if(command.aliases.size() > 1) {
                List<String> aliases = new ArrayList<>(command.aliases);
                aliases.remove(0);
                pluginCommand.setAliases(aliases);
            }

            commandMap.register(command.aliases.get(0), pluginCommand);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static class Sender {
        public static int CONSOLE = 1;
        public static int BLOCK = 2;
        public static int PROXIED = 4;
        public static int RCON = 8;
        public static int PLAYER = 16;

        public static int ALL = Integer.MAX_VALUE;
        public static int NON_PLAYER = ALL ^ PLAYER;
    }
}
