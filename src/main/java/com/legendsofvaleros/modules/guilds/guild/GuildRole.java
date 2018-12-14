package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.guilds.GuildManager;
import com.legendsofvaleros.modules.guilds.guild.Guild;

import java.util.*;

@Table(name = "guild_roles")
public class GuildRole {
    @ForeignKey(table = Guild.class, name = "guild_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_id")
    private UUID guildId;

    @Column(primary = true, name = "guild_role_id")
    private UUID roleId;
    public UUID getId() { return roleId; }

    @Column(name = "guild_role_name", length = 16)
    private String name;
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    private transient Map<GuildPermission, GuildRolePermission> permissions = new HashMap<>();
    public Set<GuildPermission> getPermissions() { return permissions.keySet(); }
    public void putPermission(GuildRolePermission permission) { permissions.put(permission.getPermission(), permission); }

    public GuildRole(UUID guildId, String name) {
        this.guildId = guildId;
        this.roleId = UUID.randomUUID();
        this.name = name;
    }

    public void addPermission(GuildPermission permission) {
        GuildRolePermission grp;
        permissions.put(permission, grp = new GuildRolePermission(guildId, roleId, permission));
        grp.save();
    }

    public void removePermission(GuildPermission permission) {
        permissions.remove(permission)
                .remove();
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildRoleTable().save(this, true); }
    public ListenableFuture<Boolean> remove() {
        for(GuildMember member : Guild.getIfLoaded(guildId).getMembers()) {
            if(member.getRole() == this) {
                member.setRole(null);
                member.save();
            }
        }

        return GuildManager.getGuildRoleTable().delete(this, true);
    }
}