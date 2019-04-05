package com.legendsofvaleros.modules.parties.core;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerParty {
    private UUID partyID;

    public UUID getUniqueId() {
        return partyID;
    }

    private final List<Score> scores = new ArrayList<>();
    private Scoreboard board;
    private Objective objective;

    public List<CharacterId> members = new ArrayList<>();
    public List<CharacterId> invitations = new ArrayList<>();

    public List<CharacterId> getMembers() {
        return members;
    }

    public CharacterId getLeader() {
        return members.get(0);
    }

    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (CharacterId id : members) {
            PlayerCharacter p = Characters.getInstance().getCharacter(id);
            if (p != null && p.isCurrent())
                online.add(p.getPlayer());
        }
        return online;
    }

    public PlayerParty() {
        this(UUID.randomUUID());
    }

    public PlayerParty(UUID id) {
        partyID = id;
    }

    public void onDisbanded() {
        objective.unregister();
        objective = null;
    }

    public void onMemberJoin(CharacterId uniqueId) {
        invitations.remove(uniqueId);

        Player joined = Bukkit.getPlayer(uniqueId.getPlayerId());
        MessageUtil.sendUpdate(joined, "You joined the party!");

        for (Player p : getOnlineMembers())
            if (p.getUniqueId().compareTo(uniqueId.getPlayerId()) != 0)
                MessageUtil.sendUpdate(p, joined.getName() + " joined the party!");
    }

    public void onMemberLeave(CharacterId uniqueId) {
        Player left = Bukkit.getPlayer(uniqueId.getPlayerId());

        MessageUtil.sendUpdate(left, "You left the party!");
        sendMessageToParty(left.getName() + " left the party!");

        left.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void onMemberEnter(CharacterId identifier) {
        if (board == null)
            board = Bukkit.getScoreboardManager().getNewScoreboard();

        if (objective == null) {
            objective = board.registerNewObjective(partyID.toString().substring(0, 15), "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName("------");

            updateUI();
        }
    }

    public void sendMessageToParty(String message) {
        for (Player p : getOnlineMembers()) {
            MessageUtil.sendUpdate(p, message);
        }
    }

    public void onMemberExit(CharacterId identifier) {
        PlayerCharacter pc = Characters.getInstance().getCharacter(identifier);
        if (pc != null && pc.isCurrent()) {
            pc.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            if (getOnlineMembers().size() == 0) {
                objective.unregister();
                objective = null;
            }
        }
    }

    public void updateUI() {
        if (members.size() == 0)
            return;

        final LinkedHashMap<String, ScoreHolder> scoreHolders = new LinkedHashMap<>();

        for (CharacterId uuid : this.members) {
            PlayerCharacter pc = Characters.getInstance().getCharacter(uuid);
            if (pc != null && pc.isCurrent()) pc.getPlayer().setScoreboard(board);

            ScoreHolder sh = new ScoreHolder(uuid);
            if (!scoreHolders.containsKey(sh.name))
                scoreHolders.put(sh.name, sh);
        }

        for (Score s : scores)
            board.resetScores(s.getEntry());
        scores.clear();

        int i = scoreHolders.size() * 3 - 1;
        int blanks = 0;

        Score s;
        for (Entry<String, ScoreHolder> entry : scoreHolders.entrySet()) {
            String color = "";

            if (entry.getValue().online) {
                if (getLeader().getPlayerId().compareTo(entry.getValue().uuid.getPlayerId()) == 0)
                    color += ChatColor.GOLD;
                else
                    color += ChatColor.WHITE;
            } else
                color += ChatColor.GRAY;

            s = objective.getScore(color + entry.getKey());
            scores.add(s);
            s.setScore(i);

            s = objective.getScore("§" + blanks + entry.getValue().getDisplayString());
            scores.add(s);
            s.setScore(i - 1);

            s = objective.getScore(i - 2 == 0 ? "     ------" : "§" + blanks + " ");
            scores.add(s);
            s.setScore(i - 2);

            i -= 3;
            blanks++;
        }
    }
}