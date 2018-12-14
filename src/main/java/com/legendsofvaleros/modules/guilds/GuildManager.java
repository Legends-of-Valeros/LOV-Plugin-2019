package com.legendsofvaleros.modules.guilds;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.guilds.guild.GuildMember;
import com.legendsofvaleros.modules.guilds.guild.GuildRole;
import com.legendsofvaleros.modules.guilds.guild.GuildRolePermission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildManager {
    private static ORMTable<Guild> guildTable;
    public static ORMTable<Guild> getGuildTable() { return guildTable; }

    private static ORMTable<GuildRole> guildRoleTable;
    public static ORMTable<GuildRole> getGuildRoleTable() { return guildRoleTable; }

    private static ORMTable<GuildRolePermission> guildRolePermissionTable;
    public static ORMTable<GuildRolePermission> getGuildRolePermissionTable() { return guildRolePermissionTable; }

    private static ORMTable<GuildMember> guildMemberTable;
    public static ORMTable<GuildMember> getGuildMemberTable() { return guildMemberTable; }

    /**
     * Guilds are in this map as long as a single player in the guild is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private static Map<UUID, Guild> guilds = new HashMap<>();
    public static Guild getGuild(UUID guildId) { return guilds.get(guildId); }

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
        guildRolePermissionTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildRolePermission.class);
        guildMemberTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildMember.class);

        GuildController.getInstance().registerEvents(new PlayerListener());
    }

    public static void remove(UUID guildId) {
        Guild g = guilds.remove(guildId);

        for(GuildMember m : g.getMembers())
            guildMembers.remove(m.getId());

        onlineMembers.removeAll(guildId);
    }

    public static ListenableFuture<Guild> load(UUID guildId) {
        SettableFuture<Guild> ret = SettableFuture.create();

        guildTable.query().first(guildId)
                .forEach(guild -> {
                    List<GuildRole> roles = new ArrayList<>();
                    List<GuildRolePermission> rolePermissions = new ArrayList<>();
                    List<GuildMember> members = new ArrayList<>();

                    AtomicInteger remaining = new AtomicInteger(3);
                    Runnable finished = () -> {
                        if(remaining.decrementAndGet() == 0) {
                            for(GuildRole role : roles) guild.putRole(role);

                            // Add the permission nodes to the roles in the guild
                            for(GuildRolePermission rolePerm : rolePermissions)
                                guild.getRole(rolePerm.getId()).putPermission(rolePerm);

                            for(GuildMember member : members) {
                                guild.putMember(member);

                                // Set the member's role object
                                member.setRole(guild.getRole(member.getRoleId()));
                            }

                            ret.set(guild);
                        }
                    };

                    guildRoleTable.query().select(guildId).build()
                            .forEach((r) -> roles.add(r))
                            .onFinished(finished)
                            .execute(true);

                    guildRolePermissionTable.query().select(guildId).build()
                            .forEach((rp) -> rolePermissions.add(rp))
                            .onFinished(finished)
                            .execute(true);

                    guildMemberTable.query().select(guildId).build()
                            .forEach((m) -> members.add(m))
                            .onFinished(finished)
                            .execute(true);
                })
            .onEmpty(() -> ret.set(null))
            .execute(true);

        return ret;
    }

    public static class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            // Only load a guild when the player first logs in.
            if(!event.isFirstInSession()) return;

            if(!guildMembers.containsKey(event.getPlayer().getUniqueId())) {
                PhaseLock lock = event.getLock("guild");

                guildMemberTable.query().select()
                            .where("player_id", event.getPlayer().getUniqueId())
                            .limit(1)
                        .build()
                        .forEach((guildMember) -> {
                            load(guildMember.getGuildId()).addListener(() -> {
                                onlineMembers.put(guildMember.getGuildId(), guildMember.getId());

                                lock.release();
                            }, GuildController.getInstance().getScheduler()::async);
                        }).onEmpty(() -> lock.release());
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
                    remove(guild.getId());
                }
            }
        }
    }
}