package com.legendsofvaleros.modules.guilds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.guilds.guild.GuildMember;
import com.legendsofvaleros.modules.guilds.guild.GuildRole;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("guild|lov guild")
public class GuildCommands extends BaseCommand {
    @Subcommand("create")
    @Description("Create a new guild")
    public void cmdCreate(Player player, String name) {
        if(!Characters.isPlayerCharacterLoaded(player)) return;

        if(Guild.getGuildByMember(player.getUniqueId()) != null) {
            MessageUtil.sendError(player, "You are already in a guild!");
            return;
        }

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        // 1000 gold
        if(Money.get(pc) < 10000 * 1000) {
            MessageUtil.sendError(player, "You need " + Money.Format.format(10000 * 1000) + " gold to do that!");
            return;
        }

        Guild guild = new Guild(name);
        guild.save().addListener(() -> {
            ListenableFuture<GuildRole> future = guild.addRole("Owner");
            future.addListener(() -> {
                try {
                    GuildMember member = guild.addMember(player);
                    member.setRole(future.get());
                    member.save().addListener(() -> {
                        MessageUtil.sendUpdate(player, "Guild created successfully!");
                    }, GuildController.getInstance().getScheduler()::async);
                } catch(Exception e) {
                    MessageUtil.sendException(GuildController.getInstance(), e, true);
                }
            }, GuildController.getInstance().getScheduler()::async);
        }, GuildController.getInstance().getScheduler()::async);
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}