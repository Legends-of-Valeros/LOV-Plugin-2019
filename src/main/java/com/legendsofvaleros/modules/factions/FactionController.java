package com.legendsofvaleros.modules.factions;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.factions.listener.ReputationListener;

@DependsOn(Characters.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Factions", info = "")
public class FactionController extends FactionAPI {
    private static FactionController instance;
    public static FactionController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        registerEvents(new ReputationListener());

		/*NOTIFICATION_UP = AdvancementAPI.builder(new NamespacedKey(this, "factions/up"))
				                .title("Faction Rep+")
				                .description("Faction rep increased.")
				                .icon("minecraft:paper")
				                .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
				                .hidden(true)
				                .toast(true)
				                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
				                .frame(FrameType.TASK)
				            .build();

		NOTIFICATION_DOWN = AdvancementAPI.builder(new NamespacedKey(this, "factions/down"))
				                .title("Faction Rep-")
				                .description("Faction rep decreased.")
				                .icon("minecraft:paper")
				                .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
				                .hidden(true)
				                .toast(true)
				                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
				                .frame(FrameType.TASK)
				            .build();*/
    }
}