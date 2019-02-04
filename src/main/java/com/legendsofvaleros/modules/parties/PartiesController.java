package com.legendsofvaleros.modules.parties;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.parties.commands.PartyCommands;
import com.legendsofvaleros.modules.parties.integration.PvPIntegration;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(ChatController.class)
@IntegratesWith(module = PlayerMenu.class, integration = PvPIntegration.class)
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
@ModuleInfo(name = "Parties", info = "")
public class PartiesController extends Module {
    private static PartiesController instance;

    public static PartiesController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        PartyManager.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new PartyCommands());
    }

}