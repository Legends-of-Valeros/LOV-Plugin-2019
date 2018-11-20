package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import org.bukkit.NamespacedKey;

public class Advancements {
	public static AdvancementAPI PARENT;
	
	public static void onEnable() {
		PARENT = AdvancementAPI.builder(new NamespacedKey(LegendsOfValeros.getInstance(), "lov/parent"))
	                .title("Welcome to Valeros")
	                .description("Begin your adventure.")
	                .icon("minecraft:golden_apple")
	                .trigger(Trigger.builder(Trigger.TriggerType.LOCATION, "instant"))
	                .hidden(false)
	                .toast(false)
	                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
	                .frame(FrameType.GOAL)
                .build();
	}
}