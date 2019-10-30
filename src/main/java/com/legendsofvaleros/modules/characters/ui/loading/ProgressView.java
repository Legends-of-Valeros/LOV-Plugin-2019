package com.legendsofvaleros.modules.characters.ui.loading;

import org.bukkit.entity.Player;

/**
 * A user interface which shows a player information it is provided about a process that progresses
 * over time.
 * <p>
 * An example of this would be a loading bar that shows how far the loading has progressed so far
 * and is updated as tasks are completed.
 * <p>
 * Views cannot generally be reused. They start, update, and end. If a view is needed again,
 * construct a new view.
 * <p>
 * Not every implementation will display all of the data it is provided. However, for the sake of
 * flexibility it is important to provide all data required by this interface so that a variety of
 * implementations can function.
 */
public interface ProgressView {

  Player getPlayer();

  /**
   * Starts the view.
   *
   * @param totalTasks The total number of tasks/things that need to complete. Should be positive.
   * Once the view is started, this cannot be changed.
   */
  void start(int totalTasks);

  /**
   * Starts the view with some progress already completed.
   *
   * @param totalTasks The total number of tasks/things that need to complete. Should be positive.
   * Once the view is started, this cannot be changed.
   * @param completedTasks The number of tasks that have completed so far. Should be <code>0</code>
   * or positive and less than or equal to the total number of tasks.
   */
  void start(int totalTasks, int completedTasks);

  /**
   * Updates the state of the view with the number of things/tasks to be completed and the number
   * that have already been completed.
   *
   * @param completedTasks The number of tasks that have completed so far. Should be <code>0</code>
   * or positive and less than or equal to the total number of tasks.
   */
  void update(int completedTasks);

  /**
   * Ends the loading screen.
   */
  void end();

  /**
   * Gets whether this view has started yet.
   *
   * @return <code>true</code> if this view has started and the player is seeing it.
   */
  boolean hasStarted();

  /**
   * Gets whether this view has ended and is now inactive.
   *
   * @return <code>true</code> if this view has ended and the player no longer sees it.
   */
  boolean hasEnded();

}
