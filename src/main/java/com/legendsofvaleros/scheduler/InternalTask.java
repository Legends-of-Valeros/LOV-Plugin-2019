package com.legendsofvaleros.scheduler;

import com.legendsofvaleros.util.MessageUtil;

/*
 * Created by Crystall on 10/10/2018
 * Represents an executing thread for a module
 */
public class InternalTask {
    private InternalScheduler executor = null;
    public InternalScheduler getExecutor() {
        return executor;
    }
    public void setExecutor(InternalScheduler executor) {
        this.executor = executor;
    }

    private final String trace;
    public String getTrace() { return trace; }

    private Runnable command;
    public Runnable getCommand() { return command; }
    public void setTask(Runnable command) {
        this.command = command;
    }

    private boolean sync = false;
    public boolean isSync() {
        return sync;
    }
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    private boolean executed = false;
    public boolean wasExecuted() {
        return executed;
    }
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    private boolean repeat = false;
    public boolean getRepeating() {
        return repeat;
    }
    public void setRepeating(boolean repeat) { this.repeat = repeat; }

    private long net = 0;
    public long nextExecuteTick() { return net; }
    public void setNextExecuteTick(long l) { net = l; }

    private long ri = 1;
    public long getRepeatingInterval() { return ri; }
    public void setRepeatingInterval(long i) { ri = i; }

    public InternalTask(Runnable command) {
        this.trace = MessageUtil.getStackTrace(new Throwable("Created InternalTask"));

        this.command = command;
    }

    public void run() {
        if(command == null)
            throw new RuntimeException("Attempted to run() an empty InternalTask.");

        if(executor != null) {
            if(sync)
                executor.totalS++;
            else
                executor.totalA++;
        }

        command.run();
    }

    public void cancel() {
        setRepeating(false);
    }
}
