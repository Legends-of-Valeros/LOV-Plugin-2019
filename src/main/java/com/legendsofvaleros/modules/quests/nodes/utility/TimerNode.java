package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class TimerNode extends AbstractQuestNode<Void> {
    // TODO: this logic

    @SerializedName("OnTrigger")
    public IOutportTrigger<Void> onTrigger = new IOutportTrigger<>(this);
    
    @SerializedName("OnStopped")
    public IOutportTrigger<Void> onStopped = new IOutportTrigger<>(this);
    
    @SerializedName("Seconds")
    public IInportObject<Void, Integer> seconds = IInportValue.of(this, Integer.class, 0);
    
    @SerializedName("Start")
    public IInportTrigger<Void> onStart =IInportTrigger.empty(this);
    
    @SerializedName("Stop")
    public IInportTrigger<Void> onStop =IInportTrigger.empty(this);
    
    @SerializedName("Reset")
    public IInportTrigger<Void> onReset =IInportTrigger.empty(this);
    
    @SerializedName("Cancel")
    public IInportTrigger<Void> onCancel =IInportTrigger.empty(this);
    
    public TimerNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}