package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.modules.arena.arenaModes.ArenaMode;
import com.legendsofvaleros.modules.arena.arenaModes.OneVersusOne;
import com.legendsofvaleros.modules.arena.arenaModes.TwoVersusTwo;
import com.legendsofvaleros.modules.queue.Queue;
import com.legendsofvaleros.modules.queue.QueueController;
import com.legendsofvaleros.modules.queue.events.QueueReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 07/24/2019
 */
public class ArenaController extends ArenaAPI {
    public static Location ARENA_MIDDLE_POSITION = new Location(Bukkit.getWorld("Valeros"), 2451, 93, 2193);

    private static ArenaController instance;

    public static ArenaController getInstance() {
        return instance;
    }

    private List<Arena> running = new ArrayList<>();

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        QueueController.getInstance().registerQueue(OneVersusOne.class, 2);
        QueueController.getInstance().registerQueue(TwoVersusTwo.class, 4);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        instance = null;
    }

//    @EventHandler
//    public void onLogout(PlayerCharacterLogoutEvent event) {
//        onLogout(event.getPlayerCharacter().getPlayerId())
//                .onFailure(err -> MessageUtil.sendException(ArenaController.getInstance(), err.toString()));
//    }
//
//    @EventHandler
//    public void onLogin(PlayerCharacterFinishLoadingEvent event) {
//        PlayerLock.lockPlayer(event.getPlayer());
//
//        onLogin(event.getPlayerCharacter().getPlayerId())
//                .onSuccess(() -> {
//                    //TODO add value to map
//                })
//                .onFailure(err -> MessageUtil.sendException(ArenaController.getInstance(), err.toString()));
//    }


    @EventHandler
    public void onQueueReady(QueueReadyEvent event) {
        Queue queue = event.getQueue();
        ArenaMode mode = getModeFromQueue(queue);
        List<Player> queuedPlayers = queue.getQueuedPlayers();
        List<Player> team1 = new ArrayList<>();
        List<Player> team2 = new ArrayList<>();

        for (int i = 0; i < queuedPlayers.size(); i++) {
            if (i < queuedPlayers.size() / 2) {
                team1.add(queuedPlayers.get(i));
                continue;
            }
            team2.add(queuedPlayers.get(i));
        }
        new Arena(team1, team2, mode, false);
    }

    private ArenaMode getModeFromQueue(Queue queue) {
        if (queue.get() instanceof OneVersusOne) {
            return new OneVersusOne();
        } else if (queue.get() instanceof TwoVersusTwo) {
            return new TwoVersusTwo();
        }

        return null;
    }

}
