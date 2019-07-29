package com.legendsofvaleros.modules.gear.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gear|lov gear")
public class ItemCommands extends BaseCommand {
    @Subcommand("reload")
    @Description("Reload the item cache.")
    @CommandPermission("gear.reload")
    public void cmdReload(CommandSender sender) {
        GearController.getInstance().loadAll();
        Model.loadAll();

        MessageUtil.sendUpdate(sender, "Item and item model cache reloaded.");
    }

    @Subcommand("spawn")
    @Description("Spawn an item.")
    @CommandPermission("gear.spawn")
    public void cmdSpawn(Player player, String itemId, @Optional Integer amount) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Gear gear = Gear.fromId(itemId);

        if (gear == null) {
            MessageUtil.sendError(player, "That item name doesn't exist.");
            return;
        }

        Gear.Instance instance = gear.newInstance();
        instance.amount = amount != null ? amount : 1;
        ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);

        MessageUtil.sendUpdate(player, "Spawned: " + gear.getName());
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}