package com.legendsofvaleros.modules.guilds;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.parties.IParty;
import com.legendsofvaleros.modules.parties.PartyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
    private static ORMTable<Guild> guildTable;
    private static ORMTable<GuildRole> guildRoleTable;
    private static ORMTable<GuildMember> guildMemberTable;

    /**
     * Guilds are in this map as long as a single player in the guild is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private static Map<UUID, Guild> guilds = new HashMap<>();

    /**
     * Guilds are in this map as long as a single player in the guild is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private static Map<UUID, UUID> guildMembers = new HashMap<>();
    private static Multimap<UUID, UUID> onlineMembers = HashMultimap.create();

    /**
     * Get the guild for the specified UUID.
     * @param uuid The UUID.
     * @return The guild that the UUID is in. Returns null if it is not in a guild.
     */
    public static Guild getGuildByMember(UUID uuid) {
        if(!guildMembers.containsKey(uuid)) return null;
        return guilds.get(guildMembers.get(uuid));
    }

    protected static void onEnable() {
        guildTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Guild.class);
        guildRoleTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildRole.class);
        guildMemberTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildMember.class);

        GuildController.getInstance().registerEvents(new PlayerListener());
    }

    public static ListenableFuture<Void> save(UUID guildId) {
        return null;
    }

    public static ListenableFuture<Void> saveAndRemove(UUID guildId) {
        return null;
    }

    public static ListenableFuture<Void> load(UUID guildId) {
        return null;
    }

    public static class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerJoinEvent event) {
            if(!guildMembers.containsKey(event.getPlayer().getUniqueId())) {
                guildMemberTable.query().select()
                            .where("player_id", event.getPlayer().getUniqueId())
                            .limit(1)
                        .build()
                        .forEach((guildMember) -> {
                            onlineMembers.remove(guildMember.getGuildId(), guildMember.getId());

                            load(guildMember.getGuildId());
                        });
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if(guildMembers.containsKey(event.getPlayer().getUniqueId())) {
                Guild guild = getGuildByMember(event.getPlayer().getUniqueId());

                onlineMembers.remove(guild.getId(), event.getPlayer().getUniqueId());

                // If no more players in the guild are online
                if(onlineMembers.get(guild.getId()).size() == 0) {
                    // Unload guild
                    saveAndRemove(guild.getId());
                }
            }
        }
    }
}