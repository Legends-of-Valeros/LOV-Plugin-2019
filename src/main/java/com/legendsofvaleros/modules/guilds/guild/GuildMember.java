package com.legendsofvaleros.modules.guilds.guild;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.guilds.GuildManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Table(name = "guild_members")
public class GuildMember {
    @ForeignKey(table = Guild.class, name = "guild_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_id", length = 32)
    private UUID guildId;
    public UUID getGuildId() { return guildId; }

    @Column(primary = true, unique = true, name = "player_id")
    private UUID playerId;
    public UUID getId() { return playerId; }

    @ForeignKey(table = GuildRole.class, name = "guild_role_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.SET_NULL)
    @Column(name = "guild_role_id")
    private UUID roleId;
    public UUID getRoleId() { return roleId; }

    private transient GuildRole role;
    public GuildRole getRole() { return role; }

    public GuildMember(UUID guildId, UUID playerId, UUID roleId) {
        this.guildId = guildId;
        this.playerId = playerId;
        this.roleId = roleId;
    }

    public void setRole(GuildRole role) {
        this.roleId = role.getId();
        this.role = role;
    }

    public boolean isPlayerOnline() {
        return Bukkit.getOfflinePlayer(playerId).isOnline();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }

    public ListenableFuture<Boolean> save() { return GuildManager.getGuildMemberTable().save(this, true); }
    public ListenableFuture<Boolean> remove() {
        return GuildManager.getGuildMemberTable().delete(this, true);
    }
}