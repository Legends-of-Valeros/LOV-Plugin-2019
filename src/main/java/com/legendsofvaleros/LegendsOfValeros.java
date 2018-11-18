package com.legendsofvaleros;

import com.legendsofvaleros.modules.ModuleManager;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.dueling.Dueling;
import com.legendsofvaleros.modules.factions.Factions;
import com.legendsofvaleros.modules.fast_travel.FastTravel;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.graveyard.Graveyards;
import com.legendsofvaleros.modules.hearthstones.Hearthstones;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.keepoutofocean.KeepOutOfOcean;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.loot.LootManager;
import com.legendsofvaleros.modules.mobs.Mobs;
import com.legendsofvaleros.modules.mount.Mounts;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.parties.Parties;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.skills.Skills;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Crystall on 11/15/2018
 */
public class LegendsOfValeros extends JavaPlugin {

    private static LegendsOfValeros instance;

    public static LegendsOfValeros getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        registerModules();
        ModuleManager.loadModules();
    }

    @Override
    public void onDisable() {
        instance = null;
        ModuleManager.unloadModules();
    }

    private void registerModules() {
        ModuleManager.registerModule(new Utilities());
        ModuleManager.registerModule(new PlayerMenu());
        ModuleManager.registerModule(new NPCs());
        ModuleManager.registerModule(new CombatEngine());
        ModuleManager.registerModule(new KeepOutOfOcean());
        ModuleManager.registerModule(new LevelArchetypes());
        ModuleManager.registerModule(new Characters());
        ModuleManager.registerModule(new Quests());
        ModuleManager.registerModule(new Hearthstones());
        ModuleManager.registerModule(new Factions());
        ModuleManager.registerModule(new Regions());
        ModuleManager.registerModule(new Chat());
        ModuleManager.registerModule(new Hotswitch());
        ModuleManager.registerModule(new Parties());
        ModuleManager.registerModule(new Gear());
        ModuleManager.registerModule(new LootManager());
        ModuleManager.registerModule(new Bank());
        ModuleManager.registerModule(new PvP());
        ModuleManager.registerModule(new Mobs());
        ModuleManager.registerModule(new Zones());
        ModuleManager.registerModule(new Skills());
        ModuleManager.registerModule(new Mounts());
        ModuleManager.registerModule(new FastTravel());
        ModuleManager.registerModule(new Graveyards());
        ModuleManager.registerModule(new Dueling());
    }

}
