package com.legendsofvaleros.modules.quests.objective;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class TalkObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    public String id;

    private transient NPCData npc;

    @Override
    protected void onInit() {
        if (!NPCs.isNPC(id)) {
            MessageUtil.sendException(Quests.getInstance(), null, new Exception("No NPC with that ID in quest. Offender: " + id + " in " + getQuest().getId()), false);
            return;
        }

        npc = NPCs.getNPC(id);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return npc != null ? npc.loc : null;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{NPCRightClickEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (!e.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(id)) {
            progress.value = true;

            e.setCancelled(true);
        }
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return "Talk to " + npc.name;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Spoke to " + npc.name;
    }
}