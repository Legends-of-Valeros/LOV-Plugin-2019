package com.legendsofvaleros.modules.mobs.listener;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.mobs.ExperienceHelper;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.parties.PartyManager;
import com.legendsofvaleros.modules.parties.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ExperienceListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if(event.getDied().isPlayer()) return;

        Mob.Instance entity = Mob.Instance.get(event.getDied().getLivingEntity());
        if(entity == null) return;

        if(event.getKiller() == null || !event.getKiller().isPlayer()) return;
        if(!Characters.isPlayerCharacterLoaded((Player)event.getKiller().getLivingEntity())) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player)event.getKiller().getLivingEntity());

        // Parties is optional. If the parties plugin doesn't exist, just give xp to the murderer.
        if(Bukkit.getPluginManager().getPlugin("Parties") == null) {
            int xp = ExperienceHelper.getExperience(pc, entity);
            pc.getExperience().addExperience(xp, false);
        }else{
            PlayerParty party = (PlayerParty) PartyManager.getPartyByMember(pc.getUniqueCharacterId());
            if (party != null) {
                double xpMod = (0.25 * (party.getOnlineMembers().size() - 2));
                // Only apply to parties > 2
                xpMod = (xpMod > 0 ? xpMod + 1 : 1);

                PlayerCharacter highestPC = pc;

                for (Player p : party.getOnlineMembers()) {
                    PlayerCharacter ppc = Characters.getPlayerCharacter(p);
                    if (ppc.getExperience().getLevel() > highestPC.getExperience().getLevel())
                        highestPC = ppc;
                }

                int xp = (int) (ExperienceHelper.getExperience(highestPC, entity) * xpMod);

                for (Player p : party.getOnlineMembers()) {
                    PlayerCharacter ppc = Characters.getPlayerCharacter(p);
                    ppc.getExperience().addExperience(xp * ppc.getExperience().getLevel() / (highestPC.getExperience().getLevel() + ppc.getExperience().getLevel()), false);
                }
            } else {
                int xp = ExperienceHelper.getExperience(pc, entity);
                pc.getExperience().addExperience(xp, false);
            }
        }
    }
}