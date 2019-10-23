package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.util.StringUtil;
import com.legendsofvaleros.features.gui.item.Book;
import com.legendsofvaleros.modules.characters.testing.StatDisplay;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SkillInsight extends AdminSkill {
	public static final String ID = "admin-insight";
	private static final Object[] DESCRIPTION = new Object[] {
			"View the base stats of any entity."
		};

	public SkillInsight() { super(ID, Material.BOOK, Type.NEUTRAL, null, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Insight"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		CombatEntity target = getTarget(ce, 50);
		if(target == null) return false;

		Book book = new Book("Entity Information", "Acolyte");

		TextBuilder tb;
		StringBuilder tooltip = new StringBuilder();

		StatDisplay.IStatDisplay disp;
		for (Stat.Category cat : Stat.Category.values()) {
			tb = new TextBuilder(StringUtil.center(Book.WIDTH, cat.getUserFriendlyName() + " Stats") + "\n\n").color(ChatColor.BLACK).underlined(true);
			for (Stat s : Stat.values()) {
				if (s.getCategory() != cat) continue;

				tooltip.append(ChatColor.GRAY);
				tooltip.append(String.join("\n", StringUtil.splitForStackLore(CombatEngine.getEngineConfig().getStatDescription(s))));

				if ((disp = StatDisplay.getFor(s)) != null) {
					tooltip.append("\n\n");
					tooltip.append(ChatColor.GOLD);
					tooltip.append(ChatColor.BOLD);
					tooltip.append("Per Point:");
					tooltip.append(ChatColor.WHITE);

					for (String str : disp.getPerPoint()) {
						tooltip.append("\n ");
						tooltip.append(str);
					}
				}

				tb.append(s.getShortName() + ":").color(ChatColor.BLACK)
						.hover(s.getUserFriendlyName(), "", tooltip.toString().trim())
						.append(StringUtil.right(Book.WIDTH, s.getShortName(), s.formatForUserInterface(target.getStats().getStat(s))) + "\n").color(ChatColor.DARK_GRAY);

				tooltip.setLength(0);
			}

			book.addPage(tb.create());
		}

		book.open((Player)ce.getLivingEntity(), false);

		return true;
	}
}