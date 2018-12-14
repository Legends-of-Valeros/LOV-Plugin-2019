package com.legendsofvaleros.modules.guilds;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Table(name = "player_guilds")
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
    private transient Map<UUID, GuildMember> members = new HashMap<>();
}
