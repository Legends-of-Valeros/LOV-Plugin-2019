package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.guilds.commands.GuildCommands;

@DependsOn(Characters.class)
public class GuildController extends ModuleListener {
    private static GuildController instance;
    public static GuildController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        GuildManager.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new GuildCommands());
    }
}