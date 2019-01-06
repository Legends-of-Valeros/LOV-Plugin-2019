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
import com.legendsofvaleros.modules.quests.Quests;
import org.bukkit.Bukkit;
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

    protected static void onEnable() {
        guildTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Guild.class);
        guildRoleTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildRole.class);
        guildRolePermissionTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildRolePermission.class);
        guildMemberTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GuildMember.class);

        GuildController.getInstance().registerEvents(new PlayerListener());

        GuildController.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            Guild.cleanUp();
        }, 0L, 20L);
    }

    public static ListenableFuture<Guild> load(UUID guildId) {
        SettableFuture<Guild> ret = SettableFuture.create();

        guildTable.query().first(guildId)
                .forEach((guild, i) -> {
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

                            Guild.track(guild);

                            ret.set(guild);
                        }
                    };

                    guildRoleTable.query().select(guildId).build()
                            .forEach((r, j) -> roles.add(r))
                            .onFinished(finished)
                            .execute(true);

                    guildRolePermissionTable.query().select(guildId).build()
                            .forEach((rp, j) -> rolePermissions.add(rp))
                            .onFinished(finished)
                            .execute(true);

                    guildMemberTable.query().select(guildId).build()
                            .forEach((m, j) -> members.add(m))
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

            Guild g;
            if((g = Guild.getGuildByMember(event.getPlayer().getUniqueId())) == null) {
                PhaseLock lock = event.getLock("guild");

                guildMemberTable.query().select()
                            .where("player_id", event.getPlayer().getUniqueId().toString())
                            .limit(1)
                        .build()
                        .forEach((guildMember, i) -> {
                            ListenableFuture<Guild> future = load(guildMember.getGuildId());
                            future.addListener(() -> {
                                try {
                                    future.get().onLogin(event.getPlayer().getUniqueId());
                                } catch(Exception e) { e.printStackTrace(); }

                                lock.release();
                            }, GuildController.getInstance().getScheduler()::async);
                        }).onEmpty(() -> lock.release())
                        .execute(true);
            }else
                g.onLogin(event.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Guild guild;
            if((guild = Guild.getGuildByMember(event.getPlayer().getUniqueId())) != null)
                guild.onLogout(event.getPlayer().getUniqueId());
        }
    }
}