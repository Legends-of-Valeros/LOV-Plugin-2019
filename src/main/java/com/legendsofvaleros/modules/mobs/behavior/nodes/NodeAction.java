package com.legendsofvaleros.modules.mobs.behavior.nodes;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public class NodeAction extends Node {
	final BehaviorAction action;
	
	public NodeAction(BehaviorAction action) {
		this.action = action;
	}

	public NodeStatus onStep(CombatEntity ce, long ticks) {
		NodeStatus status = action.onStep(ce, ticks);
		
		if(status == NodeStatus.SUCCESS)
			super.onStep(ce, ticks);
		
		return status;
	}

	public void onCleanup(CombatEntity ce) {
		action.onTerminate(ce);
	}
	
	@Override
	public NodeAction clone() {
		NodeAction branch = new NodeAction(action);
		
		for(Node b : branch.nodes)
			branch.node(b.clone());
		
		return branch;
	}
}