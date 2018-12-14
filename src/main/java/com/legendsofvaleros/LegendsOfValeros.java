package com.legendsofvaleros;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.PaperCommandManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.dueling.Dueling;
import com.legendsofvaleros.modules.factions.FactionModule;
import com.legendsofvaleros.modules.fast_travel.FastTravel;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.graveyard.Graveyards;
import com.legendsofvaleros.modules.guilds.GuildController;
import com.legendsofvaleros.modules.hearthstones.Hearthstones;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.keepoutofocean.KeepOutOfOcean;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.loot.LootManager;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.mobs.Mobs;
import com.legendsofvaleros.modules.mount.Mounts;
import com.legendsofvaleros.modules.nanny.Nanny;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.parties.Parties;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.skills.Skills;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.ProgressBar;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Crystall on 11/15/2018
 */
public class LegendsOfValeros extends JavaPlugin {
    private static LegendsOfValeros instance;
    public static LegendsOfValeros getInstance() { return instance; }

    public static long startTime = 0;

    private ServerMode mode;
    public static ServerMode getMode() {
        return instance.mode;
    }

    private PaperCommandManager manager;
    public PaperCommandManager getCommandManager() {
        return manager;
    }

    private Map<Integer, String> loadedEventClassesName = new HashMap<>();
    private Cache<Integer, Listener> loadedEventClasses = CacheBuilder.newBuilder()
                                                                        .weakValues()
                                                                        .removalListener((v) -> {
                                                                            instance.getLogger().severe("Event listener GC'd: " + loadedEventClassesName.remove(v.getKey()));
                                                                        }).build();

    @Override
    public void onEnable() {
        instance = this;

        startTime = System.currentTimeMillis();

        mode = ServerMode.valueOf(getConfig().getString("server-mode", "LIVE"));

        getLogger().info("Server mode is set to: " + mode.name());
        if (mode.isVerbose()) getLogger().info("  - Verbosity enabled");
        if (mode.doLogSaving()) getLogger().info("  - Log saving enabled");
        if (mode.isLenient()) getLogger().warning("  - Leniency enabled: THIS SHOULD NOT BE ENABLED ON A LIVE SERVER");

        manager = new PaperCommandManager(LegendsOfValeros.getInstance());
        manager.enableUnstableAPI("help");

        try {
            Field field = JavaPlugin.class.getDeclaredField("logger");
            field.setAccessible(true);

            field = BukkitCommandManager.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(manager, getLogger());
        } catch (Exception e) { e.printStackTrace(); }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // This is done so we get almost-live updates on GC'd listeners.
            loadedEventClasses.cleanUp();
        }, 0L, 20L);

        try {
            registerModules();

            Modules.loadModules();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onDisable() {
        for(InternalScheduler scheduler : InternalScheduler.getAllSchedulers())
            scheduler.shutdown();

        Modules.unloadModules();

        loadedEventClasses.invalidateAll();
    }

    private void registerModules() throws Exception {
        // TODO add config file and add check if module should be enabled or not
        // TODO add commands to disable single modules (&change the config?)
        // Utilities is not an optional module EVER. In fact, no modules should ever
        // have define it as a dependency. Load it immediately.
        Modules.loadModuleBypass(Utilities.class);

        Modules.registerModule(Bank.class);
        Modules.registerModule(Characters.class);
        Modules.registerModule(Chat.class);
        Modules.registerModule(CombatEngine.class);
        Modules.registerModule(Dueling.class);
        Modules.registerModule(FactionModule.class);
        Modules.registerModule(FastTravel.class);
        Modules.registerModule(GearController.class);
        Modules.registerModule(Graveyards.class);
        Modules.registerModule(GuildController.class);
        Modules.registerModule(Hearthstones.class);
        Modules.registerModule(Hotswitch.class);
        Modules.registerModule(KeepOutOfOcean.class);
        Modules.registerModule(LevelArchetypes.class);
        Modules.registerModule(LootManager.class);
        Modules.registerModule(Mobs.class);
        Modules.registerModule(Mounts.class);
        Modules.registerModule(Nanny.class);
        Modules.registerModule(NPCs.class);
        Modules.registerModule(Parties.class);
        Modules.registerModule(PlayerMenu.class);
        Modules.registerModule(PvP.class);
        Modules.registerModule(Quests.class);
        Modules.registerModule(Regions.class);
        Modules.registerModule(Skills.class);
        Modules.registerModule(Zones.class);
        Modules.registerModule(AuctionController.class);
        Modules.registerModule(MailboxController.class);
    }

    public void registerEvents(Listener listener, Module module) {
        String listenerName = listener.getClass().getName().replace("com.legendsofvaleros.modules.", "") + "@" + Integer.toHexString(listener.hashCode());

        module.getLogger().info("Registered listener: " + listenerName + ".");

        // Is it possible to have hashCode collisions for non-similar classes?
        if (loadedEventClasses.getIfPresent(listener.hashCode()) != null)
            module.getLogger().severe(listenerName + " has already been registered as an event listener! This may cause unintended side effects!");
        loadedEventClasses.put(listener.hashCode(), listener);
        loadedEventClassesName.put(listener.hashCode(), listenerName);

        Bukkit.getServer().getPluginManager().registerEvents(listener, this);
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
        return ProgressBar.getBar((float) ((tps + 0.5) / 20F), 40, tpsc, ChatColor.GRAY, ChatColor.DARK_GREEN);
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
