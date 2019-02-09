package com.legendsofvaleros.modules.quests.objective.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Random;

public class InteractBlockObjective extends AbstractQuestObjective<Boolean> {
    private static final Random RAND = new Random();

    public String world;
    public int x, y, z;

    public transient Location loc;

    @Override
    protected void onInit() {
        this.loc = new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return loc;
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
        return "Right click at " + x + ", " + y + ", " + z;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Right clicked at " + x + ", " + y + ", " + z;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{ PlayerInteractEvent.class };
    }

    @Override
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        PlayerInteractEvent e = (PlayerInteractEvent) event;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return progress;

        Location bloc = e.getClickedBlock().getLocation();

        if (bloc.equals(loc)
                || bloc.clone().add(
                e.getBlockFace().getModX(),
                e.getBlockFace().getModY(),
                e.getBlockFace().getModZ()).equals(loc)) {
            e.setCancelled(true);

            return true;
        }

        return progress;
    }

    @Override
    public int getUpdateTimer() { return 4; }

    @Override
    public Boolean onUpdate(PlayerCharacter pc, Boolean progress, int ticks) {
        if(!progress) {
            pc.getPlayer().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(RAND.nextDouble(), RAND.nextDouble(), RAND.nextDouble()), 1);
        }

        return progress;
    }
}