package com.legendsofvaleros.util.cmd;

import java.util.Map;
import java.util.Set;

public class CommandHelp extends Command {
    private final Command command;

    public CommandHelp(Command command) {
        super("help");

        this.command = command;
    }

    @Override
    public String getDescription() {
        return "Display command usage information.";
    }

    @Override
    public void onCommand(CommandContext context) {
        context.getSender().sendMessage("Usage: " + command.getCommandUsage());
        context.getSender().sendMessage(command.getDescription());

        Set<Command> children = command.getChildren();
        if(children.size() - 1 > 0) {
            context.getSender().sendMessage("");
            for(Command child : children) {
                String label = child.getAliases().get(0);
                if(label.equals("help")) continue;
                context.getSender().sendMessage(label + " " + child.getDescription());
            }
        }

        Set<Map.Entry<String, CommandArgument>> required = command.getRequiredArguments();
        if(required.size() > 0) {
            context.getSender().sendMessage("");
            for (Map.Entry<String, CommandArgument> entry : required) {
                String label = entry.getValue().getArgumentLabel();
                context.getSender().sendMessage("  " + (label == null ? entry.getKey() : label) + ": " + entry.getValue().getDescription());
            }
        }

        Set<Map.Entry<String, CommandSwitch>> switches = command.getSwitches();
        if(switches.size() > 0) {
            context.getSender().sendMessage("");
            for (Map.Entry<String, CommandSwitch> entry : switches) {
                context.getSender().sendMessage("  --" + entry.getKey() + (entry.getValue().getArgumentLabel() != null ? " <" + entry.getValue().getArgumentLabel() + ">" : "") + ": " + entry.getValue().getDescription());
            }
        }

        Set<Map.Entry<String, CommandArgument>> optional = command.getOptionalArguments();
        if(optional.size() > 0) {
            context.getSender().sendMessage("");
            context.getSender().sendMessage("Optional:");
            for (Map.Entry<String, CommandArgument> entry : optional) {
                String label = entry.getValue().getArgumentLabel();
                context.getSender().sendMessage("  " + (label == null ? entry.getKey() : label) + ": " + entry.getValue().getDescription());
            }
        }
    }
}
