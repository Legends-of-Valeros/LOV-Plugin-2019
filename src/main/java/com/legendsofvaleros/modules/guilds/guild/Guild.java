package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
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
    public void putMember(GuildMember member) { members.put(member.getId(), member); }

    public GuildRole addRole(String name) {
        GuildRole role = new GuildRole(guildId, name);
        roles.put(role.getId(), role);
        role.save();
        return role;
    }

    public void removeRole(UUID id) {
        roles.remove(id)
                .remove();
    }

    public GuildMember addMember(Player player) {
        GuildMember gm = new GuildMember(guildId, player.getUniqueId(), null);
        members.put(gm.getId(), gm);
        gm.save();
        return gm;
    }

    public void removeMember(UUID id) {
        members.remove(id)
                .remove();
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildTable().save(this, true); }
    public ListenableFuture<Boolean> remove() { return GuildManager.getGuildTable().delete(this, true); }
}