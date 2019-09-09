package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.mobs.api.IMob;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceEntityNode extends AbstractQuestNode<Void> {
    @SerializedName("Reference")
    public IInportValue<Void, IMob> reference = new IInportValue<>(this, IMob.class, null);

    @SerializedName("Entity")
    public IOutportValue<Void, IMob> entity = new IOutportValue<>(this, IMob.class, (instance, data) ->
                                                        reference.get(instance));

    public ReferenceEntityNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}