package com.legendsofvaleros.modules.mobs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.loot.LootManager;
import com.legendsofvaleros.modules.mobs.behavior.BehaviorEngine;
import com.legendsofvaleros.modules.mobs.commands.MobCommands;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.MobSpawner;
import com.legendsofvaleros.modules.mobs.listener.ExperienceListener;
import com.legendsofvaleros.modules.mobs.listener.LootListener;
import com.legendsofvaleros.modules.mobs.listener.MobListener;
import com.legendsofvaleros.modules.mobs.pl8.MobHealthbarManager;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.parties.PartyManager;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.modules.quests.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(LevelArchetypes.class)
@DependsOn(GearController.class)
@DependsOn(LootManager.class)
@DependsOn(PartiesController.class)
@DependsOn(NPCsController.class)
public class MobsController extends Module {
    private static MobsController instance;
    public static MobsController getInstance() { return instance; }

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

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new MobCommands());

        MobsController.getInstance().registerEvents(new ExperienceListener());
        MobsController.getInstance().registerEvents(new LootListener());
        MobsController.getInstance().registerEvents(new MobListener());
        MobsController.getInstance().registerEvents(new MobHealthbarManager());

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
}