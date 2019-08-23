package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.loading.PlayerLock;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;

/**
 * Created by Crystall on 07/24/2019
 * // TODO let this controller handle all login locks that are existing and also prevent players being locked forever
 *  (probably with a cronjob?)
 */
public class ArenaController extends ArenaAPI {

    private static ArenaController instance;

    public static ArenaController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }

    @Override
    public void onUnload() {
        super.onUnload();

        instance = null;
    }

    @EventHandler
    public void onLogout(PlayerCharacterLogoutEvent event) {
        onLogout(event.getPlayerCharacter().getPlayerId())
                .onFailure(err -> MessageUtil.sendException(ArenaController.getInstance(), err.toString()));
    }

    @EventHandler
    public void onLogin(PlayerCharacterFinishLoadingEvent event) {
        PlayerLock.lockPlayer(event.getPlayer());

        onLogin(event.getPlayerCharacter().getPlayerId())
                .onSuccess(() -> {
                    //TODO add value to map
                })
                .onFailure(err -> MessageUtil.sendException(ArenaController.getInstance(), err.toString()));
    }

}
