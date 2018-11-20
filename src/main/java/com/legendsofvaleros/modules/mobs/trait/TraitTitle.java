package com.legendsofvaleros.modules.mobs.trait;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.ChatColor;

public class TraitTitle extends LOVTrait {
	public String text;

	public transient TextLine titleLine;

	@Override
	public void onSpawn() {
		titleLine = trait.nameplates.get(Nameplates.BASE).appendTextLine(ChatColor.YELLOW + "<" + text + ">");
	}

	@Override
	public void onDespawn() {
		if(titleLine != null && !titleLine.getParent().isDeleted())
			titleLine.removeLine();
	}
}