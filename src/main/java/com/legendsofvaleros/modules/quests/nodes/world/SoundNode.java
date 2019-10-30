package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class SoundNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Location")
    public IInportObject<Void, Vector> location = IInportValue.of(this, Vector.class, new Vector(0, 0, 0));

    @SerializedName("Relative")
    public IInportObject<Void, Boolean> relative = IInportValue.of(this, Boolean.class, false);
    
    @SerializedName("Sound")
    public IInportObject<Void, Sound> sound = IInportValue.of(this, Sound.class, null);
    
    @SerializedName("Volume")
    public IInportObject<Void, Float> volume = IInportValue.of(this, Float.class, 1F);
    
    @SerializedName("Pitch")
    public IInportObject<Void, Float> pitch = IInportValue.of(this, Float.class, 1F);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.of(this, (instance, data) -> {
        World world = relative.get(instance) ? instance.getPlayer().getWorld() : Bukkit.getWorlds().get(0);
        Location loc = (Boolean.TRUE.equals(relative.get(instance)) ? instance.getPlayer().getLocation().add(location.get(instance)) : location.get(instance).toLocation(world));
        world.playSound(loc, sound.get(instance), volume.get(instance), pitch.get(instance));

        this.onCompleted.run(instance);
    });
    
    public SoundNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}