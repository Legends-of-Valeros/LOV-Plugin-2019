package com.legendsofvaleros.modules.quests.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestNode extends AbstractQuestNode<TestNode.Data> {
    public class Data {
        boolean started = false;

        transient Long startTime;
        long remaining = 20L * 10L;
    }

    private static final Map<CharacterId, BukkitTask> TIMER = new HashMap<>();

    @SerializedName("Start")
    public IInportTrigger<Data> onStart = new IInportTrigger<>(this, (instance, data) -> {
        if(data.started) throw new IllegalStateException("Timer already activated.");

        data.started = true;

        onActivated(instance, data);
    });

    @SerializedName("Completed")
    public IOutportTrigger onComplete = new IOutportTrigger(this);

    public TestNode(UUID id) {
        super(id);
    }

    @Override
    public TestNode.Data newInstance() {
        return new Data();
    }

    @Override
    public void onActivated(IQuestInstance instance, TestNode.Data data) {
        // If a timer was in progress
        if(data.started) {
            // Set the start time to now
            data.startTime = instance.getPlayerCharacter().getLocation().getWorld().getFullTime();

            // Start the timer using the remaining time field.
            TIMER.put(instance.getPlayerCharacter().getUniqueCharacterId(),
                    Bukkit.getServer().getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> {
                        // Remove the tracker and reset the data.
                        TIMER.remove(instance.getPlayerCharacter().getUniqueCharacterId());

                        data.started = false;
                        data.startTime = null;

                        onComplete.run(instance);
                    }, data.remaining)
            );
        }
    }

    @Override
    public void onDeactivated(IQuestInstance instance, TestNode.Data data) {
        // Remove the task if it hasn't fired, yet.
        BukkitTask task = TIMER.remove(instance.getPlayerCharacter().getUniqueCharacterId());
        if(task != null) {
            // Subtrack the ticks elapsed from the remaining time.
            data.remaining -= instance.getPlayerCharacter().getLocation().getWorld().getFullTime() - data.startTime;

            // Ensure the task doesn't fire.
            task.cancel();
        }
    }
}