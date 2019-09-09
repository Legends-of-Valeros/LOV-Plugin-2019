package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceEntityNode extends AbstractQuestNode<Void> {
    @SerializedName("Reference")
    public IInportValue<Void, String> reference = new IInportValue<>(this, String.class, null);

    @SerializedName("Entity")
    public IOutportValue<Void, Mob> entity = new IOutportValue<>(this, Mob.class, (instance, data) ->
                                                    MobsController.getInstance().getEntity(reference.get(instance)));

    public ReferenceEntityNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}