package com.legendsofvaleros.modules.characters.ui;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.ui.loading.ProgressView;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An abstract basis to implement a progress view.
 * <p>
 * Sanitizes, synchronizes, and simplifies potentially dangerous or nonsensical behavior from
 * clients. Stops unnecessary <code>update</code> calls where nothing has changed. Also supports
 * simple percentage-based (50% would be represented as 50.0, not 0.5) updates as an alternative to
 * x/y tasks done.
 * <p>
 * This basis is not appropriate for all implementations and is by no means essential. (This should
 * not be directly referenced as a type, instead reference the more generic
 * <code>ProgressView</code>)
 * <p>
 * This is not thread safe per se. This synchronizes requests from an arbitrary thread to the
 * server's main thread, but if the thread it is called from still needs to be consistent or it will
 * cause memory-consistency issues.
 */
public abstract class AbstractProgressView implements ProgressView {
	private final UUID pid;
	private final Player player;

	private boolean started;
	private boolean ended;
	private int totalTasks;

	private int previousCompletedTasks;

	private final BossBar bossBar;

	public AbstractProgressView(UUID player, String text, BarColor color, BarStyle style, float progress, BarFlag...flags) throws IllegalArgumentException, IllegalStateException {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		this.pid = player;

		this.player = Bukkit.getPlayer(player);
		if (this.player == null || !this.player.isOnline()) {
			throw new IllegalStateException("a player for the given UUID could not be found");
		}

		this.totalTasks = 1;
		
		this.bossBar = Bukkit.createBossBar(text, color, style, flags);
		this.bossBar.setProgress(progress);
		this.bossBar.addPlayer(this.player);
	}

	/**
	 * A safe version of a start method, run on the main thread, only called when appropriate, and
	 * with sanitized values.
	 * <p>
	 * Also includes a pre-computed percentage value for the amount of completion as an optional
	 * alternative to tasks completed / total tasks.
	 * 
	 * @param totalTasks A sanitized number of total tasks. A positive value.
	 * @param completedTasks A sanitized number of total tasks. <code>0</code> or positive and less
	 *        than or equal to the total number of tasks.
	 * @param percentageCompleted The percentage completed.
	 */
	public abstract void safeStart(int totalTasks, int completedTasks, float percentageCompleted);

	/**
	 * A safe version of an update method, run on the main thread, only called when something has
	 * actually changed, and with sanitized values.
	 * <p>
	 * Also includes a pre-computed percentage value for the amount of completion as an optional
	 * alternative to tasks completed / total tasks.
	 * 
	 * @param completedTasks A sanitized number of total tasks. <code>0</code> or positive and less
	 *        than or equal to the total number of tasks.
	 * @param percentageCompleted The percentage completed.
	 */
	public abstract void safeUpdate(int completedTasks, float percentageCompleted);

	/**
	 * A safe version of an end method, run on the main thread and only called when appropriate.
	 */
	public abstract void safeEnd();

	@Override
	public final void start(int totalTasks) {
		start(totalTasks, 0);
	}

	@Override
	public final void start(int totalTasks, int completedTasks) {
		if (started) {
			return;
		}
		started = true;

		if (totalTasks < 1) {
			totalTasks = 1;
		}
		final int safeTotal = totalTasks;
		this.totalTasks = safeTotal;

		if (completedTasks < 0) {
			completedTasks = 0;
		} else if (completedTasks > totalTasks) {
			completedTasks = totalTasks;
		}
		final int safeCompleted = completedTasks;
		this.previousCompletedTasks = safeCompleted;

		final float percentageCompleted = getPercentage(safeCompleted);

		Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> safeStart(safeTotal, safeCompleted, percentageCompleted));
	}

	@Override
	public final void update(int completedTasks) {
		if (!started || ended || completedTasks == previousCompletedTasks) {
			return;
		}

		if (completedTasks < 0) {
			completedTasks = 0;
		} else if (completedTasks > totalTasks) {
			completedTasks = totalTasks;
		}
		final int safeCompleted = completedTasks;
		this.previousCompletedTasks = safeCompleted;

		final float percentageCompleted = getPercentage(safeCompleted);

		Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> safeUpdate(safeCompleted, percentageCompleted));
	}

	@Override
	public final void end() {
		if (!started || ended) {
			return;
		}
		ended = true;

		Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> safeEnd());
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	protected final BossBar getBossBar() {
		return bossBar;
	}

	protected final Player getPlayer() {
		return player;
	}

	protected final UUID getPlayerUID() {
		return pid;
	}

	private float getPercentage(int completedTasks) {
		return ((float) completedTasks) / ((float) totalTasks);
	}
}
