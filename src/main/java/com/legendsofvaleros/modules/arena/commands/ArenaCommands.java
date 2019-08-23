package com.legendsofvaleros.modules.arena.commands;

/**
 * Created by Crystall on 08/04/2019
 */

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 08/02/2019
 */
@CommandAlias("arena")
public class ArenaCommands {

    @Subcommand("quit")
    @Description("Quits out of the arena and gives up")
    public void onQuit(Player player) {
        //TODO get arena and quit out of it
    }

    @Subcommand("setSpawn")
    @Description("Sets the 'middle' of the arena. This is a workaround until instanced arenas are a thing")
    public void onSetSpawn(Player player) {

    }

}