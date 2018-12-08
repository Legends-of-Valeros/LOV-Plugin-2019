package com.legendsofvaleros.modules.regions;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.Map.Entry;

public class RegionManager implements Listener {
    private static String REGION_ID = "region_id";

    private static String REGION_TABLE = "regions";

    private static String REGION_WORLD = "region_world";

    private static String REGION_START_X = "region_start_x";
    private static String REGION_START_Y = "region_start_y";
    private static String REGION_START_Z = "region_start_z";

    private static String REGION_END_X = "region_end_x";
    private static String REGION_END_Y = "region_end_y";
    private static String REGION_END_Z = "region_end_z";

    private static String REGION_ALLOW_ACCESS = "region_allow_access";
    private static String REGION_ALLOW_HEARTHSTONE = "region_allow_hearthstone";

    private static String REGION_MSG_ENTER = "region_msg_enter";
    private static String REGION_MSG_EXIT = "region_msg_exit";
    private static String REGION_MSG_ERROR = "region_msg_error";

    private static String PLAYER_REGIONS_TABLE = "player_region";
    private static String CHARACTER_FIELD = "character_id";
    private static String ACCESS_LEVEL = "can_access";

    private static final List<Region> EMPTY_LIST = new ArrayList<>();

    private TableManager manager;
    private TableManager managerPlayers;

    private Map<String, IRegionType> regionTypes = new HashMap<>();

    public void registerType(String region_type, IRegionType type) {
        regionTypes.put(region_type, type);
    }

    /**
     * A map who's key is a chunk x,y pair and the regions inside it. Allows for extremely fast region searching.
     */
    private Multimap<String, String> regionChunks = HashMultimap.create();

    public List<Region> findRegions(Location location) {
        String chunkId = location.getChunk().getX() + "," + location.getChunk().getZ();
        if (!regionChunks.containsKey(chunkId))
            return EMPTY_LIST;

        List<Region> foundRegions = new ArrayList<>();

        for (String regionId : regionChunks.get(chunkId)) {
            Region region = regions.get(regionId);

            if (region.isInside(location))
                foundRegions.add(region);
        }

        return foundRegions;
    }

    private HashMap<String, Region> regions = new HashMap<>();

    public Region getRegion(String region_id) {
        return regions.get(region_id);
    }

    private Multimap<Player, String> playerRegions = HashMultimap.create();

    public Collection<String> getPlayerRegions(Player p) {
        return playerRegions.get(p);
    }

    public Table<CharacterId, String, Boolean> playerAccess = HashBasedTable.create();

    public RegionManager() {
        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), REGION_TABLE);

        manager.primary(REGION_ID, "VARCHAR(32)")
                .column(REGION_WORLD, "VARCHAR(16)")

                .column(REGION_START_X, "INT(11)")
                .column(REGION_START_Y, "INT(11)")
                .column(REGION_START_Z, "INT(11)")

                .column(REGION_END_X, "INT(11)")
                .column(REGION_END_Y, "INT(11)")
                .column(REGION_END_Z, "INT(11)")

                .column(REGION_ALLOW_ACCESS, "BOOLEAN")
                .column(REGION_ALLOW_HEARTHSTONE, "BOOLEAN")

                .column(REGION_MSG_ENTER, "TEXT")
                .column(REGION_MSG_EXIT, "TEXT")
                .column(REGION_MSG_ERROR, "TEXT").create();

        managerPlayers = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), PLAYER_REGIONS_TABLE);

        managerPlayers.primary(CHARACTER_FIELD, "VARCHAR(38)")
                .primary(REGION_ID, "VARCHAR(64)")
                .column(ACCESS_LEVEL, "BOOLEAN").create();

        Regions.getInstance().registerEvents(this);

        loadRegions();
    }

    @EventHandler
    public void onPlayerRemoved(PlayerCharacterRemoveEvent e) {
        managerPlayers.query()
                .remove()
                .where(CHARACTER_FIELD, e.getPlayerCharacter().getUniqueCharacterId().toString())
                .build()
                .execute(true);
    }

    public void setRegionAccessibility(PlayerCharacter pc, String region, boolean accessible) {
        boolean current = (playerAccess.contains(pc.getUniqueCharacterId(), region) ? playerAccess.get(pc.getUniqueCharacterId(), region) : false);
        if (accessible == current) return;

        playerAccess.put(pc.getUniqueCharacterId(), region, accessible);

        if (!accessible)
            managerPlayers.query()
                    .remove()
                    .where(CHARACTER_FIELD, pc.getUniqueCharacterId().toString(),
                            REGION_ID, region)
                    .build()
                    .execute(true);
        else
            managerPlayers.query()
                    .insert()
                    .values(CHARACTER_FIELD, pc.getUniqueCharacterId().toString(),
                            REGION_ID, region,
                            ACCESS_LEVEL, accessible)
                    .build()
                    .execute(true);
    }

    public void loadRegionsForPlayer(PlayerCharacter pc, PhaseLock lock) {
        managerPlayers.query()
                .select()
                .where(CHARACTER_FIELD, pc.getUniqueCharacterId().toString())
                .build()
                .callback((result) -> {
                    while (result.next()) {
                        playerAccess.put(pc.getUniqueCharacterId(), result.getString(REGION_ID), result.getBoolean(ACCESS_LEVEL));
                    }

                    lock.release();
                })
                .execute(true);
    }

    @EventHandler
    public void onPlayerLoading(PlayerCharacterStartLoadingEvent event) {
        loadRegionsForPlayer(event.getPlayerCharacter(), event.getLock("Regions"));
    }

    @EventHandler
    public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
        playerRegions.removeAll(event.getPlayer());
        Regions.selection.remove(event.getPlayer());

        playerAccess.row(event.getPlayerCharacter().getUniqueCharacterId()).clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            return;

        List<Region> toRegions = findRegions(event.getTo());
        if (toRegions.size() > 0) {
            if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
                MessageUtil.sendError(event.getPlayer(), toRegions.get(0).msgFailure);
                event.getPlayer().teleport(event.getFrom());
                return;
            }

            PlayerCharacter pc = Characters.getPlayerCharacter(event.getPlayer());
            for (Region region : toRegions) {
                if (!playerAccess.contains(pc.getUniqueCharacterId(), region.id)
                        || !playerAccess.get(pc.getUniqueCharacterId(), region.id)) {
                    MessageUtil.sendError(event.getPlayer(), region.msgFailure);
                    event.getPlayer().teleport(event.getFrom());
                    return;
                }

                break;
            }
        }

        List<Region> discrepancies = findRegions(event.getFrom());

        for (Region region : toRegions) {
            discrepancies.remove(region);

            if (playerRegions.containsEntry(event.getPlayer(), region.id))
                continue;

            if (Regions.REGION_DEBUG)
                event.getPlayer().sendMessage("Entered region: " + region.id);

            if (region.msgEnter != null && region.msgEnter.length() > 0)
                MessageUtil.sendInfo(event.getPlayer(), region.msgEnter);

            playerRegions.put(event.getPlayer(), region.id);
            Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(event.getPlayer(), region));
        }

        // Lessens checks for users in no region
        for (Region region : discrepancies) {
            if (Regions.REGION_DEBUG)
                event.getPlayer().sendMessage("Left region: " + region.id);

            if (region.msgExit != null && region.msgExit.length() > 0)
                MessageUtil.sendInfo(event.getPlayer(), region.msgExit);

            playerRegions.remove(event.getPlayer(), region.id);
            Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(event.getPlayer(), region));
        }
    }

    private void loadRegions() {
        regionChunks.clear();
        regions.clear();

        manager.query()
                .select().build()
                .callback((result) -> {
                    while (result != null && result.next()) {
                        Region region = new Region(result.getString(REGION_ID),
                                Bukkit.getWorld(result.getString(REGION_WORLD)),
                                new RegionBounds().setBounds(
                                        result.getInt(REGION_START_X),
                                        result.getInt(REGION_START_Y),
                                        result.getInt(REGION_START_Z),
                                        result.getInt(REGION_END_X),
                                        result.getInt(REGION_END_Y),
                                        result.getInt(REGION_END_Z)));

                        region.allowAccess = result.getBoolean(REGION_ALLOW_ACCESS);
                        region.allowHearthstone = result.getBoolean(REGION_ALLOW_HEARTHSTONE);

                        region.msgEnter = result.getString(REGION_MSG_ENTER);
                        region.msgExit = result.getString(REGION_MSG_EXIT);
                        region.msgFailure = result.getString(REGION_MSG_ERROR);

                        addRegion(region, false);
                    }
                })
                .execute(false);
    }

    public void updateRegion(String region_id) {
        if (!regions.containsKey(region_id))
            return;

        updateRegion(regions.get(region_id));
    }

    public void updateRegion(Region region) {
        manager.query()
                .insert()
                .values(REGION_ID, region.id,
                        REGION_WORLD, region.world.getName(),

                        REGION_START_X, region.getBounds().getStartX(),
                        REGION_START_Y, region.getBounds().getStartY(),
                        REGION_START_Z, region.getBounds().getStartZ(),

                        REGION_END_X, region.getBounds().getEndX(),
                        REGION_END_Y, region.getBounds().getEndY(),
                        REGION_END_Z, region.getBounds().getEndZ(),

                        REGION_ALLOW_ACCESS, region.allowAccess,
                        REGION_ALLOW_HEARTHSTONE, region.allowHearthstone,

                        REGION_MSG_ENTER, region.msgEnter,
                        REGION_MSG_EXIT, region.msgExit,
                        REGION_MSG_ERROR, region.msgFailure)
                .onDuplicateUpdate(REGION_ID, REGION_WORLD,

                        REGION_START_X, REGION_START_Y, REGION_START_Z,
                        REGION_END_X, REGION_END_Y, REGION_END_Z,

                        REGION_ALLOW_ACCESS, REGION_ALLOW_HEARTHSTONE,

                        REGION_MSG_ENTER, REGION_MSG_EXIT, REGION_MSG_ERROR)
                .build()
                .execute(true);
    }

    public void addRegion(Region region, boolean triggerUpdate) {
        if (regions.containsKey(region.id))
            return;

        if (region.world == null) {
            MessageUtil.sendException(Regions.getInstance(), "Region has a null world. Offender: " + region.id, false);
            return;
        }

        regions.put(region.id, region);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x <= bounds.getEndX(); x++)
            for (int y = bounds.getStartY(); y <= bounds.getEndY(); y++)
                for (int z = bounds.getStartZ(); z <= bounds.getEndZ(); z++) {
                    Chunk chunk = region
                            .world
                            .getChunkAt(
                                    new Location(region
                                            .world, x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (!regionChunks.containsEntry(chunkId, region.id))
                        regionChunks.put(chunkId, region.id);
                }

        if (triggerUpdate)
            updateRegion(region.id);
    }

    public void removeRegion(String region_id) {
        if (!regions.containsKey(region_id))
            return;

        Region region = regions.get(region_id);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x < bounds.getEndX(); x++)
            for (int y = bounds.getStartY(); y < bounds.getEndY(); y++)
                for (int z = bounds.getStartZ(); z < bounds.getEndZ(); z++) {
                    Chunk chunk = region.world.getChunkAt(new Location(region.world, x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (regionChunks.containsEntry(chunkId, region))
                        regionChunks.remove(chunkId, region.id);
                }

        for (Entry<Player, String> e : playerRegions.entries())
            if (region.id.equals(e.getValue()))
                playerRegions.remove(e.getKey(), e.getValue());

        regions.remove(region_id);

        manager.query()
                .remove()
                .where(REGION_ID, region_id)
                .limit(1)
                .build()
                .execute(true);
    }
}