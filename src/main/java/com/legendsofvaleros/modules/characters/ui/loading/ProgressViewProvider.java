package com.legendsofvaleros.modules.characters.ui.loading;

import org.bukkit.entity.Player;

/**
 * Provides UI objects to inform of progress.
 */
public interface ProgressViewProvider {

  /**
   * Gets a progress view object to display to a player that is currently undergoing some process.
   *
   * @param player The player the progress display is for.
   * @return An object to display progress to the player via, if any. <code>null</code> if no
   * progress should be reported.
   */
  ProgressView getProgressView(Player player);

}
