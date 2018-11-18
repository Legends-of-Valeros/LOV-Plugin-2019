package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.util.NPCEmulator;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionSpeech extends AbstractAction {
    String npcId;
    String text;
    boolean wait;

    @Override
    public void play(Player player, Next next) {
        if (!NPCs.isNPC(npcId)) {
            MessageUtil.sendError(player, "No NPC with that name: " + npcId);
            next.go();
            return;
        }

        NPCEmulator.speak(npcId, player, ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(player, text)));

        if (wait) {
            Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), next::go, (int) (text.length() * 1.2));
        } else
            next.go();
    }
}