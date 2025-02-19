package com.legendsofvaleros.module;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.Utilities;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Event;

public class ModuleEventTimings {

  private Map<Class<? extends Event>, Long> calls = new ConcurrentHashMap<>();
  private Table<Class<? extends Event>, Long, Long> timings = HashBasedTable.create();
  private InternalTask cleanupTask;

  public ModuleEventTimings(Module module) {
    // Clean up the timings list every 10 seconds.
    // Run this under the Utilities module so we don't pollute
    // execution counters with it.
    this.cleanupTask = (Utilities.getInstance() == null ? module : Utilities.getInstance())
        .getScheduler().executeInMyCircleTimer(() -> {
          try {
            for (Class<? extends Event> ec : getTracked()) {
              Set<Long> times = new HashSet<>(timings.row(ec).keySet());

              for (Long time : times) {
                // Keep timings or 1 minute.
                if (System.currentTimeMillis() - time > 60L * 1000L) {
                  timings.remove(ec, time);
                }
              }
            }
          } catch (ConcurrentModificationException e) {
          }
        }, 20L * 60L, 20L * 10L);
  }

  public void onUnload() {
    this.cleanupTask.cancel();
  }

  public Collection<Long> getTimings(Class<? extends Event> ec) {
    return timings.row(ec).values();
  }

  public Collection<Class<? extends Event>> getTracked() {
    return calls.keySet();
  }

  public Long getCalls(Class<? extends Event> ec) {
    return calls.get(ec);
  }


  public Long getAverageTiming(Class<? extends Event> ec) {
    Collection<Long> times = timings.row(ec).values();

    if (times.isEmpty()) {
      return null;
    }

    long avg = 0;
    for (Long timing : times) {
      avg += timing;
    }
    return avg / times.size();
  }

  public double getAverageTPSUsage(Class<? extends Event> ec) {
    Long timing = getAverageTiming(ec);
    if (timing == null) {
      return 0;
    }
    double avg = timing / (1000D / 20D);
    return (int) (avg * 10000) / 10000D;
  }

  public void calledEvent(Event e, long tookTime) {
    if (!cleanupTask.getRepeating()) {
      // If the task is not repeating, it means its been cancelled.
      // We no longer want to store statistics to prevent the stored
      // data expanding to use all available RAM.
      return;
    }

    Class<? extends Event> ec = e.getClass();

    long i = 0L;
    if (calls.containsKey(ec)) {
      i = calls.get(ec);
    }
    calls.put(ec, i + 1L);

    timings.put(ec, System.currentTimeMillis(), tookTime);
  }
}
