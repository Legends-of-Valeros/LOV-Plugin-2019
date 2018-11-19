package com.legendsofvaleros.modules.gear;

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

public class ItemCommands {
    @CommandManager.Cmd(cmd = "gear clear", help = "Clear the item cache.", permission = "gear.clear")
    public static CommandManager.CommandFinished cmdClear(CommandSender sender, Object[] args) {
        ItemManager.cache.invalidateAll();
        Model.cache.invalidateAll();
        MessageUtil.sendUpdate(sender, "Item and item model cache cleared.");
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "gear spawn", args = "<item name> [amount]", argTypes = {CommandManager.Arg.ArgString.class, CommandManager.Arg.ArgInteger.class}, help = "Spawn in a custom item.", permission = "gear.spawn", only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdSpawn(CommandSender sender, Object[] args) {
        ListenableFuture<GearItem> future = GearItem.fromID(String.valueOf(args[0]));

        future.addListener(() -> {
            try {
                GearItem gear = future.get();

                if (gear == null)
                    throw new Exception("That item name doesn't exist. Offender: " + String.valueOf(args[0]));

                GearItem.Instance instance = gear.newInstance();

                if (args.length > 1)
                    instance.amount = (Integer) args[1];

                ItemUtil.giveItem(Characters.getPlayerCharacter((Player) sender), instance);
            } catch (Exception e) {
                MessageUtil.sendException(Gear.getInstance(), sender, e, false);
            }
        }, Utilities.asyncExecutor());

        return CommandManager.CommandFinished.DONE;
    }
}