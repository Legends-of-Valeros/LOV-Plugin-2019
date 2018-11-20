package com.legendsofvaleros.modules.mobs.behavior.nodes;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NodeOnceAction extends Node {
	final BehaviorAction action;

	private final Set<UUID> fired = new HashSet<>();
	
	public NodeOnceAction(BehaviorAction action) {
		this.action = action;
	}

	public NodeStatus onStep(CombatEntity ce, long ticks) {
		if(fired.contains(ce.getUniqueId())) return NodeStatus.FAIL;
		
		fired.add(ce.getUniqueId());
		
		NodeStatus status = action.onStep(ce, ticks);
		
		if(status == NodeStatus.SUCCESS)
			super.onStep(ce, ticks);
		
		return status;
	}

	public void onCleanup(CombatEntity ce) {
		action.onTerminate(ce);
		
		fired.remove(ce.getUniqueId());
	}
	
	@Override
	public NodeOnceAction clone() {
		NodeOnceAction branch = new NodeOnceAction(action);
		
		for(Node b : branch.nodes)
			branch.node(b.clone());
		
		return branch;
	}
}