package com.legendsofvaleros.modules.mobs.behavior.nodes;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.nodes.NodeParallel.Orchestrator;
import com.legendsofvaleros.modules.mobs.behavior.nodes.NodeParallel.Policy;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class Node implements Cloneable {
	// The base node must always run all nodes, regardless of their return policy.
	public static Node create() { return new NodeParallel(NodeParallel.Orchestrator.IGNORE, NodeParallel.Policy.IGNORE); }

	protected Node parent;
	public final Node done() { return end(); }
	public final Node end() {
		if(parent == null)
			throw new RuntimeException("Cannot do that on the root node!");
		return parent;
	}
	
	public final List<Node> nodes = new ArrayList<>();
	
	public NodeStatus onStep(CombatEntity ce, long ticks) {
		for(Node b : nodes)
			b.onStep(ce, ticks);
		return null;
	}

	public void onCleanup(CombatEntity ce) {
		for(Node b : nodes)
			b.onCleanup(ce);
	}

	/**
	 * A lazy branch runs one sub branch per step. This could help reduce CPU load for expensive
	 * operations, but beware as it can cause "slow" AI.
	 */
	public <T extends Node> T node(T node) {
		node.parent = this;
		nodes.add(node);
		return node;
	}

	public Node program(@Nonnull Node node) { return this.branch(node); }
	public Node branch(Node node) {
		return node(node.clone());
	}

	public NodeParallel parallel(@Nonnull NodeParallel.Orchestrator orchestrator, @Nonnull NodeParallel.Policy policy) {
		return node(new NodeParallel(orchestrator, policy));
	}

	public NodeParallel lazy() { return parallel(NodeParallel.Orchestrator.LAZY, NodeParallel.Policy.IGNORE); }
	public NodeParallel sequence() { return parallel(NodeParallel.Orchestrator.LAZY, NodeParallel.Policy.SEQUENCE); }
	public NodeParallel selector() { return parallel(NodeParallel.Orchestrator.LAZY, NodeParallel.Policy.SELECTOR); }

	public Node then(@Nonnull BehaviorAction action) { return action(action); }
	public Node fire(@Nonnull BehaviorAction action) { return action(action); }
	public Node action(@Nonnull BehaviorAction action) {
		node(new NodeAction(action));
		return this;
	}
	
	/**
	 * Fires the action once per cleanup.
	 */
	public Node fireOnce(@Nonnull BehaviorAction action) { return once(action); }
	public Node once(@Nonnull BehaviorAction action) {
		node(new NodeOnceAction(action));
		return this;
	}

	public NodeLoop circuit(@Nonnull ITest test) { return this.doWhile(test); }
	public NodeLoop doWhile(@Nonnull ITest test) {
		return node(new NodeLoop(test));
	}

	// Man I love aliasing for the sake of correct grammar.
	public NodeCondition condition(@Nonnull ITest test) { return this.is(test); }
	public NodeCondition does(@Nonnull ITest test) { return this.is(test); }
	public NodeCondition is(@Nonnull ITest test) {
		return node(new NodeCondition(test));
	}
	
	@Override
	public Node clone() { return null; }
}