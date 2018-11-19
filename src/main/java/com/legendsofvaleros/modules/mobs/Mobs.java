package com.legendsofvaleros.modules.mobs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorEngine;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.MobSpawner;
import com.legendsofvaleros.modules.mobs.listener.ExperienceListener;
import com.legendsofvaleros.modules.mobs.listener.LootListener;
import com.legendsofvaleros.modules.mobs.listener.MobListener;
import com.legendsofvaleros.modules.mobs.pl8.MobHealthbarManager;
import com.legendsofvaleros.modules.mobs.quest.KillObjective;
import com.legendsofvaleros.modules.mobs.trait.TraitTitle;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Mobs extends ListenerModule {
    private static Mobs instance;

    public static Mobs getInstance() {
        return instance;
    }

    private BehaviorEngine ai;

    public static BehaviorEngine ai() {
        return instance.ai;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        getLogger().info("AI will update all entities over the course of " + LegendsOfValeros.getInstance().getConfig().getInt("ai-update-smear", 20) + " ticks.");
        ai = new BehaviorEngine(getConfig().getInt("ai-update-smear", 10));

        SpawnManager.onEnable();
        MobManager.onEnable();

        NPCs.registerTrait("title", TraitTitle.class);

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new MobCommands());

        Mobs.getInstance().registerEvents(new ExperienceListener());
        Mobs.getInstance().registerEvents(new LootListener());
        Mobs.getInstance().registerEvents(new MobListener());
        Mobs.getInstance().registerEvents(new MobHealthbarManager());

        ObjectiveFactory.registerType("kill", KillObjective.class);

        for (EntityType type : EntityType.values()) {
            if (type == EntityType.PLAYER || type.getEntityClass() == null)
                continue;
            if (!(LivingEntity.class.isAssignableFrom(type.getEntityClass())))
                continue;

            LevelArchetypes.getInstance().registerLevelProvider(entity -> Mob.Instance.get(entity).level, type);
        }
        new MobSpawner();
    }

    @Override
    public void onUnload() {
        super.onUnload();

        for (World world : Bukkit.getWorlds())
            for (Entity e : world.getEntities())
                if (e instanceof LivingEntity && !(e instanceof Player))
                    e.remove();

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getKiller() == null || !event.getKiller().isPlayer()) return;

        Player p = (Player) event.getKiller().getLivingEntity();

        if (!Characters.isPlayerCharacterLoaded(p)) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(p));
    }
}