package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crystall on 09/19/2019
 */
public class Guild {

    public String guildName;
    public String guildTag;
    public String motd;
    public List<CharacterId> member;
    public List<PlayerCharacter> onlineMember = new ArrayList<>();
    public Map<CharacterId, GuildRank> guildRankHashMap = new HashMap<>();
    public transient boolean isOnline = false;

    public Guild(String guildName, String guildTag, String motd, List<CharacterId> members) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.motd = motd;
        this.member = members;
    }

    /**
     * Sends a message to all online guild members
     * @param msg the message to send to everyone
     */
    public void onChat(String msg) {
        //TODO
        onlineMember.forEach(playerCharacter ->
                playerCharacter.getPlayer().sendMessage(String.format("%s %s %s", ChatColor.DARK_GREEN + guildTag, playerCharacter.getPlayer().getDisplayName(), msg))
        );
    }

    /**
     * Checks if the given player id is online
     * @param characterId the characterid to check for
     * @return boolean if the characterid is online in the guild
     */
    public boolean isPlayerOnline(CharacterId characterId) {
        return onlineMember.contains(characterId);
    }
}
