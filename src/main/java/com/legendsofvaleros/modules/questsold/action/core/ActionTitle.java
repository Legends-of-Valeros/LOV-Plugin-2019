package com.legendsofvaleros.modules.questsold.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.action.AbstractQuestAction;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;

public class ActionTitle extends AbstractQuestAction {
    String text;
    String subtext;

    int fadeInTime;
    int stayTime;
    int fadeOutTime;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        Title title = new Title(text, subtext, fadeInTime, stayTime, fadeOutTime);
        title.setTimingsToTicks();
        title.setTitleColor(ChatColor.WHITE);
        title.setSubtitleColor(ChatColor.GRAY);
        TitleUtil.queueTitle(title, pc.getPlayer());
    }
}