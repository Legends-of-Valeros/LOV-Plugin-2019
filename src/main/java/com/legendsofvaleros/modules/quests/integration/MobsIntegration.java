package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.parties.PartyManager;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.objective.mobs.KillObjective;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MobsIntegration extends Integration implements Listener {
    public MobsIntegration() {
        QuestController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("kill", KillObjective.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getKiller() == null || !event.getKiller().isPlayer()) return;

        Player p = (Player) event.getKiller().getLivingEntity();

        if (!Characters.isPlayerCharacterLoaded(p)) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(p);

        QuestManager.callEvent(event, pc);

        // Update for each player in the party
        if(Modules.isLoaded(PartiesController.class)) {
            PlayerParty party = (PlayerParty) PartyManager.getPartyByMember(pc.getUniqueCharacterId());
            if(party != null) {
                for (Player pp : party.getOnlineMembers()) {
                    if(p == pp || !Characters.isPlayerCharacterLoaded(pp))
                        continue;

                    PlayerCharacter ppc = Characters.getPlayerCharacter(pp);
                    QuestManager.callEvent(event, ppc);
                }
            }
        }
    }
}
