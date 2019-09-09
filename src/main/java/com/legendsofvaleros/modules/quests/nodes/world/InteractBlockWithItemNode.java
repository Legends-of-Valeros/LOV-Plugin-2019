package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;
import com.legendsofvaleros.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InteractBlockWithItemNode extends AbstractQuestNode<Boolean> {
    InternalTask task;
    Queue<IQuestInstance> instances;

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        return "Node<" + getClass().getSimpleName() + ">";
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Location")
    public IInportValue<Boolean, Vector> location = new IInportValue<>(this, Vector.class, null);
    
    @SerializedName("Item")
    public IInportValue<Boolean, IGear> item = new IInportValue<>(this, IGear.class, null);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        instance.setNodeInstance(this, false);
    });

    public InteractBlockWithItemNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @Override
    public void onWake() {
        this.instances = new ConcurrentLinkedQueue<>();

        this.task = QuestController.getInstance().getScheduler().executeInSpigotCircleTimer(() -> {
            for(IQuestInstance instance : instances) {
                Location loc = location.get(instance).toLocation(instance.getPlayer().getLocation().getWorld());
                instance.getPlayer().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(Math.random(), Math.random(), Math.random()), 1);
            }
        }, 0L, 10L);
    }

    @Override
    public void onActivated(IQuestInstance instance, Boolean data) {
        if(Boolean.FALSE.equals(data) && isCorrectItem(instance, instance.getPlayer().getInventory().getItemInMainHand())) {
            this.instances.add(instance);
        }
    }

    @Override
    public void onDeactivated(IQuestInstance instance, Boolean data) {
        this.instances.remove(instance);
    }

    @Override
    public void onSleep() {
        this.task.cancel();
        this.instances = null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, PlayerItemHeldEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if(!isCorrectItem(instance, instance.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            // We are no longer holding the required item, so don't show the particles
            this.instances.remove(instance);
        }else{
            // We are holding the item, spawn the particles
            this.instances.add(instance);
        }
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, PlayerInteractEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // If the held item is incorrect
        if (!isCorrectItem(instance, instance.getPlayer().getInventory().getItemInMainHand())) return;

        Location bloc = event.getClickedBlock().getLocation();
        Location target = location.get(instance).toLocation(instance.getPlayer().getWorld());

        if (!bloc.equals(target)
                || !bloc.clone().add(
                event.getBlockFace().getModX(),
                event.getBlockFace().getModY(),
                event.getBlockFace().getModZ()).equals(target)) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }

    private boolean isCorrectItem(IQuestInstance instance, ItemStack stack) {
        Gear.Instance gearInstance = Gear.Instance.fromStack(stack);
        return (gearInstance != null && gearInstance.gear.isSimilar(item.get(instance)));
    }
}