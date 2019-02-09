package com.legendsofvaleros.modules.quests.objective.mobs;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.mobs.MobManager;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.concurrent.ExecutionException;

public class KillObjective extends AbstractQuestObjective<Integer> {
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
                    MessageUtil.sendException(MobsController.getInstance(), "No instance with that ID in quest. Offender: " + id + " in " + getQuest().getId());
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
    public Integer onBegin(PlayerCharacter pc, Integer progress) {
        return 0;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Integer progress) {
        return progress >= amount;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Integer progress) {
        if (amount == 1) return "Kill " + (mob == null ? "UNKNOWN" : mob.getName());
        return progress + "/" + amount + " " + (mob == null ? "UNKNOWN" : mob.getName()) + " killed";
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
    public Integer onEvent(Event event, PlayerCharacter pc, Integer progress) {
        if (mob == null) return progress;

        CombatEngineDeathEvent e = (CombatEngineDeathEvent) event;

        if (e.getKiller() == null || !(e.getKiller().getLivingEntity() instanceof Player)) return progress;

        if (progress >= amount) return progress;

        Mob.Instance instance = Mob.Instance.get(e.getDied().getLivingEntity());
        if (mob.equals(instance.mob))
            return progress + 1;

        return progress;
    }
}