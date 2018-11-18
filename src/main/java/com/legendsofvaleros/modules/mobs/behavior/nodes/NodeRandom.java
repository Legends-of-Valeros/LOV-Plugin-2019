package com.legendsofvaleros.modules.mobs.behavior.nodes;

import java.util.Random;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public class NodeRandom extends Node {
	static Random rand = new Random();
	
	final float chance;
	
	public NodeRandom(float chance) {
		this.chance = chance;
	}
	
	@Override
	public NodeStatus onStep(CombatEntity ce, long ticks) {
		if(rand.nextFloat() > chance) return NodeStatus.FAIL;
		return super.onStep(ce, ticks);
	}
	
	@Override
	public NodeRandom clone() {
		NodeRandom branch = new NodeRandom(chance);
		
		for(Node b : branch.nodes)
			branch.node(b.clone());
		
		return branch;
	}
}