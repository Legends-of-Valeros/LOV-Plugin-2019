package com.legendsofvaleros.modules.mobs.ai;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.gear.component.core.GearUseSpeed;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AttackBehavior {
    static final Random rand = new Random();

    static final Cache<UUID, Long> swingTime = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();

    public static final BehaviorAction THREAT = new BehaviorAction() {
        @Override
        public @Nonnull NodeStatus onStep(CombatEntity ce, long ticks) {
            Long time = swingTime.getIfPresent(ce.getLivingEntity().getUniqueId());

            if (time == null || time - System.currentTimeMillis() <= 0) {
                time = System.currentTimeMillis() + 1000L;

                Gear.Instance gear = Gear.Instance.fromStack(ce.getLivingEntity().getEquipment().getItemInMainHand());
                if (gear != null) {
                    GearUseSpeed.Persist use = gear.getPersist(GearUseSpeed.Component.class);
                    if (use != null)
                        time = System.currentTimeMillis() + (long)(use.speed * 1000L);
                }

                time += (long) (1000L * rand.nextDouble());

                LivingEntity target = ce.getThreat().getTarget().getLivingEntity();

                NPC npc = NPCsController.manager().registry.getNPC(ce.getLivingEntity());
                npc.faceLocation(target.getEyeLocation());
                MobTrait trait = npc.getTrait(MobTrait.class);

                // This might be null.
                EntityClass ec = trait.instance.mob.getEntityClass();

                CombatEngine.getInstance().causePhysicalDamage(target,
                        ce.getLivingEntity(), PhysicalType.MELEE,
                        ec == null ? 1 : Characters.getInstance().getCharacterConfig().getClassConfig(ec).getBaseMeleeDamage(),
                        ce.getLivingEntity().getLocation(), true, true);

                if (ce.getThreat().getTarget() != null && ce.getThreat().getTarget().isPlayer()) {
                    EntityPlayer p = ((CraftPlayer) target).getHandle();
                    if (p != null) {
                        PacketPlayOutAnimation animationPacket = new PacketPlayOutAnimation(((CraftEntity) ce.getLivingEntity()).getHandle(), 0);
                        p.playerConnection.sendPacket(animationPacket);
                    }
                }

                swingTime.put(ce.getLivingEntity().getUniqueId(), time);

                return NodeStatus.SUCCESS;
            }

            return NodeStatus.FAIL;
        }
    };
}