package com.legendsofvaleros.modules.mobs.behavior.nodes;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class NodeParallel extends Node {
    public enum Orchestrator {
        /**
         * Runs all nodes in one step, ignoring returned statuses.
         */
        IGNORE,

        /**
         * Runs all possible nodes in one step.
         */
        FAST,

        /**
         * Runs one node per step in order.
         */
        LAZY
    }

    public enum Policy {
        /**
         * Ignores all child status'. Returns success when all child nodes complete.
         */
        IGNORE(null, NodeStatus.SUCCESS),

        /**
         * Fails as soon as one child fails. If all children succeed, the node succeeds.
         */
        SEQUENCE(NodeStatus.FAIL, NodeStatus.SUCCESS),

        /**
         * Succeeds as soon as one child succeeds. If all children fail, the node fails.
         */
        SELECTOR(NodeStatus.SUCCESS, NodeStatus.FAIL);

        final NodeStatus returnPassthrough, completeStatus;

        Policy(NodeStatus returnOn, NodeStatus completeStatus) {
            this.returnPassthrough = returnOn;
            this.completeStatus = completeStatus;
        }
    }

    private final Map<UUID, Integer> index = new HashMap<>();
    private final Table<UUID, Integer, NodeStatus> statuses = HashBasedTable.create();

    final Orchestrator orchestrator;
    final Policy policy;

    public NodeParallel(Orchestrator orchestrator, Policy policy) {
        this.orchestrator = orchestrator;
        this.policy = policy;
    }

    @Override
    public NodeStatus onStep(CombatEntity ce, long ticks) {
        int i = 0;
        if (index.containsKey(ce.getUniqueId())) {
            i = index.get(ce.getUniqueId());
        }

        Node node = nodes.get(i);
        NodeStatus status = node.onStep(ce, ticks);
        statuses.put(ce.getUniqueId(), i, status);

        if (status != NodeStatus.ONGOING) {
            node.onCleanup(ce);
        }

        if (orchestrator == Orchestrator.IGNORE || status != NodeStatus.ONGOING) {
            index.put(ce.getUniqueId(), ++i);
        }

        if (status == policy.returnPassthrough) {
            index.remove(ce.getUniqueId());
            return policy.returnPassthrough;
        }

        if (i >= nodes.size()) {
            index.remove(ce.getUniqueId());
            return policy.completeStatus;
        }

        switch (orchestrator) {
            case IGNORE:
                return onStep(ce, ticks);
            case FAST:
                if (status != NodeStatus.ONGOING) {
                    return onStep(ce, ticks);
                }
            case LAZY:
                return NodeStatus.ONGOING;
        }

        return NodeStatus.FAIL;
    }

    @Override
    public void onCleanup(CombatEntity ce) {
        // Clean up any marked as ONGOING.
        for (Entry<Integer, NodeStatus> entry : statuses.row(ce.getUniqueId()).entrySet()) {
            if (entry.getValue() == NodeStatus.ONGOING) {
                nodes.get(entry.getKey()).onCleanup(ce);
            }
        }

        index.remove(ce.getUniqueId());
        statuses.row(ce.getUniqueId()).clear();
    }

    @Override
    public NodeParallel clone() {
        NodeParallel branch = new NodeParallel(orchestrator, policy);

        for (Node b : branch.nodes) {
            branch.node(b.clone());
        }

        return branch;
    }
}