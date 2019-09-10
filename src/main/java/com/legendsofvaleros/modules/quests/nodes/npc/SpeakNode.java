package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.core.NPCEmulator;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.scheduler.InternalTask;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class SpeakNode extends AbstractQuestNode<Void> {
    Map<CharacterId, InternalTask> tasks = new HashMap<>();

    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Speaker")
    public IInportValue<Void, INPC> speaker = new IInportValue<>(this, INPC.class, null);
    
    @SerializedName("Text")
    public IInportValue<Void, String> text = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        INPC npc = speaker.get(instance);

        String line = text.get(instance);

        NPCEmulator.speak(npc, instance.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(instance.getPlayerCharacter(), line)));

        InternalTask task = QuestController.getInstance().getScheduler().executeInSpigotCircleLater(() -> {
            onCompleted.run(instance);
        }, (int) (line.length() * 1.2));

        tasks.put(instance.getPlayerCharacter().getUniqueCharacterId(), task);
    });

    public SpeakNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Void _, NPCRightClickEvent event) {
        if (!event.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = event.getNPC().getTrait(TraitLOV.class);
        if (lov.getLovNPC() != speaker.get(instance)) {
            return;
        }

        InternalTask task = tasks.get(instance.getPlayerCharacter().getUniqueCharacterId());
        if(task != null)
            task.run();
    }
}