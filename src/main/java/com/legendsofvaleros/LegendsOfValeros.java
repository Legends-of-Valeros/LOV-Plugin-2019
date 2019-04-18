package com.legendsofvaleros;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.PaperCommandManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bigbrother.BigBrotherController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.cooldowns.CooldownsController;
import com.legendsofvaleros.modules.dueling.DuelingController;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.fast_travel.FastTravelController;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.graveyard.GraveyardController;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.keepoutofocean.KeepOutOfOcean;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mount.MountsController;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.professions.ProfessionsController;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.restrictions.RestrictionsController;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Crystall on 11/15/2018
 */
public class LegendsOfValeros extends JavaPlugin {
    private static LegendsOfValeros instance;
    private boolean isShutdown = false;
    public static long startTime = 0;
    private ServerMode mode;
    private PaperCommandManager manager;

    public boolean isShutdown() {
        return isShutdown;
    }

    public void setShutdown(boolean shutdown) {
        isShutdown = shutdown;
    }

    public static LegendsOfValeros getInstance() {
        return instance;
    }

    public static ServerMode getMode() {
        return instance.mode;
    }


    public PaperCommandManager getCommandManager() {
        return manager;
    }

    private Map<Integer, String> loadedEventClassesName = new HashMap<>();
    private Cache<Integer, Listener> loadedEventClasses = CacheBuilder.newBuilder()
            .weakValues()
            .removalListener(entry -> {
                instance.getLogger().severe("Event listener GC'd: " + loadedEventClassesName.remove(entry.getKey()));
            }).build();

    @Override
    public void onEnable() {
        instance = this;

        startTime = System.currentTimeMillis();
        mode = ServerMode.valueOf(getConfig().getString("server-mode", "LIVE"));

        getLogger().info("Server mode is set to: " + mode.name());
        if (mode.isVerbose()) getLogger().info("  - Verbosity enabled");
        if (mode.doLogSaving()) getLogger().info("  - Log saving enabled");
        if (mode.allowEditing())
            getLogger().warning("  - Editing enabled: THIS SHOULD NOT BE ENABLED ON A LIVE SERVER");
        if (mode.isLenient()) getLogger().warning("  - Leniency enabled: THIS SHOULD NOT BE ENABLED ON A LIVE SERVER");

        manager = new PaperCommandManager(LegendsOfValeros.getInstance());
        manager.enableUnstableAPI("help");

        try {
            Field field = JavaPlugin.class.getDeclaredField("logger");
            field.setAccessible(true);

            field = BukkitCommandManager.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(manager, getLogger());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        for (InternalScheduler scheduler : InternalScheduler.getAllSchedulers()) {
            scheduler.shutdown();
        }

        Modules.unloadModules();

        loadedEventClasses.invalidateAll();
    }

    private void registerModules() throws Exception {
        // TODO add config file and add check if module should be enabled or not
        // TODO add commands to disable single modules (&change the config?)
        // These are not optional modules EVER. In fact, no modules should ever
        // have define them as a dependency. Load them immediately.
        Modules.loadModuleBypass(APIController.class);
        Modules.loadModuleBypass(Utilities.class);

        Modules.registerModule(BankController.class);
        Modules.registerModule(BigBrotherController.class);
        Modules.registerModule(Characters.class);
        Modules.registerModule(ChatController.class);
        Modules.registerModule(CombatEngine.class);
        Modules.registerModule(CooldownsController.class);
        Modules.registerModule(DuelingController.class);
        Modules.registerModule(FactionController.class);
        Modules.registerModule(FastTravelController.class);
        Modules.registerModule(GearController.class);
        Modules.registerModule(GraveyardController.class);
        // Modules.registerModule(GuildController.class);
        Modules.registerModule(HearthstoneController.class);
        Modules.registerModule(Hotswitch.class);
        Modules.registerModule(KeepOutOfOcean.class);
        Modules.registerModule(LevelArchetypes.class);
        Modules.registerModule(LootController.class);
        Modules.registerModule(MobsController.class);
        Modules.registerModule(MountsController.class);
        // Modules.registerModule(Nanny.class);
        Modules.registerModule(NPCsController.class);
        Modules.registerModule(PartiesController.class);
        Modules.registerModule(PlayerMenu.class);
        Modules.registerModule(PvPController.class);
        Modules.registerModule(QuestController.class);
        Modules.registerModule(RegionController.class);
        Modules.registerModule(SkillsController.class);
        Modules.registerModule(ZonesController.class);
        Modules.registerModule(AuctionController.class);
        Modules.registerModule(MailboxController.class);
        Modules.registerModule(ProfessionsController.class);
        Modules.registerModule(RestrictionsController.class);
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

}
