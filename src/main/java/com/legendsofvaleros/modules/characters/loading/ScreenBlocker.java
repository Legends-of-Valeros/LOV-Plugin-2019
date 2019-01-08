package com.legendsofvaleros.modules.characters.loading;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Various static methods to block and unblock a player's view.
 */
public class ScreenBlocker {

  private static final int RADIUS = 2;

  private static final Material BLACK_SCREEN_MATERIAL = Material.OBSIDIAN;
  private static final byte BLACK_SCREEN_DATA = (byte) 0;

  /**
   * Blocks a player's screen.
   * <p>
   * Note that this may conflict with other packet-editing, gameplay, and general Minecraft
   * features.
   * <p>
   * The player must stay in the same location for screen blocking to work reliably.
   * 
   * @param player The player whose screen to block.
   * @param delay The number of ticks to wait before actually blocking the screen. If this is called
   *        on a player's login, this should be delayed by a 2-5 ticks. <code>0</code> for no delay.
   * @return A screen blocker that can be removed with {@link #unblockScreen()}.
   */
  public static ScreenBlocker blockScreen(Player player, long delay) {
    return new ScreenBlocker(player, delay);
  }

  /**
   * Makes a player's client think the player is surrounded in a box of a type of block.
   * 
   * @param player The player to send block change packets to.
   * @param mat The type of block. <code>null</code> to revert the blocks around the player to their
   *        real state on the server.
   * @param data The data value for the block.
   * @return A collection of all of the blocks changed.
   */
  @SuppressWarnings("deprecation")
  private static Collection<Block> surroundPlayer(Player player, Material mat, byte data) {
    List<Block> ret = new LinkedList<>();

    // TODO: Remove this. Removing the screen blocker for now in an attempt to fix the login flicker glitch
    player = null;

    if (player == null || mat == null) {
      return ret;
    }

    Block feet = player.getLocation().getBlock();
    for (int y = -1 * RADIUS - 1; y <= RADIUS + 1; y++) { // up/down
      for (int x = -1 * RADIUS; x <= RADIUS; x++) {
        for (int z = -1 * RADIUS; z <= RADIUS; z++) {
          Block block = feet.getRelative(x, y, z);
          Location loc = block.getLocation();
          player.sendBlockChange(loc, mat, data);
          ret.add(block);
        }
      }
    }

    return ret;
  }

  private final WeakReference<Player> player;
  private BukkitRunnable changeTask;
  private Collection<Block> changedBlocks;

  private ScreenBlocker(Player blockFor, long delay) {
    this.player = new WeakReference<>(blockFor);
    this.changeTask = new BukkitRunnable() {
      @Override
      public void run() {
        changedBlocks = surroundPlayer(player.get(), BLACK_SCREEN_MATERIAL, BLACK_SCREEN_DATA);
      }
    };
    if (delay <= 0) {
      changeTask.run();
    } else {
      changeTask.runTaskLater(LegendsOfValeros.getInstance(), delay);
    }
  }

  /**
   * Unblocks the player's screen or cancels the scheduled blocking, if it hasn't happened yet.
   * <p>
   * Irreversible. To reblock a player's screen, get a new blocker with
   * {@link #blockScreen(Player, long)}.
   */
  @SuppressWarnings("deprecation")
  public void unblockScreen() {
    changeTask.cancel();

    Player p = player.get();
    if (p != null && changedBlocks != null) {
      for (Block block : changedBlocks) {
        p.sendBlockChange(block.getLocation(), block.getType(), block.getData());
      }
    }

    changedBlocks = null;
  }

}
