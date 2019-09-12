package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IInportTrigger<T> implements INodeInput<IOutportTrigger<?>> {
    public static <T> IInportTrigger<T> empty(IQuestNode<T> node) {
        return new IInportTrigger<>(null, node);
    }

    public static <T> IInportTrigger<T> of(IQuestNode<T> node, @NotNull INodeRunnable<T> runnable) {
        return new IInportTrigger<>(null, node, runnable);
    }

    /**
     * Runs the trigger in an async thread. If already in an async thread, this does not swap threads. This should be
     * used if you're getting the value of an IInportReference so it doesn't lock the main thread if it must be loaded.
     */
    public static <T> IInportTrigger<T> async(IQuestNode<T> node, @NotNull INodeRunnable<T> runnable) {
        return new IInportTrigger<>(true, node, runnable);
    }

    public static <T> IInportTrigger<T> sync(IQuestNode<T> node, @NotNull INodeRunnable<T> runnable) {
        return new IInportTrigger<>(false, node, runnable);
    }

    final Boolean async;

    final IQuestNode<T> node;

    IOutportTrigger<?> port;

    final INodeRunnable<T> runnable;

    public IInportTrigger(Boolean async, IQuestNode<T> node) {
        this.async = async;

        this.node = node;
        this.runnable = INodeRunnable.NOTHING;
    }

    public IInportTrigger(Boolean async, IQuestNode<T> node, @NotNull INodeRunnable<T> runnable) {
        this.async = async;

        this.node = node;
        this.runnable = runnable;
    }

    @Override
    public void setConnection(IOutportTrigger<?> port) {
        this.port = port;
    }

    @Override
    public Optional<IOutportTrigger<?>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public void run(IQuestInstance instance) {
        // Respect inactive quests
        if(!instance.getState().isActive()) {
            return;
        }

        // Swap to sync or async
        if(async == null) {

        }else if(async && Bukkit.isPrimaryThread()) {
            QuestController.getInstance().getScheduler().async(() -> run(instance));
            return;
        }else if(!async && !Bukkit.isPrimaryThread()) {
            QuestController.getInstance().getScheduler().sync(() -> run(instance));
            return;
        }

        this.runnable.run(instance, instance.getNodeInstance(node));
    }
}