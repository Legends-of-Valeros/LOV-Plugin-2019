package com.legendsofvaleros.modules.quests.objective.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class ReturnObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
    public String id;

    private transient NPCData npc;

    @Override
    protected void onInit() {
        if (!NPCsController.isNPC(id)) {
            MessageUtil.sendException(QuestController.getInstance(), "No NPC with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);
            return;
        }

        npc = NPCsController.getNPC(id);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return npc != null ? npc.loc : null;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return "Return to " + (npc == null ? "UNKNOWN" : npc.name);
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Returned to " + (npc == null ? "UNKNOWN" : npc.name);
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{NPCRightClickEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (id == null) return;

        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (progress.value && !e.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(id)) {
            for (IQuestObjective<?> obj : getQuest().getObjectiveGroup(pc))
                if (obj != this && !obj.isCompleted(pc))
                    return;

            progress.value = true;

            e.setCancelled(true);
        }
    }
}