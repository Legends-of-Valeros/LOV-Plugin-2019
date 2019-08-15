package com.legendsofvaleros.modules.questsold.action.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;

public class ActionRemoveItem extends AbstractQuestAction {
    String itemId;
    Integer amount;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        Gear item = Gear.fromId(itemId);
        MessageUtil.sendUpdate(pc.getPlayer(), new TextBuilder("[").color(ChatColor.YELLOW)
                .append(item.getName()).color(ChatColor.GREEN)
                .append("] was removed from your inventory!").color(ChatColor.YELLOW).create());

        Gear.Instance instance = item.newInstance();
        instance.amount = amount;
        ItemUtil.removeItem(pc.getPlayer(), instance);

        next.go();
    }
}