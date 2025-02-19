package com.legendsofvaleros.modules.gear.component.core;

import com.legendsofvaleros.util.StringUtil;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.NoPersist;
import org.bukkit.ChatColor;

public class LoreComponent extends GearComponent<NoPersist> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.LORE; }
	@Override public NoPersist onInit() { return null; }

	public String text;

	@Override
	public double getValue(Gear.Instance item, NoPersist persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, NoPersist persist, ItemBuilder builder) {
		for(String line : StringUtil.splitForStackLore(text))
			builder.addLore(ChatColor.YELLOW + "" + ChatColor.ITALIC + line);
	}
}