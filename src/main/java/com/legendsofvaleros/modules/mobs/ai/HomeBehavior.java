package com.legendsofvaleros.modules.mobs.ai;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorAction;
import com.legendsofvaleros.modules.mobs.behavior.NodeStatus;
import com.legendsofvaleros.modules.mobs.behavior.test.ITest;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCs;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

import java.util.Random;

public class HomeBehavior {
    static Random rand = new Random();

    public static final ITest NEAR = (ce) -> {
        NPC npc = NPCs.manager().registry.getNPC(ce.getLivingEntity());
        MobTrait trait = npc.getTrait(MobTrait.class);
        Location loc = trait.instance.home.getLocation();
        return npc.getEntity().getLocation().distance(loc) <= trait.instance.home.getRadius() + trait.instance.home.getPadding();
    };

    public static final BehaviorAction NAVIGATE = new BehaviorAction() {
        @Override
        public NodeStatus onStep(CombatEntity ce, long ticks) {
            NPC npc = NPCs.manager().registry.getNPC(ce.getLivingEntity());
            MobTrait trait = npc.getTrait(MobTrait.class);
            Location loc = trait.instance.home.getLocation();
            loc.add((double) trait.instance.home.getRadius() - rand.nextInt(trait.instance.home.getRadius() * 2),
                    0,
                    (double) trait.instance.home.getRadius() - rand.nextInt(trait.instance.home.getRadius() * 2));
            npc.getNavigator().setTarget(loc);
            return NodeStatus.SUCCESS;
        }
    };
}
