package com.legendsofvaleros.modules.characters.loading;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.ui.loading.ProgressView;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A phase made up of a series of asynchronous tasks. The phase is in progress while its tasks are
 * running and done when all tasks are completed.
 * <p>
 * This phase offers two methods to acquire a lock which will prevent the task from finishing.
 * <ol>
 * <li>Registering a task with the phase. When the phase starts, all of the registered tasks are run
 * asynchronously and when each completes, its hold on the phase expires.
 * <li>Acquiring a direct lock on the phase. A direct lock will hold the phase until it is manually
 * released.
 * </ol>
 * <p>
 * After the registered tasks start executing, this class periodically checks in to see how many
 * locks have been released, and updates a user interface on the progress, if one is provided.
 * 
 * @param <V> A value stored in this task phase and returned in its callback that can be used for
 *        things like identifying it and its purpose. For example, if each task phase had an
 *        <code>int</code> name number, a <code>TaskPhase&lt;Integer&gt;</code> might be used. When
 *        starting the task, the <code>Integer</code> name would be passed in, and when it called
 *        back, the <code>Integer</code> name would be in the callback and allow for the calling phase
 *        to be identified.
 */
public class TaskPhase<V> implements Callback<PhaseLock> {

  // how often to check whether all locks are released and update the user interface, in millseconds
  private static final long UPDATE_INTERVAL = 100;

  private final String name;

  private final AtomicInteger totalLocks; // all locks acquired, not just current ones.
  private final AtomicInteger locksReleased; // how many locks have been released total.
  private final Map<Integer, PhaseLock> locks;

  // tasks executed for the client by this phase (not necessarily the only tasks locking this phase.
  // Clients are able to get locks manually and decide when to release them).
  private final List<Runnable> tasks;

  private volatile boolean started; // defaults to false
  private volatile boolean complete; // defaults to false

  private ProgressView view; // UI displaying info about this phase's progress.

  private V value; // arbitrary, client-defined value, potentially used to identify this phase

  public TaskPhase(String name, ProgressView progressView) throws IllegalArgumentException {
    this(name);

    this.view = progressView;
    if (view != null && view.hasStarted()) {
      throw new IllegalArgumentException("view cannot have started");
    }
  }

  public TaskPhase(String name) throws IllegalArgumentException {
    this.name = name;

    this.totalLocks = new AtomicInteger();
    this.locksReleased = new AtomicInteger();
    this.locks = new ConcurrentHashMap<>();
    this.tasks = Collections.synchronizedList(new LinkedList<>());
  }

  /**
   * Starts the phase and any registered tasks. Also begins updating the user interface view
   * associated with this loading phase.
   * <p>
   * After the phase has started, no more tasks can be registered and no more locks can be acquired.
   * 
   * @param value The object to return with the callback.
   * @param callback The callback to inform when the phase completes. Calls back with the value it
   *        was given.
   */
  public void start(final V value, final Callback<V> callback) {
    if (started || callback == null) {
      return;
    }
    this.started = true;
    this.value = value;

    if (locks.isEmpty()) { // no locks to begin with, just calls back
      completePhase(callback);
      return;
    }

    if (view != null) {
      initializeView();
    }

    // starts all of the registered tasks
    for (final Runnable task : tasks) {
      Utilities.getInstance().getScheduler().executeInMyCircle(task);
    }

    // Cannot execute this in the scheduler, as it's a looping task.
    new Thread(() -> {
      int notifyTimer = 30000; // Ten seconds

      while (!complete) {
        if (view != null) {
          // when this runs for the first time, some locks may have already been released.
          view.update(locksReleased.get());
        }

        // when all the locks have been released, the loading phase is complete
        if (locks.isEmpty()) {
          completePhase(callback);

        } else {

          try {
            Thread.sleep(UPDATE_INTERVAL);
          } catch (Exception e) { }

          notifyTimer -= UPDATE_INTERVAL;

          if(notifyTimer <= 0) {
            StringBuilder sb = new StringBuilder("'" + name + "' phase slow, is it frozen? Unfinished tasks: ");
            for(PhaseLock lock : locks.values())
              sb.append(lock.getName() + ", ");
            MessageUtil.sendError(view.getPlayer(), sb.toString());

            notifyTimer = 30000;
          }
        }
      }
    }).start();
  }

  /**
   * Registers a task to run when the phase starts and holds the loading phase until the task has
   * completed.
   * <p>
   * Does nothing if this phase has already started or an invalid task is passed in.
   * 
   * @param task The task to run.
   * @return <code>true</code> if the task was registered successfully.
   */
  public boolean registerTask(String name, final Runnable task) {
    if (started || task == null) {
      return false;
    }

    final PhaseLock lock = getLock(name);
    // wraps the task in another runnable that will inform this class when it finishes.
    tasks.add(() -> {
      task.run();
      lock.release();
    });
    return true;
  }

  /**
   * Gets a lock for this phase. The phase concludes when all locks are released.
   * 
   * @return A lock for this phase. Returns <code>null</code> if the phase has already started.
   */
  public PhaseLock getLock(String name) {
    if (started) {
      return null;
    }

    PhaseLock lock = new PhaseLock(name, totalLocks.getAndIncrement(), this);
    locks.put(lock.getId(), lock);

    return lock;
  }

  /**
   * Gets whether there are any locks in this phase currently.
   * 
   * @return <code>true</code> if the phase has at least one manual lock or registered task.
   */
  public boolean hasLocks() {
    return !locks.isEmpty();
  }

  @Override
  public void callback(PhaseLock value, Throwable error) {
    if (value == null) {
      return;
    }

    if (locks.remove(value.getId()) != null) {
      locksReleased.incrementAndGet();
    }
  }

  private void completePhase(Callback<V> callback) {
    complete = true;

    if (view != null) {
      view.end();
    }

    callback.callback(value, null);
  }

  private void initializeView() {
    view.start(totalLocks.get(), locksReleased.get());
  }

}
