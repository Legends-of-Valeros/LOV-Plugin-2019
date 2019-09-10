package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, new Vector(0, 0, 0));

    @SerializedName("Relative")
    public IInportValue<Void, Boolean> relative = new IInportValue<>(this, Boolean.class, false);
    
    @SerializedName("Particle")
    public IInportValue<Void, Particle> particle = new IInportValue<>(this, Particle.class, null);

    @SerializedName("Offset")
    public IInportValue<Void, Vector> offset = new IInportValue<>(this, Vector.class, new Vector(0, 0, 0));

    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 1);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        World world = relative.get(instance) ? instance.getPlayer().getWorld() : Bukkit.getWorlds().get(0);
        Location loc = (Boolean.TRUE.equals(relative.get(instance)) ? instance.getPlayer().getLocation().add(location.get(instance)) : location.get(instance).toLocation(world));
        world.spawnParticle(particle.get(instance), loc, count.get(instance));

        this.onCompleted.run(instance);
    });
    
    public ParticleNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}