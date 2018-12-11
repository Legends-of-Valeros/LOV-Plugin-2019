package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.util.NPCEmulator;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionSpeech extends AbstractQuestAction {
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
            Quests.getInstance().getScheduler().executeInSpigotCircleLater(next::go, (int) (text.length() * 1.2));
        } else
            next.go();
    }
}