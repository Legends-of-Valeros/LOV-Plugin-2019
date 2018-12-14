package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionGiveItem extends AbstractQuestAction {
    String itemId;
    Integer amount;

    @Override
    public void play(Player player, Next next) {
        Gear.Instance instance = Gear.fromID(itemId).newInstance();
        instance.amount = amount == null ? 1 : amount;

        MessageUtil.sendUpdate(player, new TextBuilder("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
                .append(instance.getName()).color(ChatColor.GREEN)
                .append("]!").color(ChatColor.AQUA).create());

        ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);
    }
}