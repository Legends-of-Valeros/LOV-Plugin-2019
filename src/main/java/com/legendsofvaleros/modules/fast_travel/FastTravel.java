package com.legendsofvaleros.modules.fast_travel;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.Module;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.NPCs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FastTravel extends Module {
    private static FastTravel instance;
    public static FastTravel getInstance() { return instance; }

    public void onLoad() {
        super.onLoad();

        instance = this;

        DiscoveredFastTravels.onEnable();

        NPCs.registerTrait("fasttravel", TraitFastTravel.class);
    }

    public void onUnload() {
        super.onUnload();
        for (Player p : Bukkit.getOnlinePlayers()) {
            DiscoveredFastTravels.onLogout(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        }
    }
}