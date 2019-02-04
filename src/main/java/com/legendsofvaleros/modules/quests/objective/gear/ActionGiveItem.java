package com.legendsofvaleros.modules.quests.objective.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;

public class ActionGiveItem extends AbstractQuestAction {
    String itemId;
    Integer amount;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        Gear.Instance instance = Gear.fromID(itemId).newInstance();
        instance.amount = amount == null ? 1 : amount;

        MessageUtil.sendUpdate(pc.getPlayer(), new TextBuilder("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
                .append(instance.getName()).color(ChatColor.GREEN)
                .append("]!").color(ChatColor.AQUA).create());

        ItemUtil.giveItem(pc, instance);

        next.go();
    }
}