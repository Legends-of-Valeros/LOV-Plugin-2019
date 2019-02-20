package com.legendsofvaleros.modules.mobs.ai;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

import java.util.Random;

public class NavigationBehavior {
	static Random rand = new Random();
	
	public static final ITest HAS_TARGET = (ce) -> {
		NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
		return npc.getNavigator().getTargetAsLocation() != null;
	};
	
	public static final ITest NEAR_TARGET = (ce) -> {
		NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
		return ce.getLivingEntity().getLocation().distance(npc.getNavigator().getTargetAsLocation()) < 2D;
	};

	public static final BehaviorAction WANDER = new BehaviorAction() {
		@Override
		public NodeStatus onStep(CombatEntity ce, long ticks) {
			NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
			MobTrait trait = npc.getTrait(MobTrait.class);
			Location spawn = trait.instance.home.getLocation();
			
			Location loc;
			do {
				loc = npc.getEntity().getLocation().clone();
				loc.add(rand.nextInt(10) - 5, 0, rand.nextInt(10) - 5);
			} while(loc.distance(spawn) > trait.instance.home.getRadius() + trait.instance.home.getPadding());
			npc.getNavigator().setTarget(loc);
			
			return NodeStatus.SUCCESS;
		}
	};

	public static final BehaviorAction LOOK_RANDOM = new BehaviorAction() {
		@Override
		public NodeStatus onStep(CombatEntity ce, long ticks) {
			NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
			Location loc = npc.getEntity().getLocation().clone();
			loc.add(rand.nextInt(10) - 5, rand.nextInt(3) - 1.5D, rand.nextInt(10) - 5);
			npc.faceLocation(loc);
			
			return NodeStatus.SUCCESS;
		}
	};
}