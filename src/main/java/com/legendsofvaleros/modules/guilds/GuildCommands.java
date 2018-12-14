package com.legendsofvaleros.modules.guilds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.ItemManager;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("guild|lov guild")
public class GuildCommands extends BaseCommand {
    @Subcommand("create")
    @Description("Create a new guild")
    public void cmdCreate(Player player, String name) {

    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}