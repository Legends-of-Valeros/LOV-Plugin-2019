package com.legendsofvaleros.modules.mobs.behavior;

import com.legendsofvaleros.modules.mobs.ai.AttackBehavior;
import com.legendsofvaleros.modules.mobs.ai.HomeBehavior;
import com.legendsofvaleros.modules.mobs.ai.NavigationBehavior;
import com.legendsofvaleros.modules.mobs.ai.ThreatBehavior;
import com.legendsofvaleros.modules.mobs.behavior.nodes.Node;
import com.legendsofvaleros.modules.mobs.behavior.nodes.NodeRandom;
import com.legendsofvaleros.modules.mobs.behavior.test.Condition;

public class StaticAI {
	public static final Node AGGRESSIVE = Node.create()
			.selector()
				// While has a threat
				.doWhile(ThreatBehavior.HAS)
					// If the threat is not nearby
					.is(Condition.not(ThreatBehavior.NEAR))
						.action(ThreatBehavior.NAVIGATE)
					.end()
				.end()

				.action(ThreatBehavior.FIND)
				
				.condition(Condition.not(ThreatBehavior.HAS))
					.is(Condition.not(HomeBehavior.NEAR))
						.once(HomeBehavior.NAVIGATE)
					.end()
	
					.is(HomeBehavior.NEAR)
						.node(new NodeRandom(.05F))
							.action(NavigationBehavior.WANDER)
						.done()
						.node(new NodeRandom(.1F))
							.action(NavigationBehavior.LOOK_RANDOM)
						.done()
					.end()
				.end()
			.end()

			// If near threat, attack target
			.is(ThreatBehavior.NEAR)
				.action(AttackBehavior.THREAT)
			.end();
}
