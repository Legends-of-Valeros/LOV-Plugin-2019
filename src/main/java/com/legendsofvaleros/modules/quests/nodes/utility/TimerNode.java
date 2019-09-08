package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class TimerNode extends AbstractQuestNode<Void> {
    @SerializedName("OnTrigger")
    public IOutportTrigger<Void> onOnTrigger = new IOutportTrigger<>(this);
    
    @SerializedName("OnStopped")
    public IOutportTrigger<Void> onOnStopped = new IOutportTrigger<>(this);
    
    @SerializedName("Start")
    public IInportTrigger<Void> onStart = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Stop")
    public IInportTrigger<Void> onStop = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Reset")
    public IInportTrigger<Void> onReset = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Cancel")
    public IInportTrigger<Void> onCancel = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Seconds")
    public IInportValue<Void, Integer> seconds = new IInportValue<>(this, Integer.class, 0);
    
    public TimerNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}