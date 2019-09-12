package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class NotificationNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Icon")
    public IInportObject<Void, String> icon = IInportValue.of(this, String.class, null);
    
    @SerializedName("Text")
    public IInportObject<Void, String> text = IInportValue.of(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.of(this, (instance, data) -> {
        AdvancementAPI.builder(new NamespacedKey(LegendsOfValeros.getInstance(), "quests/" + UUID.randomUUID().toString()))
                .icon(icon.get(instance))
                .title(text.get(instance))
                .description("A notification")
                .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
                .hidden(true)
                .toast(true)
                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                .frame(FrameType.TASK)
            .build()
        .show(instance.getPlayer());

        onCompleted.run(instance);
    });
    
    public NotificationNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}