package com.legendsofvaleros.modules.pvp.command;

import com.legendsofvaleros.modules.pvp.toggle.PvPToggle;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggle;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggle;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDebug implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be run as player, nigga.");
            return true;
        }

        Player p = (Player) commandSender;
        commandSender.sendMessage(ChatColor.AQUA + "=== Active Toggles ===");
        for (PvPToggle toggle : PvP.getInstance().getToggles().getTogglesFor(p.getUniqueId())) {
            commandSender.sendMessage(String.format("%sPriority: %s | Enabled: %s | Honor: %d", ChatColor.AQUA, toggle.getPriority(), toggle.isEnabled(), toggle.getHonorPoints()));
        }

        PvPToggle ruling = PvP.getInstance().getToggles().getRulingToggleFor(p.getUniqueId());

        commandSender.sendMessage(String.format("%sThe Ruling Toggle | Priority: %s | Enabled: %s | Honor: %d", ChatColor.GREEN, ruling.getPriority(), ruling.isEnabled(), ruling.getHonorPoints()));
        return true;
    }
}
