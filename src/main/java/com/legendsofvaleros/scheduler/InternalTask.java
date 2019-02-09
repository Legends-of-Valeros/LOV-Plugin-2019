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

    private Runnable command;
    public Runnable getCommand() { return command; }
    public void setCommand(Runnable command) {
        this.name = command.getClass().getName();
        this.command = command;
    }

    private String name;
    public String getName() { return name; }

    private final String trace;
    public String getTrace() { return trace; }

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

        setCommand(command);
    }

    public void run() {
        if(command == null)
            return;

        // Don't run a non-repeating task additional times
        if(!repeat && executed)
            return;

        if(executor != null) {
            if(sync)
                executor.totalS++;
            else
                executor.totalA++;
        }

        executed = true;

        try {
            command.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        setRepeating(false);
    }

    @Override
    public String toString() {
        return "InternalTask{name=" + name + ",sync=" + sync + ",repeat=" + repeat + ",ri=" + ri + ",tick=" + net + ",executed=" + executed +"}";
    }
}
