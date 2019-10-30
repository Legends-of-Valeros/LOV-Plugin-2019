package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceEntityNode extends AbstractQuestNode<Void> {
    @SerializedName("Reference")
    public IInportObject<Void, IEntity> reference = IInportValue.of(this, IEntity.class, null);

    @SerializedName("Entity")
    public IOutportValue<Void, IEntity> entity = new IOutportValue<>(this, IEntity.class, (instance, data) ->
                                                        reference.get(instance));

    public ReferenceEntityNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}