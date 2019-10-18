package com.legendsofvaleros.modules.guilds;

/**
 * Created by Crystall on 09/19/2019
 */
public enum GuildRank {
    LEADER("Leader", "L"),
    OFFICER("Officer", "O"),
    MEMBER("Member", "M"),
    RECRUIT("Recruit", "R");

    private String displayName;
    private String guildChatTag;

    GuildRank(String displayName, String guildChatTag) {
        this.displayName = displayName;
        this.guildChatTag = guildChatTag;
    }
}
