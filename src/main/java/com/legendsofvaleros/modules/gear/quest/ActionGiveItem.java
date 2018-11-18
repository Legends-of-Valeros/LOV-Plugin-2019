package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionGiveItem extends AbstractAction {
    String itemId;
    Integer amount;

    @Override
    public void play(Player player, Next next) {
        ListenableFuture<GearItem> future = GearItem.fromID(itemId);
        future.addListener(() -> {
            try {
                GearItem.Instance instance = future.get().newInstance();
                instance.amount = amount == null ? 1 : amount;

                MessageUtil.sendUpdate(player, new FancyMessage("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
                        .then(instance.gear.getName()).color(ChatColor.GREEN)
                        .then("]!").color(ChatColor.AQUA));

                ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), player, e, false);
            }

            next.go();
        }, Utilities.asyncExecutor());
    }
}