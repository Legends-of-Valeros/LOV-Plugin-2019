package com.legendsofvaleros.modules.quests.objective.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class FetchForNPCObjective extends AbstractQuestObjective<Boolean> {
    private String id;
    private int amount;

    private transient Gear item;

    private String npcId;
    private transient NPCData npc;

    @Override
    protected void onInit() {
        item = Gear.fromId(id);

        if (item == null)
            MessageUtil.sendException(GearController.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId());

        if (!NPCsController.getInstance().isNPC(npcId)) {
            MessageUtil.sendException(GearController.getInstance(), "No NPC with that ID in quest. Offender: " + id + " in " + getQuest().getId());
            return;
        }

        npc = NPCsController.getInstance().getNPC(npcId);
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
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        if (id == null || item == null) {
            return progress;
        }

        if (npcId == null || npc.name == null) {
            return progress;
        }

        if (progress) {
            return progress;
        }

        NPCRightClickEvent e = (NPCRightClickEvent) event;

        if (!e.getNPC().hasTrait(TraitLOV.class)) {
            return progress;
        }

        TraitLOV lov = e.getNPC().getTrait(TraitLOV.class);
        if (lov.npcId != null && lov.npcId.equals(npcId)) {
            if (!ItemUtil.hasItem(pc.getPlayer(), item, amount)) {
                return progress;
            }

            Gear.Instance instance = item.newInstance();
            instance.amount = amount;
            ItemUtil.removeItem(pc.getPlayer(), instance);

            return true;
        }

        return progress;
    }
}