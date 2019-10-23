package com.legendsofvaleros.modules.gear.component.core;

import com.legendsofvaleros.util.StringUtil;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.ChatColor;

public class LoreComponent extends GearComponent<Void> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.LORE; }
	@Override public Void onInit() { return null; }

	public String text;

	@Override
	public double getValue(Gear.Instance item, Void persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, Void persist, ItemBuilder builder) {
		for(String line : StringUtil.splitForStackLore(text))
			builder.addLore(ChatColor.YELLOW + "" + ChatColor.ITALIC + line);
	}
}