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
import com.legendsofvaleros.util.ProgressBar;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

/**
 * Created by Crystall on 11/15/2018
 */
public class LegendsOfValeros extends JavaPlugin {

    private static LegendsOfValeros instance;

    //needed for threads, so they dont continue running after shutdown
    public static boolean shutdown;
    public static long startTime = 0;


    public static LegendsOfValeros getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        shutdown = false;
        startTime = System.currentTimeMillis();



        registerModules();
        ModuleManager.loadModules();
    }

    @Override
    public void onDisable() {
        shutdown = true;
        instance = null;
        ModuleManager.unloadModules();
    }

    private void registerModules() {
        //TODO add config file and add check if module should be enabled or not
        //TODO add commands to disable single modules (&change the config?)
        //TODO add method to determine dependencies between each module (if a module depends on a disabled module, it wont be enabled either)
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

    /**
     * Creates a progressbar for the given tps
     * @param tps
     * @return
     */
    public String createTPSBar(double tps) {
        ChatColor tpsc = ChatColor.GREEN;
        if (tps < 14.5) tpsc = ChatColor.YELLOW;
        if (tps < 9) tpsc = ChatColor.GOLD;
        if (tps < 5.5) tpsc = ChatColor.RED;
        if (tps < 2.7) tpsc = ChatColor.DARK_RED;
        return ProgressBar.getBar((float) ((tps + 0.5) / 20F), 20, tpsc, ChatColor.GRAY, ChatColor.DARK_GREEN);
    }

    /**
     * Returns the current uptime of the server
     * @return
     */
    public String getUptime() {
        Date d = new Date(System.currentTimeMillis() - startTime);
        String rest = "";
        if (d.getHours() > 10) rest += "" + (d.getHours() - 1);
        else rest += "0" + (d.getHours() - 1);
        rest += ":";
        if (d.getMinutes() > 9) rest += "" + (d.getMinutes());
        else rest += "0" + (d.getMinutes());
        rest += ":";
        if (d.getSeconds() > 9) rest += "" + (d.getSeconds());
        else rest += "0" + (d.getSeconds());
        return (d.getDay() > 4 ? (d.getDay() - 4) + "" + ChatColor.GREEN + " Tage " + ChatColor.GRAY : "") + rest;
    }
}
