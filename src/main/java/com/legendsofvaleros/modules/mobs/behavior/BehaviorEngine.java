package com.legendsofvaleros.modules.mobs.behavior;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.Mobs;
import com.legendsofvaleros.modules.mobs.behavior.nodes.Node;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BehaviorEngine {
    private static class BehaviorData {
        final CombatEntity ce;
        final Node tree;

        BehaviorData(CombatEntity ce, Node tree) {
            this.ce = ce;
            this.tree = tree;
        }
    }

    private final List<BehaviorData> entities = new ArrayList<>(1024);

    public void bind(CombatEntity ce, Node branch) {
        entities.add(new BehaviorData(ce, branch));
    }

    private final int allUpdateInterval;

    public BehaviorEngine(int allUpdateInterval) {
        this.allUpdateInterval = allUpdateInterval;

        new BukkitRunnable() {
            private long time = 0;

            @Override
            public void run() {
                updateAI(time++);
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1, 1);
    }

    int block, blockSize;
    BehaviorData data;

    public void updateAI(long time) {
        block = (int) (time % allUpdateInterval);
        blockSize = (int) Math.ceil((double) entities.size() / allUpdateInterval);
        for (int i = block * blockSize; i < entities.size() && i < block * blockSize + blockSize; i++) {
            data = entities.get(i);

            if (!data.ce.isActive() || data.ce.getLivingEntity() == null || data.ce.getLivingEntity().isDead()) {
                entities.remove(i--);
                continue;
            }

            try {
                data.tree.onStep(data.ce, time);
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, e, true);
            }
        }
    }
}
