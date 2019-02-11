package com.legendsofvaleros.modules.mobs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.loot.LootController;
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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(LevelArchetypes.class)
@DependsOn(GearController.class)
@DependsOn(LootController.class)
@DependsOn(PartiesController.class)
@DependsOn(NPCsController.class)
@ModuleInfo(name = "Mobs", info = "")
public class MobsController extends Module {
    private static MobsController instance;
    public static MobsController getInstance() { return instance; }

    private MobsAPI api;
    public MobsAPI getApi() { return api; }

    private BehaviorEngine ai;

    public static BehaviorEngine ai() {
        return instance.ai;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.api = new MobsAPI();

        getLogger().info("AI will update all entities over the course of " + LegendsOfValeros.getInstance().getConfig().getInt("ai-update-smear", 20) + " ticks.");
        ai = new BehaviorEngine(getConfig().getInt("ai-update-smear", 10));

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
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.api.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
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