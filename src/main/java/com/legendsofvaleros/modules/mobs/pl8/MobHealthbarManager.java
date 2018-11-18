package com.legendsofvaleros.modules.mobs.pl8;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class MobHealthbarManager implements Listener {
    private HashMap<UUID, MobHealth> attachedHealth;

    public MobHealthbarManager() {
        attachedHealth = new HashMap<>();

        new BukkitRunnable() {
            List<UUID> removalQueue = new ArrayList<>();

            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                for (Entry<UUID, MobHealth> c : attachedHealth.entrySet())
                    if (!c.getValue().update(ticks % 10 == 0))
                        removalQueue.add(c.getKey());

                if (removalQueue.size() > 0) {
                    for (UUID uuid : removalQueue)
                        attachedHealth.remove(uuid);
                    removalQueue.clear();
                }
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1L, 2L);
    }

    public void attachHealth(LivingEntity entity) {
        if (!attachedHealth.containsKey(entity.getUniqueId()))
            attachedHealth.put(entity.getUniqueId(), new MobHealth(entity));
    }

    @EventHandler
    public void onEntityDamaged(CombatEngineDamageEvent event) {
        if (event.getDamaged().isPlayer()) return;

        Mob.Instance spawnedEntity = Mob.Instance.get(event.getDamaged().getLivingEntity());
        if (spawnedEntity == null) {
            if (DebugFlags.get((Player) event.getAttacker().getLivingEntity()).damage)
                MessageUtil.sendDebug(event.getAttacker().getLivingEntity(), "That mob is not tracked.");
            return;
        }

        if (spawnedEntity.mob.isInvincible()) {
            if (DebugFlags.get((Player) event.getAttacker().getLivingEntity()).damage)
                MessageUtil.sendDebug(event.getAttacker().getLivingEntity(), "That mob is tagged as invincible.");
            event.setCancelled(true);
        } else if (event.getAttacker() != null && event.getAttacker().getLivingEntity() instanceof Player) {
            attachHealth(event.getDamaged().getLivingEntity());
        }
    }
}

class MobHealth {
    LivingEntity entity;
    CombatEntity ce;
    Mob.Instance instance;

    TextLine line;
    int timer = 0;

    public MobHealth(LivingEntity entity) {
        this.entity = entity;
        this.ce = CombatEngine.getEntity(entity);
        this.instance = Mob.Instance.get(entity);
    }

    public boolean update(boolean updateHealth) {
        if (line == null)
            line = Nameplates.get(entity).get(Nameplates.BASE).appendTextLine("");

        if (entity == null || entity.isDead() || timer > 20 * 15) {
            if (!line.getParent().isDeleted()) line.removeLine();
            return false;
        }

        timer++;

        if (updateHealth) {
            double health = ce.getStats().getRegeneratingStat(RegeneratingStat.HEALTH);
            double maxHealth = ce.getStats().getStat(Stat.MAX_HEALTH);
            double percent = health / maxHealth;
            int grey = (int) (percent * 30);

            StringBuilder left = new StringBuilder();
            StringBuilder right = new StringBuilder();

            for (int i = 0; i < 15; i++)
                left.append("|");
            for (int i = 0; i < 15; i++)
                right.append("|");

            StringBuilder center = new StringBuilder();
            {
                center.append(ChatColor.YELLOW);
                center.append(" [");
                center.append(ChatColor.WHITE);
                center.append((int) Math.ceil(health));
                center.append(ChatColor.YELLOW);
                center.append("] ");
            }

            if (grey < 15) {
                left.insert(grey, ChatColor.GRAY);
                right.insert(0, ChatColor.GRAY);
            } else {
                right.insert(grey - 15, ChatColor.GRAY);
                right.insert(0, ChatColor.GREEN);
            }

            line.setText(ChatColor.GREEN + left.toString() + center.toString() + right.toString());
        }

        return true;
    }
}