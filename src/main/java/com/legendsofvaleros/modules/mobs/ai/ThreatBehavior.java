package com.legendsofvaleros.modules.mobs.ai;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class ThreatBehavior {
	public static final ITest HAS = (ce) -> ce.getThreat() != null && ce.getThreat().getTarget() != null && ce.getThreat().getTarget().getLivingEntity() != null;
	
	public static final ITest NEAR = (ce) -> HAS.isSuccess(ce) && ce.getLivingEntity().getLocation().distance(ce.getThreat().getTarget().getLivingEntity().getLocation()) < 2D;
	
	public static final BehaviorAction FIND = new BehaviorAction() {
		@Override
		public NodeStatus onStep(CombatEntity ce, long ticks) {
			NPC npc = NPCsController.manager().registry.getNPC(ce.getLivingEntity());
			MobTrait trait = npc.getTrait(MobTrait.class);
			List<Entity> entities = ce.getLivingEntity().getNearbyEntities(trait.instance.mob.getOptions().distance.detection, trait.instance.mob.getOptions().distance.detection, trait.instance.mob.getOptions().distance.detection);
			for(Entity entity : entities)
				if(entity instanceof LivingEntity && !NPCsController.manager().registry.isNPC(entity)) {
					if(entity.getLocation().getBlockY() - npc.getEntity().getLocation().getBlockY() >= 2)
						continue;
					if(entity instanceof Player) {
						if(((Player)entity).isSneaking()) {
							// Sneaking players have a halved detection distance
							if(entity.getLocation().distance(ce.getLivingEntity().getLocation()) > trait.instance.mob.getOptions().distance.detection / 2) {
								continue;
							}
						}
					}

					ce.getThreat().editThreat((LivingEntity)entity, 10);
					
					if(ce.getThreat().getThreat((LivingEntity)entity) > 0)
						return NodeStatus.SUCCESS;
				}

			return NodeStatus.FAIL;
		}
	};
	
	public static final BehaviorAction NAVIGATE = new BehaviorAction() {
		@Override
		public NodeStatus onStep(CombatEntity ce, long ticks) {
			NPC npc = NPCsController.manager().registry.getNPC(ce.getLivingEntity());

			npc.getNavigator().setTarget(ce.getThreat().getTarget().getLivingEntity(), false);

			/*boolean inWater = false;

			// If an NPC is in water, they need to use the citizens pathfinder,
			// otherwise they get stuck. This is not default, because they look
			// significantly more robotic.
			Block standingOn;
			for(int i = -1; i < 2; i++) {
				standingOn = ce.getLivingEntity().getWorld().getBlockAt(ce.getLivingEntity().getLocation().subtract(0, i, 0));
				if(standingOn.getType() == Material.WATER || standingOn.getType() == Material.STATIONARY_WATER) {
                    inWater = true;
                    break;
                }
			}

			if(inWater) {
                npc.getNavigator().getLocalParameters().useNewPathfinder(true);
                npc.getNavigator().setTarget(ce.getThreat().getTarget().getLivingEntity().getLocation());
            }else{
                npc.getNavigator().getLocalParameters().useNewPathfinder(false);
                npc.getNavigator().setTarget(ce.getThreat().getTarget().getLivingEntity(), false);
            }*/

			return NodeStatus.SUCCESS;
		}
	};
}