package com.legendsofvaleros.modules.guilds;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "player_guilds")
public class GuildRole {
    @ForeignKey(table = Guild.class, name = "guild_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "guild_id")
    private UUID guildId;

    @Column(primary = true, name = "role_id")
    private UUID roleId;
    public UUID getId() { return roleId; }

    @Column(name = "role_name", length = 16)
    private String name;
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    private transient Set<String> permissions = new HashSet<>();
}
