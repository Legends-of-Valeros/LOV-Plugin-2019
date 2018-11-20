package com.legendsofvaleros.modules.gear;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gear")
public class ItemCommands extends BaseCommand {
    @Subcommand("clear")
    @Description("Clear the item cache.")
    @CommandPermission("gear.clear")
    public void cmdClear(CommandSender sender) {
        ItemManager.reload();
        Model.reload();
        MessageUtil.sendUpdate(sender, "Item and item model cache cleared.");
    }

    @Subcommand("spawn")
    @Description("Spawn an item.")
    @CommandPermission("gear.spawn")
    @Syntax("<item id> [amount]")
    public void cmdSpawn(Player player, String id, @Optional Integer amount) {
        GearItem gear = GearItem.fromID(id);

        if (gear == null) {
            MessageUtil.sendError(player, "That item name doesn't exist.");
            return;
        }

        GearItem.Instance instance = gear.newInstance();

        instance.amount = amount != null ? amount : 1;

        ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);
    }

    @Default
    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}