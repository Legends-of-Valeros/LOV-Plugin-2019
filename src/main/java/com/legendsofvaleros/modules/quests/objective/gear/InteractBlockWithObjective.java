package com.legendsofvaleros.modules.quests.objective.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.Random;

public class InteractBlockWithObjective extends AbstractQuestObjective<InteractBlockWithObjective.State> {
    enum State { HELD, DONE }

    private static final Random RAND = new Random();

    public String world;
    public int x, y, z;

    String itemId;

    public transient Location loc;

    private transient Gear item;

    @Override
    protected void onInit() {
        this.loc = new Location(Bukkit.getWorld(world), x, y, z);

        item = Gear.fromId(itemId);

        if (item == null)
            MessageUtil.sendException(GearController.getInstance(), "No item with that ID in quest. Offender: " + itemId + " in " + getQuest().getId());
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        return loc;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[] {
                PlayerInteractEvent.class,
                PlayerItemHeldEvent.class
        };
    }

    @Override
    public InteractBlockWithObjective.State onStart(PlayerCharacter pc) {
        Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItemInMainHand());
        if(instance == null || !item.isSimilar(instance)) return null;

        return State.HELD;
    }

    @Override
    public InteractBlockWithObjective.State onEvent(Event event, PlayerCharacter pc, InteractBlockWithObjective.State state) {
        if(state == State.DONE) return state;

        if(event instanceof PlayerItemHeldEvent) {
            Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItem(((PlayerItemHeldEvent)event).getNewSlot()));
            return item != null && item.isSimilar(instance) ? State.HELD : null;
        }else if(event instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) event;

            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return state;

            if(state != State.HELD) return state;

            Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItemInMainHand());
            if (instance == null || !item.isSimilar(instance)) return state;

            Location bloc = e.getClickedBlock().getLocation();

            if (bloc.equals(loc)
                    || bloc.clone().add(
                    e.getBlockFace().getModX(),
                    e.getBlockFace().getModY(),
                    e.getBlockFace().getModZ()).equals(loc)) {
                e.setCancelled(true);

                return State.DONE;
            }
        }

        return state;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, InteractBlockWithObjective.State state) {
        return state == State.DONE;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, InteractBlockWithObjective.State state) {
        if(state == null)
            return "Get " + (item == null ? "UNKNOWN" : item.getName());
        else
            return "Right click at " + x + ", " + y + ", " + z;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Right clicked at " + x + ", " + y + ", " + z + " with " + (item == null ? "UNKNOWN" : item.getName());
    }

    @Override
    public int getUpdateTimer() { return 4; }

    @Override
    public InteractBlockWithObjective.State onUpdate(PlayerCharacter pc, InteractBlockWithObjective.State state, int ticks) {
        if(state == State.HELD)
            pc.getPlayer().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(RAND.nextDouble(), RAND.nextDouble(), RAND.nextDouble()), 1);
        return state;
    }
}