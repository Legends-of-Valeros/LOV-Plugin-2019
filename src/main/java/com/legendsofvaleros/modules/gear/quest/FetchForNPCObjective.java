package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class FetchForNPCObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    private String id;
    private int amount;

    private transient GearItem item;

    private String npcId;
    private transient NPCData npc;

    @Override
    protected void onInit() {
        item = GearItem.fromID(id);

        if (item == null)
            MessageUtil.sendException(Gear.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId(), false);

        if (!NPCs.isNPC(npcId)) {
            MessageUtil.sendException(Gear.getInstance(), "No NPC with that ID in quest. Offender: " + id + " in " + getQuest().getId(), false);
            return;
        }

        npc = NPCs.getNPC(npcId);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return npc != null ? npc.loc : null;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
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
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
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