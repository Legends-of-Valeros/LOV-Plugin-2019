package com.legendsofvaleros.modules.mobs;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
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
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(LevelArchetypes.class)
@DependsOn(GearController.class)
@DependsOn(LootController.class)
@DependsOn(PartiesController.class)
@DependsOn(NPCsController.class)
@ModuleInfo(name = "Mobs", info = "")
public class MobsController extends MobsAPI {
    private static MobsController instance;

    public static MobsController getInstance() {
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

        APIController.getInstance().getGsonBuilder().registerTypeAdapter(Mob.StatsMap.class, (JsonDeserializer<Mob.StatsMap>) (json, typeOfT, context) -> {
            Mob.StatsMap map = new Mob.StatsMap();

            JsonObject obj = json.getAsJsonObject();

            if(obj.has("ability")) {
                for(Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("ability").entrySet()) {
                    map.put(AbilityStat.valueOf(entry.getKey()), entry.getValue().getAsInt());
                }
            }

            if(obj.has("regenerating")) {
                for(Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("regenerating").entrySet()) {
                    map.put(RegeneratingStat.valueOf(entry.getKey()), entry.getValue().getAsInt());
                }
            }

            if(obj.has("stats")) {
                for(Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("stats").entrySet()) {
                    map.put(Stat.valueOf(entry.getKey()), entry.getValue().getAsInt());
                }
            }

            return map;
        })
        .registerTypeAdapter(Equipment.EquipmentSlot.class, (JsonDeserializer<Equipment.EquipmentSlot>) (json, typeOfT, context) -> {
            String name = json.getAsString().toUpperCase();

            if (name.equals("OFFHAND"))
                name = "OFF_HAND";

            try {
                return Equipment.EquipmentSlot.valueOf(name);
            } catch (Exception e) {
                try {
                    switch (EquipmentSlot.valueOf(name)) {
                        case HEAD:
                            return Equipment.EquipmentSlot.HELMET;
                        case CHEST:
                            return Equipment.EquipmentSlot.CHESTPLATE;
                        case LEGS:
                            return Equipment.EquipmentSlot.LEGGINGS;
                        case FEET:
                            return Equipment.EquipmentSlot.BOOTS;
                        default:
                            return null;
                    }
                } catch (Exception e1) {
                    throw new RuntimeException("Unknown equipment slot. Offender: " + name);
                }
            }
        });

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
    public void onUnload() {
        super.onUnload();

        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof LivingEntity && !(e instanceof Player)) {
                    e.remove();
                }
            }
        }
    }


}