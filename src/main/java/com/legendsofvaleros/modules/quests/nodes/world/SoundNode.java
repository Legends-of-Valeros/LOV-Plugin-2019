package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class SoundNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, new Vector(0, 0, 0));

    @SerializedName("Relative")
    public IInportValue<Void, Boolean> relative = new IInportValue<>(this, Boolean.class, false);
    
    @SerializedName("Sound")
    public IInportValue<Void, Sound> sound = new IInportValue<>(this, Sound.class, null);
    
    @SerializedName("Volume")
    public IInportValue<Void, Float> volume = new IInportValue<>(this, Float.class, 1F);
    
    @SerializedName("Pitch")
    public IInportValue<Void, Float> pitch = new IInportValue<>(this, Float.class, 1F);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        World world = instance.getPlayer().getWorld();
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