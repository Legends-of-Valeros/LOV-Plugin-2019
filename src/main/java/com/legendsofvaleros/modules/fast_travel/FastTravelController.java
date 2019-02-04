package com.legendsofvaleros.modules.fast_travel;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.npcs.NPCsController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@DependsOn(CombatEngine.class)
@DependsOn(BankController.class)
@DependsOn(Characters.class)
@DependsOn(NPCsController.class)
public class FastTravelController extends Module {
    private static FastTravelController instance;
    public static FastTravelController getInstance() { return instance; }

    public void onLoad() {
        super.onLoad();

        instance = this;

        DiscoveredFastTravels.onEnable();
    }

    public void onUnload() {
        super.onUnload();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if(!Characters.isPlayerCharacterLoaded(p)) continue;
            DiscoveredFastTravels.onLogout(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        }
    }
}