package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.guilds.GuildManager;

import java.util.UUID;

@Table(name = "guild_role_permissions")
public class GuildRolePermission {
    @ForeignKey(table = Guild.class, name = "guild_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_id")
    private UUID guildId;

    @ForeignKey(table = GuildRole.class, name = "guild_role_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_role_id")
    private UUID roleId;
    public UUID getId() { return roleId; }

    @Column(name = "guild_role_permission")
    private GuildPermission permission;
    public GuildPermission getPermission() { return permission; }

    public GuildRolePermission(UUID guildId, UUID roleId, GuildPermission perm) {
        this.guildId = guildId;
        this.roleId = roleId;
        this.permission = perm;
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildRolePermissionTable().save(this, true); }
    public ListenableFuture<Boolean> remove() {
        GuildManager.getGuild(guildId).getRole(roleId).removePermission(permission);
        return GuildManager.getGuildRolePermissionTable().delete(this, true);
    }
}