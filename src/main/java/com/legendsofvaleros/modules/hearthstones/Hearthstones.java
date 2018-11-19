package com.legendsofvaleros.modules.hearthstones;

import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.event.EventHandler;

/**
 * Bukkit instance main for Hearthstones.
 */
public class Hearthstones extends ListenerModule {
    private static Hearthstones instance;

    public static Hearthstones getInstance() {
        return instance;
    }

    private HomeTeleporter teleporter;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        HearthstonesManager.onEnable();
        teleporter = new HomeTeleporter(getConfig().getLong("warmup-seconds"));

        NPCs.registerTrait("innkeeper", TraitInnkeeper.class);

    }

    @EventHandler
    public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        HomePoint point = HearthstonesManager.getHome(Characters.getPlayerCharacter(event.getPlayer()));
        if (point == null) return;

        event.addSlot(Model.stack("menu-hearthstone-button").setName("Hearthstone: " + point.innName).create(), (gui, p, type) -> {
            gui.close(p);

            teleporter.attemptTeleport(Characters.getPlayerCharacter(p));
        });
    }


}
