package com.legendsofvaleros.modules.gear;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gear")
public class ItemCommands extends BaseCommand {
    @Subcommand("clear")
    @Description("Clear the item cache.")
    @CommandPermission("gear.clear")
    public void cmdClear(CommandSender sender) {
        ItemManager.cache.invalidateAll();
        Model.cache.invalidateAll();
        MessageUtil.sendUpdate(sender, "Item and item model cache cleared.");
    }

    @Subcommand("spawn")
    @Description("Spawn an item.")
    @CommandPermission("gear.spawn")
    @Syntax("<item id> [amount]")
    public void cmdSpawn(Player player, String id, @Optional Integer amount) {
        ListenableFuture<GearItem> future = GearItem.fromID(id);

        future.addListener(() -> {
            try {
                GearItem gear = future.get();

                if (gear == null)
                    throw new Exception("That item name doesn't exist. Offender: " + id);

                GearItem.Instance instance = gear.newInstance();

                instance.amount = amount != null ? amount : 1;

                ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);
            } catch (Exception e) {
                MessageUtil.sendException(Gear.getInstance(), player, e, false);
            }
        }, Utilities.asyncExecutor());
    }

    @Default
    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}