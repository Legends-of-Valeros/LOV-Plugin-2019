package com.legendsofvaleros.modules.fast_travel;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.Module;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.NPCs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FastTravel implements Module {
    @Override
    public void onLoad() {
        DiscoveredFastTravels.init(LegendsOfValeros.getInstance());

        NPCs.registerTrait("fasttravel", TraitFastTravel.class);
    }

    @Override
    public void onUnload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            DiscoveredFastTravels.onLogout(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        }
    }
}