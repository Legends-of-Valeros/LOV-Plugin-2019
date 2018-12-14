package com.legendsofvaleros.modules.guilds;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;

import java.util.UUID;

@Table(name = "player_guilds")
public class GuildMember {
    @Column(primary = true, unique = true, name = "character_id")
    private UUID playerId;
    public UUID getId() { return playerId; }

    @ForeignKey(table = Guild.class, name = "guild_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_id", length = 32)
    private UUID guildId;
    public UUID getGuildId() { return guildId; }

    @ForeignKey(table = GuildRole.class, name = "role_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.SET_NULL)
    @Column(name = "role_id")
    private UUID roleId;
    public UUID getRoleId() { return roleId; }

    public GuildMember(UUID playerId, UUID guildId) {
        this.playerId = playerId;
        this.guildId = guildId;
    }
}