package com.legendsofvaleros.modules.mount;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MountManager implements Listener {
    private static final String MOUNT_TABLE = "mounts";
    private static final String MOUNT_ID = "mount_id";
    private static final String MOUNT_GROUP = "mount_group";
    private static final String MOUNT_NAME = "mount_name";
    private static final String MOUNT_TYPE = "mount_type";
    private static final String MOUNT_LEVEL = "mount_level";
    private static final String MOUNT_SPEED = "mount_speed";
    private static final String MOUNT_ICON = "mount_icon";
    private static final String MOUNT_COST = "mount_cost";

    private static final String CHARACTER_MOUNT_TABLE = "player_mounts";
    private static final String CHARACTER_ID = "character_id";
    private static final String CHARACTER_MOUNT = "character_mount";

    private final TableManager manager;
    private final TableManager managerCharacters;

    private HashMap<String, Mount> mounts = new HashMap<>();

    public Mount getMount(String id) {
        return mounts.get(id);
    }

    private Multimap<String, Mount> characterMounts = HashMultimap.create();

    public Multimap<String, Mount> getCharacterMounts() {
        return characterMounts;
    }

    public MountManager() {
        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), MOUNT_TABLE);

        manager.primary(MOUNT_ID, "VARCHAR(32)")
                .column(MOUNT_GROUP, "VARCHAR(64)")
                .column(MOUNT_NAME, "VARCHAR(32)")
                .column(MOUNT_TYPE, "VARCHAR(16)")
                .column(MOUNT_LEVEL, "SMALLINT")
                .column(MOUNT_SPEED, "FLOAT")
                .column(MOUNT_ICON, "VARCHAR(16)")
                .column(MOUNT_COST, "INT").create();

        managerCharacters = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), CHARACTER_MOUNT_TABLE);

        managerCharacters.primary(CHARACTER_ID, "VARCHAR(38)")
                .primary(CHARACTER_MOUNT, "VARCHAR(64)").create();

        MountsController.getInstance().registerEvents(this);

        loadMounts();
    }

    public void loadMounts() {
        manager.query()
                .select()
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    while (result.next()) {
                        mounts.put(result.getString(MOUNT_ID), new Mount(result.getString(MOUNT_ID),
                                result.getString(MOUNT_NAME),
                                EntityType.valueOf(result.getString(MOUNT_TYPE)),
                                result.getFloat(MOUNT_SPEED),
                                result.getInt(MOUNT_LEVEL),
                                Material.valueOf(result.getString(MOUNT_ICON)),
                                result.getInt(MOUNT_COST)));
                    }
                })
                .execute(true);
    }

    /**
     * Adds a mount to a player.
     */
    public void addMount(final CharacterId identifier, final Mount mount) {
        if (mount == null)
            return;

        characterMounts.put(identifier.toString(), mount);

        managerCharacters.query()
                .insert()
                .values(CHARACTER_ID, identifier.toString(),
                        CHARACTER_MOUNT, mount.getId())
                .build()
                .execute(true);
    }

    public ListenableFuture<Collection<Mount>> getMounts(CharacterId identifier) {
        SettableFuture<Collection<Mount>> ret = SettableFuture.create();

        if (identifier == null)
            ret.set(new ArrayList<>());
        else if (characterMounts.containsKey(identifier.toString()))
            ret.set(characterMounts.get(identifier.toString()));
        else {
            managerCharacters.query()
                    .select()
                    .where(CHARACTER_ID, identifier.toString())
                    .build()
                    .callback((statement, count) -> {
                        ResultSet result = statement.getResultSet();

                        while (result.next()) {
                            String characterID = result.getString(CHARACTER_ID);
                            String characterMount = result.getString(CHARACTER_MOUNT);

                            characterMounts.put(characterID, getMount(characterMount));
                        }

                        ret.set(characterMounts.values());
                    })
                    .execute(true);
        }

        return ret;
    }

    @EventHandler
    public void onPlayerLeave(PlayerCharacterLogoutEvent e) {
        characterMounts.removeAll(e.getPlayerCharacter().getUniqueCharacterId().toString());
    }

    @EventHandler
    public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
        managerCharacters.query()
                .remove()
                .where(CHARACTER_ID, event.getPlayerCharacter().getUniqueCharacterId().toString())
                .build()
                .execute(true);
    }
}