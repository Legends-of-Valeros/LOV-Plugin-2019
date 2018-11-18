package com.legendsofvaleros.modules.mobs.behavior.nodes;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public class NodeCondition extends Node {
	final ITest test;
	
	public NodeCondition(ITest test) {
		this.test = test;
	}
	
	@Override
	public NodeStatus onStep(CombatEntity ce, long ticks) {
		if(!test.isSuccess(ce)) return NodeStatus.FAIL;
		
		// Step the child, throw away the status.
		super.onStep(ce, ticks);
		
		// We don't care what the child branch returns, the condition was true so we return SUCCESS. 
		return NodeStatus.SUCCESS;
	}
	
	@Override
	public NodeCondition clone() {
		NodeCondition branch = new NodeCondition(test);
		
		for(Node b : branch.nodes)
			branch.node(b.clone());
		
		return branch;
	}
}