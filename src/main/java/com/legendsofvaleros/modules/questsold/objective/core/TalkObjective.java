package com.legendsofvaleros.modules.questsold.objective.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class TalkObjective extends AbstractQuestObjective<Boolean> {
    public String id;

    private transient NPCData npc;

    @Override
    protected void onInit() {
        if (!NPCsController.getInstance().isNPC(id)) {
            MessageUtil.sendException(QuestController.getInstance(), "No NPC with that ID in quest. Offender: " + id + " in " + getQuest().getId());
            return;
        }

        npc = NPCsController.getInstance().getNPC(id);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return npc != null ? npc.getLocation() : null;
    }

    @Override
    public Boolean onStart(PlayerCharacter pc) {
        return false;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Boolean progress) {
        return "Talk to " + (npc == null ? "UNKNOWN" : npc.name);
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Spoke to " + (npc == null ? "UNKNOWN" : npc.name);
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{NPCRightClickEvent.class};
    }

    @Override
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (!e.getNPC().hasTrait(TraitLOV.class)) return progress;

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(id)) {
            e.setCancelled(true);

            return true;
        }

        return progress;
    }
}