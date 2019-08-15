package com.legendsofvaleros.modules.questsold.action.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.NPCEmulator;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.questsold.api.IQuestEventReceiver;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import java.util.concurrent.TimeUnit;

public class ActionSpeech extends AbstractQuestAction implements IQuestEventReceiver  {
    String npcId;
    String text;
    boolean wait;

    Cache<CharacterId, InternalTask> tasks = CacheBuilder.newBuilder()
                                    .expireAfterWrite(30L, TimeUnit.SECONDS)
                                    .build();

    @Override
    public void play(PlayerCharacter pc, Next next) {
        if (!NPCsController.getInstance().isNPC(npcId)) {
            MessageUtil.sendError(pc.getPlayer(), "No NPC with that name: " + npcId);
            next.go();
            return;
        }

        NPCEmulator.speak(npcId, pc.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(pc.getPlayer(), text)));

        if (wait) {
            tasks.put(pc.getUniqueCharacterId(), QuestController.getInstance().getScheduler().executeInSpigotCircleLater(next::go, (int) (text.length() * 1.2)));
        } else
            next.go();
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{NPCRightClickEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc) {
        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (!e.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(npcId)) {
            InternalTask task = tasks.getIfPresent(pc.getUniqueCharacterId());
            if(task != null)
                task.run();
        }
    }
}