package com.legendsofvaleros.modules.pvp.duel;

import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelManager {

    private static List<Duel> duels = new ArrayList<>();

    private DuelManager() {
    }

    public static void removeDuel(Duel duel) {
        PvPToggles toggles = PvP.getInstance().getToggles();
        List<DuelTeam> duelTeams = duel.getDuelTeams();

        duelTeams.forEach(duelTeam -> duelTeam.getTeamMembers().forEach(member -> toggles.removeToggleFor(member.getPlayerId(), duel.getPriority())));
        duels.remove(duel);
    }

    public static void addDuel(Duel duel) {
        duels.add(duel);
    }

    public static Duel getDuelFor(UUID playerId) {
        return duels.stream().filter(duel -> duel.getDuelTeams().stream().anyMatch(t ->
                t.getTeamMembers().stream().anyMatch(member -> member.getPlayerId().equals(playerId))
            )
        ).findFirst().orElse(null);
    }

}
