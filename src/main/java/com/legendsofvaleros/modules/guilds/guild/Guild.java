package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.guilds.GuildController;
import com.legendsofvaleros.modules.guilds.GuildManager;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Table(name = "guilds")
public class Guild {
    @Column(primary = true, index = true, name = "guild_id")
    private UUID guildId;
    public UUID getId() { return guildId; }

    @Column(index = true, name = "guild_tag", length = 3)
    private String tag;
    public void setTag(String tag) { this.tag = tag; }
    public String getTag() { return tag; }

    @Column(name = "guild_name", length = 24)
    private String name;
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @Column(name = "guild_motd", type = "TEXT")
    private String motd;
    public void setMOTD(String motd) { this.motd = motd; }
    public String getMOTD() { return motd; }

    private transient Map<String, GuildRole> roles;
    public Collection<GuildRole> getRoles() { return roles.values(); }
    public GuildRole getRole(String id) { return roles.get(id); }

    // We should have a default role system that users get set to if they
    // have no role or their role gets deleted.

    // NOTICE: This only holds a map of ONLINE GUILD MEMBERS.
    private transient Map<UUID, GuildMember> members;
    public Collection<GuildMember> getMembers() { return members.values(); }
    public GuildMember getMember(UUID id) { return members.get(id); }

    @Deprecated
    public Guild() {
        roles = new HashMap<>();
        members = new HashMap<>();
    }

    public Guild(String name) {
        this();

        this.guildId = UUID.randomUUID();
        this.name = name;
    }

    public void putRole(GuildRole role) { roles.put(role.getId(), role); }

    public ListenableFuture<GuildRole> addRole(String name) { return addRole(new GuildRole(guildId, name)); }
    public ListenableFuture<GuildRole> addRole(GuildRole role) {
        roles.put(role.getId(), role);

        SettableFuture<GuildRole> ret = SettableFuture.create();
        role.save().addListener(() -> ret.set(role), GuildController.getInstance().getScheduler()::async);
        return ret;
    }

    public void removeRole(UUID id) {
        roles.remove(id)
                .remove();
    }

    public GuildMember addMember(Player player, GuildRole role) {
        if(playerGuilds.get(player.getUniqueId()) != null)
            throw new IllegalStateException("That player is already a member of a guild!");

        GuildMember gm = new GuildMember(guildId, player.getUniqueId(), role.getId());
        members.put(gm.getId(), gm);
        gm.save();

        onLogin(gm);

        return gm;
    }

    public void removeMember(Player member) {
        members.remove(member.getUniqueId())
                .remove();

        onLogout(member);
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildTable().save(this, true); }
    public ListenableFuture<Boolean> remove() {
        // Invalidating the guild in the map will automatically clear the player guild trackers
        guilds.invalidate(guildId);

        return GuildManager.getGuildTable().delete(this, true);
    }

    public void onLogin(GuildMember member) {
        if(member.getRole() == null)
            member.setRole(roles.get(member.getRoleId()));

        members.put(member.getId(), member);

        playerGuilds.put(member.getId(), this);
        guildMembers.remove(this, member.getId());
    }

    public void onLogout(Player member) {
        members.remove(member.getUniqueId());

        playerGuilds.remove(member.getUniqueId());
        guildMembers.remove(guildId, member.getUniqueId());
    }

    // State management

    private static Map<UUID, Guild> playerGuilds = new HashMap<>(); // <Player UUID, Guild>
    private static Multimap<UUID, UUID> guildMembers = HashMultimap.create(); // <Guild UUID, Player UUID>

    /**
     * Guilds are in this map as long as a single player in the guild is online.
     * Once all go offline or leave, it is removed.
     */
    private static Cache<UUID, Guild> guilds = CacheBuilder.newBuilder()
                                                    .weakValues()
                                                    .concurrencyLevel(4)
                                                    .removalListener(entry -> {
                                                        // Ignore replacements
                                                        if(entry.getCause() == RemovalCause.REPLACED) return;

                                                        GuildController.getInstance().getLogger().warning("Guild '" + entry.getKey() + "' removed from the cache: " + entry.getCause());

                                                        for(UUID playerId : guildMembers.get((UUID)entry.getKey()))
                                                            playerGuilds.remove(playerId);

                                                        guildMembers.removeAll(entry.getKey());
                                                    })
                                                    .build();
    public static void cleanUp() { guilds.cleanUp(); }
    public static void track(Guild guild) { guilds.put(guild.guildId, guild); }
    public static Guild getIfLoaded(UUID guildId) { return guilds.getIfPresent(guildId); }

    public static Guild getGuildByMember(UUID uuid) {
        if(!playerGuilds.containsKey(uuid)) return null;
        return playerGuilds.get(uuid);
    }
}