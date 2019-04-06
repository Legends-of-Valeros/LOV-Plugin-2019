package com.legendsofvaleros.modules.mobs.ai;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.DamageHistory;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

import static org.bukkit.Sound.ENTITY_ENDERMEN_TELEPORT;


public class ThreatBehavior {
    public static final ITest HAS = (ce) -> ce.getThreat() != null && ce.getThreat().getTarget() != null && ce.getThreat().getTarget().getLivingEntity() != null;

    public static final ITest NEAR = (ce) -> HAS.isSuccess(ce) && ce.getLivingEntity().getLocation().distance(ce.getThreat().getTarget().getLivingEntity().getLocation()) < 2D;

    public static final BehaviorAction FIND = new BehaviorAction() {
        @Override
        public NodeStatus onStep(CombatEntity ce, long ticks) {
            NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
            MobTrait trait = npc.getTrait(MobTrait.class);
            List<Entity> entities = ce.getLivingEntity().getNearbyEntities(trait.instance.mob.getOptions().distance.detection, trait.instance.mob.getOptions().distance.detection, trait.instance.mob.getOptions().distance.detection);
            for (Entity entity : entities)
                if (entity instanceof LivingEntity && !NPCsController.getInstance().isNPC((LivingEntity) entity)) {
                    if (entity instanceof Player) {
                        if (((Player) entity).getGameMode() != GameMode.SURVIVAL) {
                            continue;
                        }
                        if (((Player) entity).isSneaking()) {
                            // Sneaking players have a halved detection distance
                            if (entity.getLocation().distance(ce.getLivingEntity().getLocation()) > trait.instance.mob.getOptions().distance.detection / 2) {
                                continue;
                            }
                        }
                    }

                    ce.getThreat().editThreat((LivingEntity) entity, 10);

                    if (ce.getThreat().getThreat((LivingEntity) entity) > 0) {
                        return NodeStatus.SUCCESS;
                    }
                }

            return NodeStatus.FAIL;
        }
    };

    public static final BehaviorAction NAVIGATE = new BehaviorAction() {

        @Override
        public NodeStatus onStep(CombatEntity ce, long ticks) {
            NPC npc = NPCsController.getInstance().getNPC(ce.getLivingEntity());
            MobTrait trait = npc.getTrait(MobTrait.class);
            LivingEntity target = ce.getThreat().getTarget().getLivingEntity();

            npc.getNavigator().setTarget(target, false);

            //handle safe spotting
            if (target.getLocation().distanceSquared(ce.getLivingEntity().getLocation()) > 2 &&
                    Math.abs(target.getLocation().getBlockY() - npc.getEntity().getLocation().getBlockY()) > 2) {

                CharacterId characterId = Characters.getPlayerCharacter(target.getUniqueId()).getUniqueCharacterId();

                //check if the player has damaged the mob recently
                DamageHistory history = CombatEngine.getInstance().getDamageHistory(ce.getLivingEntity());
                if (history != null) {
                    if (history.didDamage(target)) {
                        if (!trait.instance.mob.getOptions().leashed.containsKey(characterId)) {
                            trait.instance.mob.getOptions().leashed.put(characterId, System.currentTimeMillis() / 1000L);
                        } else {
                            long since = trait.instance.mob.getOptions().leashed.get(characterId);

                            if ((System.currentTimeMillis() / 1000L) - since > 3) {
                                // Safespotting where the mob cannot reach the player's Y level. Teleport this mob to the player.
                                ce.getLivingEntity().teleport(((LivingEntity) target).getEyeLocation());

                                //visual and sound effects for teleporting
                                ce.getLivingEntity().getWorld().playEffect(ce.getLivingEntity().getLocation(), Effect.ENDEREYE_LAUNCH, 1);
                                ce.getLivingEntity().getWorld().playSound(ce.getLivingEntity().getLocation(), ENTITY_ENDERMEN_TELEPORT, 1, 1);

                                //reset the timer
                                trait.instance.mob.getOptions().leashed.put(characterId, System.currentTimeMillis() / 1000L);
                            }
                        }
                    }
                }
            }

            return NodeStatus.SUCCESS;
        }
    };
}