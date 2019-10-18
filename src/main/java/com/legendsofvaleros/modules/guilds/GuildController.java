package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;

/**
 * Created by Crystall on 09/19/2019
 */
public class GuildController extends GuildAPI {

    private static GuildController instance;

    public static GuildController getInstance() {
        return instance;
    }

    public boolean createGuild(Guild guild) {
        return false;
    }

    public boolean deleteGuild(Guild guild) {
        return false;
    }


    /**
     * Returns if a guild is online. A guild counts as online if three or more players are online
     * @param guild
     * @return
     */
    public boolean isGuildOnline(Guild guild) {
        if (guild.onlineMember.size() >= 3) {
            guild.isOnline = true;
        }
        return guild.isOnline = false;
    }

    /**
     * Finds the guild for a player, null if none
     * @param characterId
     * @return
     */
    public Guild getPlayerGuild(CharacterId characterId) {
        return guilds.stream().filter(guild -> guild.member.contains(characterId)).findAny().orElse(null);
    }

    /**
     * Checks if the player has a guild and if it's loaded / online
     * @param characterId to check for
     * @return
     */
    public boolean isGuildLoaded(CharacterId characterId) {
        return getPlayerGuild(characterId) != null;
    }

    @EventHandler
    public void onMemberLogin(PlayerCharacterFinishLoadingEvent event) {
        CharacterId characterId = event.getPlayerCharacter().getUniqueCharacterId();
        Guild guild = null;
        if (isGuildLoaded(characterId)) {
            guild = getPlayerGuild(characterId);
        } else {
            // guild is not loaded, load it
            loadGuild(event.getPlayerCharacter().getUniqueCharacterId())
                    .onSuccess(val -> val.ifPresent(guilds::add))
                    .onFailure(err -> MessageUtil.sendException(GuildController.getInstance(), err));
        }

        // Player has no guild
        if (guild == null) {
            return;
        }

        guild.onlineMember.add(Characters.getPlayerCharacter(characterId));
    }
}
