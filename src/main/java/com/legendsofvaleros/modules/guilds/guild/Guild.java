package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.guilds.GuildController;
import com.legendsofvaleros.modules.guilds.GuildManager;
import org.bukkit.entity.Player;

import java.util.*;

@Table(name = "guilds")
public class Guild {
    @Column(primary = true, name = "guild_id")
    private UUID guildId;
    public UUID getId() { return guildId; }

    @Column(name = "guild_tag", length = 3)
    private String tag;
    public void setTag(String tag) { this.tag = tag; }
    public String getTag() { return tag; }

    @Column(name = "guild_name", length = 16)
    private String name;
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @Column(name = "guild_motd", type = "TEXT")
    private String motd;
    public void setMOTD(String motd) { this.motd = motd; }
    public String getMOTD() { return motd; }

    private transient Map<UUID, GuildRole> roles = new HashMap<>();
    public Collection<GuildRole> getRoles() { return roles.values(); }
    public GuildRole getRole(UUID id) { return roles.get(id); }
    public void putRole(GuildRole role) { roles.put(role.getId(), role); }

    private transient Map<UUID, GuildMember> members = new HashMap<>();
    public Collection<GuildMember> getMembers() { return members.values(); }
    public GuildMember getMember(UUID id) { return members.get(id); }

    public Guild(String name) {
        this.guildId = UUID.randomUUID();
        this.tag = name.substring(0, Math.min(3, name.length())).toUpperCase();
        this.name = name;
    }

    public ListenableFuture<GuildRole> addRole(String name) {
        GuildRole role = new GuildRole(guildId, name);
        roles.put(role.getId(), role);

        SettableFuture<GuildRole> ret = SettableFuture.create();
        role.save().addListener(() -> ret.set(role), GuildController.getInstance().getScheduler()::async);
        return ret;
    }

    public void removeRole(UUID id) {
        roles.remove(id)
                .remove();
    }

    public void putMember(GuildMember member) {
        members.put(member.getId(), member);

        playerGuild.put(member.getId(), guildId);
        guildMembers.put(guildId, member.getId());
    }

    public GuildMember addMember(Player player) {
        if(playerGuild.get(player.getUniqueId()) != null)
            throw new IllegalStateException("That player is already a member of a guild!");

        GuildMember gm = new GuildMember(guildId, player.getUniqueId(), null);
        members.put(gm.getId(), gm);
        gm.save();

        playerGuild.put(player.getUniqueId(), guildId);
        guildMembers.put(guildId, player.getUniqueId());

        return gm;
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId)
                .remove();

        playerGuild.remove(playerId);
        guildMembers.remove(guildId, playerId);
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildTable().save(this, true); }
    public ListenableFuture<Boolean> remove() {
        onlineMembers.removeAll(this);

        return GuildManager.getGuildTable().delete(this, true);
    }

    public void onLogin(UUID playerId) { onlineMembers.put(this, playerId); }
    public void onLogout(UUID playerId) { onlineMembers.remove(this, playerId); }

    // State management

    private static Map<UUID, UUID> playerGuild = new HashMap<>(); // <Player UUID, Guild UUID>
    private static Multimap<UUID, UUID> guildMembers = HashMultimap.create(); // <Guild UUID, Player UUID>

    private static Multimap<Guild, UUID> onlineMembers = HashMultimap.create();

    /**
     * Guilds are in this map as long as a single player in the guild is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private static Cache<UUID, Guild> guilds = CacheBuilder.newBuilder()
                                                    .weakValues()
                                                    .concurrencyLevel(4)
                                                    .removalListener(val -> {
                                                        for(UUID playerId : guildMembers.get((UUID)val.getKey()))
                                                            playerGuild.remove(playerId);

                                                        guildMembers.removeAll(val.getKey());
                                                    })
                                                    .build();
    public static Guild getIfLoaded(UUID guildId) { return guilds.getIfPresent(guildId); }

    public static Guild getGuildByMember(UUID uuid) {
        if(!playerGuild.containsKey(uuid)) return null;
        return guilds.getIfPresent(playerGuild.get(uuid));
    }
}