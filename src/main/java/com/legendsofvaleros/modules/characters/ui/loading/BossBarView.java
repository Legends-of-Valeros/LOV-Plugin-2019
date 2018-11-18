package com.legendsofvaleros.modules.characters.ui.loading;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import com.legendsofvaleros.modules.characters.ui.AbstractProgressView;

/**
 * A loading screen view for a single player that turns their screen black while loading and shows a
 * loading bar (or "boss bar") that tracks the progress of the loading process.
 * <p>
 * Relies on the <code>BarAPI</code> library by confuserr, or a fork of that library.
 */
public class BossBarView extends AbstractProgressView {

	private static final float MINIMUM_PERCENT = 0f;
	private static final float MAXIMUM_PERCENT = 1f;

	public BossBarView(UUID player, String message, BarColor color, BarStyle style) throws IllegalArgumentException, IllegalStateException {
		super(player, ChatColor.GRAY + "" + ChatColor.ITALIC + message, color, style, 0F);
	}

	@Override
	public void safeStart(int totalTasks, int completedTasks, float percentageCompleted) {
		getBossBar().setProgress(sanitizePercentage(percentageCompleted));
	}

	@Override
	public void safeUpdate(int completedTasks, float percentageCompleted) {
		getBossBar().setProgress(sanitizePercentage(percentageCompleted));
	}

	@Override
	public void safeEnd() {
		getBossBar().setProgress(MAXIMUM_PERCENT);
		getBossBar().removePlayer(getPlayer());
	}

	private float sanitizePercentage(float percentage) {
		if(percentage < MINIMUM_PERCENT)
			return MINIMUM_PERCENT;
		else if(percentage > MAXIMUM_PERCENT)
			return MAXIMUM_PERCENT;
		return percentage;
	}
}