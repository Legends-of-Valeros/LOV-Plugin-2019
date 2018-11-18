package com.legendsofvaleros.modules.parties;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.PlayerData;
import com.legendsofvaleros.util.Utilities;
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

import static com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat.HEALTH;

public class PlayerParty implements IParty {
    private UUID partyID;

    @Override
    public UUID getUniqueId() {
        return partyID;
    }

    final List<Score> scores = new ArrayList<>();
    private Scoreboard board;
    private Objective objective;

    public List<CharacterId> members = new ArrayList<>();
    public List<CharacterId> invitations = new ArrayList<>();

    @Override
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

    @Override
    public void onDisbanded() {
        objective.unregister();
        objective = null;
    }

    @Override
    public void onMemberJoin(CharacterId uniqueId) {
        invitations.remove(uniqueId);

        Player joined = Bukkit.getPlayer(uniqueId.getPlayerId());
        MessageUtil.sendUpdate(joined, "You joined the party!");

        for (Player p : getOnlineMembers())
            if (p.getUniqueId().compareTo(uniqueId.getPlayerId()) != 0)
                MessageUtil.sendUpdate(p, joined.getName() + " joined the party!");
    }

    @Override
    public void onMemberLeave(CharacterId uniqueId) {
        Player left = Bukkit.getPlayer(uniqueId.getPlayerId());
        MessageUtil.sendUpdate(left, "You left the party!");

        for (Player p : getOnlineMembers())
            MessageUtil.sendUpdate(p, left.getName() + " left the party!");

        left.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @Override
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

    @Override
    public void onMemberExit(CharacterId identifier) {
        Characters.getInstance().getCharacter(identifier).getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (getOnlineMembers().size() == 0) {
            objective.unregister();
            objective = null;
        }
    }

    @Override
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

class ScoreHolder {
    String name;

    CharacterId uuid;
    boolean online = false;
    boolean current = false;

    int health = 0;
    int energyMana = 0;

    public ScoreHolder(CharacterId uuid) {
        this.uuid = uuid;

        name = Bukkit.getOfflinePlayer(uuid.getPlayerId()).getName();
        if (name == null) {
            name = "Unknown Player";

            ListenableFuture<PlayerData> future = PlayerData.get(uuid.getPlayerId());
            future.addListener(() -> {
                try {
                    PlayerData data = future.get();
                    name = data.username;
                } catch (Exception e) {
                }
            }, Utilities.asyncExecutor());
        }

        update();
    }

    public void update() {
        PlayerCharacter pc = Characters.getInstance().getCharacter(uuid);

        if (pc != null) {
            name = pc.getPlayer().getName();

            online = true;
            current = pc.isCurrent();

            CombatEntity ce = CombatEngine.getEntity(pc.getPlayer());
            if (ce != null) {
                this.health = (int) (ce.getStats().getRegeneratingStat(HEALTH) / ce.getStats().getStat(Stat.MAX_HEALTH) * 6);

                switch (pc.getPlayerClass().getSkillCostType()) {
                    case HEALTH:
                        break;
                    case ENERGY:
                        this.energyMana = (int) (ce.getStats().getRegeneratingStat(RegeneratingStat.ENERGY) / ce.getStats().getStat(Stat.MAX_ENERGY) * 6);
                        break;
                    case MANA:
                        this.energyMana = (int) (ce.getStats().getRegeneratingStat(RegeneratingStat.MANA) / ce.getStats().getStat(Stat.MAX_MANA) * 6);
                        break;
                }
            }
        } else {
            online = current = false;
        }
    }

    public String getDisplayString() {
        return getHealthString() + "    " + getEnergyString();
    }

    /**
     * Builds the health bar for display.
     */
    public String getHealthString() {
        StringBuilder sb = new StringBuilder();
        sb.append("♥♥♥♥♥♥");
        if (this.current)
            sb.insert(health, ChatColor.GRAY);
        sb.insert(0, this.current ? ChatColor.RED : (online ? ChatColor.DARK_GRAY : ChatColor.BLACK));
        return sb.toString();
    }

    /**
     * Builds the enerby/mana bar for display.
     */
    public String getEnergyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("☼☼☼☼☼☼");
        if (this.current)
            sb.insert(6 - energyMana, ChatColor.AQUA);
        sb.insert(0, this.current ? ChatColor.GRAY : (online ? ChatColor.DARK_GRAY : ChatColor.BLACK));
        return sb.toString();
    }
}