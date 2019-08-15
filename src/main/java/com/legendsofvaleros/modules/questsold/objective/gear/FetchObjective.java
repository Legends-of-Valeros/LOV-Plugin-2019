package com.legendsofvaleros.modules.questsold.objective.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class FetchObjective extends AbstractQuestObjective<Integer> {
    private String id;
    private int amount;

    private transient Gear item;

    @Override
    protected void onInit() {
        item = Gear.fromId(id);

        if (item == null)
            MessageUtil.sendException(GearController.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId());
    }

    @Override
    public Integer onStart(PlayerCharacter pc) {
        int progress = 0;

        for (ItemStack stack : pc.getPlayer().getInventory().getContents()) {
            if (!item.isSimilar(stack))
                continue;
            progress += stack.getAmount();
        }

        return progress;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Integer progress) {
        return progress >= amount;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Integer progress) {
        return "Find " + (amount > 1 ? "x" + amount + " " : "") + (item == null ? "UNKNOWN" : item.getName());
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return (amount > 1 ? "Items retrieved!" : "Item retrieved!");
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{GearPickupEvent.class, PlayerDropItemEvent.class};
    }

    @Override
    public Integer onEvent(Event event, PlayerCharacter pc, Integer progress) {
        if (id == null || item == null) return progress;

        if (event.getClass() == GearPickupEvent.class) {
            if (!item.isSimilar(((GearPickupEvent) event).getItem())) return progress;

            return progress + 1;

        } else if (event.getClass() == PlayerDropItemEvent.class) {
            if (!item.isSimilar(((PlayerDropItemEvent) event).getItemDrop().getItemStack())) return progress;

            return progress - 1;
        }

        return progress;
    }
}