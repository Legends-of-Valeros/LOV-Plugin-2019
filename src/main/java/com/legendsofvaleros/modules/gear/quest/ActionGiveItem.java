package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionGiveItem extends AbstractAction {
    String itemId;
    Integer amount;

    @Override
    public void play(Player player, Next next) {
        GearItem.Instance instance = GearItem.fromID(itemId).newInstance();
        instance.amount = amount == null ? 1 : amount;

        MessageUtil.sendUpdate(player, new FancyMessage("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
                .then(instance.gear.getName()).color(ChatColor.GREEN)
                .then("]!").color(ChatColor.AQUA));

        ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);
    }
}