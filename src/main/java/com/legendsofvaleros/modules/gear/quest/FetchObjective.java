package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressInteger;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class FetchObjective extends AbstractObjective<ObjectiveProgressInteger> {
    private String id;
    private int amount;

    private transient GearItem item;

    @Override
    protected void onInit() {
        ListenableFuture<GearItem> future = GearItem.fromID(id);
        future.addListener(() -> {
            try {
                item = future.get();

                if (item == null)
                    throw new Exception("No item with that ID in quest. Offender: " + id + " in " + getQuest().getId());
            } catch (Exception e) {
                MessageUtil.sendException(Gear.getInstance(), null, e, false);
            }
        }, Utilities.asyncExecutor());
    }

    @Override
    public void onBegin(PlayerCharacter pc, ObjectiveProgressInteger progress) {
        for (ItemStack stack : pc.getPlayer().getInventory().getContents()) {
            if (!item.isSimilar(stack))
                continue;
            progress.value++;
        }
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressInteger progress) {
        return progress.value >= amount;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressInteger progress) {
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
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressInteger progress) {
        if (id == null || item == null) return;

        if (event.getClass() == GearPickupEvent.class) {
            if (!item.isSimilar(((GearPickupEvent) event).getItem())) return;

            progress.value++;

        } else if (event.getClass() == PlayerDropItemEvent.class) {
            if (!item.isSimilar(((PlayerDropItemEvent) event).getItemDrop().getItemStack())) return;

            progress.value--;
        }
    }
}