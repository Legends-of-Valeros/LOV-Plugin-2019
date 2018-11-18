package com.legendsofvaleros.modules.pvp.listener;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggle;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MiscHandler implements Listener {

    private final PvPToggle TOGGLE;

    public MiscHandler() {
        ConfigurationSection configuration = LegendsOfValeros.getInstance().getConfig().getConfigurationSection("pvp.global");
        TOGGLE = new PvPToggle((byte) configuration.getInt("priority"),
                configuration.getBoolean("enabled"),
                configuration.getInt("honor"));
    }

    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(Model.stack("menu-duel-button").setName("Duel").create(), (gui, p, ice) -> gui.close(p));
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
        Player player = event.getPlayer();

        PvP.getInstance().getToggles().clearTogglesFor(player.getUniqueId());
    }

    @EventHandler
    public void onCharacterLogin(PlayerCharacterFinishLoadingEvent event) {
        Player p = event.getPlayer();

        PvP.getInstance().getToggles().setToggleFor(
                p.getUniqueId(), TOGGLE);
    }

}
