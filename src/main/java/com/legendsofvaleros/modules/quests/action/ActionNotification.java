package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class ActionNotification extends AbstractQuestAction {
    String icon;
    String text;

    private transient AdvancementAPI advancement;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        if (advancement == null) {
            advancement = AdvancementAPI.builder(new NamespacedKey(LegendsOfValeros.getInstance(), "quests/" + UUID.randomUUID().toString()))
                        .title(text)
                        .description("A notification")
                        .icon(icon)
                        .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
                        .hidden(true)
                        .toast(true)
                        .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                        .frame(FrameType.TASK)
                    .build();
            advancement.add();
        }

        advancement.show(pc.getPlayer());

        Quests.getInstance().getScheduler().executeInSpigotCircleLater(next::go, 20L);
    }
}