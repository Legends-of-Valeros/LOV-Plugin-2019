package com.legendsofvaleros.modules.quests.action.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.ItemUtil;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class FetchForNPCObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
    private String id;
    private int amount;

    private transient Gear item;

    private String npcId;
    private transient NPCData npc;

    @Override
    protected void onInit() {
        item = Gear.fromID(id);

        if (item == null)
            MessageUtil.sendException(GearController.getInstance(), "No item with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);

        if (!NPCsController.isNPC(npcId)) {
            MessageUtil.sendException(GearController.getInstance(), "No NPC with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);
            return;
        }

        npc = NPCsController.getNPC(npcId);
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
        return "Bring " + npc.name + " " + (amount > 1 ? "x" + amount + " " : "") + (item == null ? "UNKNOWN" : item.getName());
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return (amount > 1 ? "Items given to " + npc.name + "!" : "Item given to " + npc.name + "!");
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{NPCRightClickEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (id == null || item == null) return;

        if (npcId == null || npc.name == null) return;

        if (progress.value) return;

        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (!e.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(npcId)) {
            if (!ItemUtil.hasItem(pc.getPlayer(), item, amount)) return;

            ItemUtil.removeItem(pc.getPlayer(), item, amount);

            progress.value = true;
        }
    }
}