package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.core.NPCEmulator;
import com.legendsofvaleros.modules.npcs.trait.CitizensTraitLOV;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;
import com.legendsofvaleros.scheduler.InternalTask;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpeakNode extends AbstractQuestNode<Boolean> {
    Map<CharacterId, InternalTask> tasks = new HashMap<>();

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Speaker")
    public IInportReference<Boolean, INPC> speaker = IInportValue.ref(this, INPC.class);
    
    @SerializedName("Text")
    public IInportObject<Boolean, String> text = IInportValue.of(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Boolean> onExecute = IInportTrigger.async(this, (instance, data) -> {
        instance.setNodeInstance(this, false);

        INPC npc = speaker.get(instance).orElse(null);

        String line = text.get(instance);

        NPCEmulator.speak(npc, instance.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(instance.getPlayerCharacter(), line)));

        InternalTask task = QuestController.getInstance().getScheduler().executeInSpigotCircleLater(() -> {
            instance.setNodeInstance(this, true);

            onCompleted.run(instance);
        }, (int) (line.length() * 1.2));

        tasks.put(instance.getPlayerCharacter().getUniqueCharacterId(), task);
    });

    public SpeakNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @Override
    public void onActivated(IQuestInstance instance, Boolean data) {
        if(Boolean.FALSE.equals(data)) {
            instance.setNodeInstance(this, true);

            onCompleted.run(instance);
        }
    }

    @Override
    public void onDeactivated(IQuestInstance instance, Boolean data) {
        if(Boolean.FALSE.equals(data)) {
            this.tasks.remove(instance);
        }
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Boolean data, NPCRightClickEvent event) {
        // If we aren't tracking speaking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if (!event.getNPC().hasTrait(CitizensTraitLOV.class)) {
            return;
        }

        Optional<INPC> op = speaker.get(instance);
        if(!op.isPresent()) {
            return;
        }

        CitizensTraitLOV lov = event.getNPC().getTrait(CitizensTraitLOV.class);
        if (lov.getLovNPC() != op.get()) {
            return;
        }

        InternalTask task = tasks.get(instance.getPlayerCharacter().getUniqueCharacterId());
        if(task != null)
            task.run();
    }
}