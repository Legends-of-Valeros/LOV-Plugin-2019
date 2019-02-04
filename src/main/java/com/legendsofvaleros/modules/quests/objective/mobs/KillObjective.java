package com.legendsofvaleros.modules.quests.objective.mobs;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.mobs.MobManager;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressInteger;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.concurrent.ExecutionException;

public class KillObjective extends AbstractQuestObjective<QuestObjectiveProgressInteger> {
    private String id;
    private int amount;

    private transient Mob mob;

    @Override
    protected void onInit() {
        ListenableFuture<Mob> future = MobManager.loadEntity(id);
        future.addListener(() -> {
            try {
                mob = future.get();

                if (mob == null)
                    MessageUtil.sendException(MobsController.getInstance(), "No instance with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, MobsController.getInstance().getScheduler()::async);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        if(mob == null) return null;

        Location loc = null;
        double distance = Double.MAX_VALUE;

        for (SpawnArea a : mob.getSpawns()) {
            double d = a.getLocation().distance(pc.getLocation());
            if (d < distance) {
                loc = a.getLocation();
                distance = d;
            }
        }

        return loc;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressInteger progress) {
        return progress.value >= amount;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressInteger progress) {
        if (amount == 1) return "Kill " + (mob == null ? "UNKNOWN" : mob.getName());
        return progress.value + "/" + amount + " " + (mob == null ? "UNKNOWN" : mob.getName()) + " killed";
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        if (amount == 1) return mob.getName() + " killed";
        return "Killed x" + amount + " " + mob.getName();
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{CombatEngineDeathEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressInteger progress) {
        if (mob == null) return;

        CombatEngineDeathEvent e = (CombatEngineDeathEvent) event;

        if (e.getKiller() == null || !(e.getKiller().getLivingEntity() instanceof Player)) return;

        if (progress.value >= amount) return;

        Mob.Instance instance = Mob.Instance.get(e.getDied().getLivingEntity());
        if (mob.equals(instance.mob))
            progress.value++;
    }
}