package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.util.Iterator;

public class Advancements {
	public static AdvancementAPI PARENT;
	
	public static void onEnable() {
		// Just to be safe, lets remove all existing advancements, first.
		Iterator<Advancement> i = Bukkit.advancementIterator();
		i.forEachRemaining((a) -> {
			Bukkit.getUnsafe().removeAdvancement(a.getKey());

		});

		PARENT = AdvancementAPI.builder(new NamespacedKey(LegendsOfValeros.getInstance(), "lov/parent"))
	                .title("Welcome to Valeros")
	                .description("Begin your adventure.")
	                .icon("minecraft:golden_apple")
	                .trigger(Trigger.builder(Trigger.TriggerType.LOCATION, "instant"))
	                .hidden(false)
	                .toast(true)
	                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
	                .frame(FrameType.GOAL)
                .build();
		PARENT.add();
	}
}