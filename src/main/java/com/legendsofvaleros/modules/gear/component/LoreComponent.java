package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.NoPersist;
import org.bukkit.ChatColor;

public class LoreComponent extends GearComponent<NoPersist> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.LORE; }
	@Override public NoPersist onInit() { return null; }

	public String text;

	@Override
	public double getValue(GearItem.Instance item, NoPersist persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(GearItem.Instance item, NoPersist persist, ItemBuilder builder) {
		for(String line : StringUtil.splitForStackLore(text))
			builder.addLore(ChatColor.YELLOW + "" + ChatColor.ITALIC + line);
	}
}