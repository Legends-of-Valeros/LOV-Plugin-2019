package com.legendsofvaleros.modules.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.ServerMode;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.guilds.GuildController;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.guilds.guild.GuildMember;
import com.legendsofvaleros.modules.guilds.guild.GuildPermission;
import com.legendsofvaleros.modules.guilds.guild.GuildRole;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("guild|lov guild")
public class GuildCommands extends BaseCommand {
    // I really don't like this whole system. I need to make a wrapper
    // that does guild and permission verification instead of doing it
    // all manually.
    @Subcommand("create")
    @Description("Create a new guild")
    public void cmdCreate(Player player, String name) {
        if(!LegendsOfValeros.getMode().isLenient()) return;

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

        // Add name verification. Two guilds should never have the same name.

        Guild guild = new Guild(name);
        guild.save().addListener(() -> {
            ListenableFuture<GuildRole> future = guild.addRole("Owner");
            future.addListener(() -> {
                try {
                    GuildRole role = future.get();
                    role.addPermission(GuildPermission.GUILD_ADMIN);

                    guild.addMember(player, role);

                    Guild.track(guild);

                    MessageUtil.sendUpdate(player, "Guild created successfully!");
                } catch(Exception e) {
                    MessageUtil.sendException(GuildController.getInstance(), e, true);
                }
            }, GuildController.getInstance().getScheduler()::async);

            guild.addRole("Commander");
            guild.addRole("Officer");
            guild.addRole("Veteran");
            guild.addRole("Member");
            guild.addRole("Recruit");
        }, GuildController.getInstance().getScheduler()::async);
    }

    @Subcommand("tag")
    @Description("Change your guild's tag.")
    public void cmdSetTag(Player player, @Optional String tag) {
        Guild g;
        if((g = Guild.getGuildByMember(player.getUniqueId())) == null) {
            MessageUtil.sendError(player, "You are not in a guild!");
            return;
        }

        GuildMember gm = g.getMember(player.getUniqueId());

        // Add guild tag verification. Two guilds should never have the same tag(?)

        if(!gm.hasPermission(GuildPermission.GUILD_RENAME)) {
            MessageUtil.sendError(player, "You don't have permission to do that!");
            return;
        }

        if(tag.length() != 3) {
            MessageUtil.sendError(player, "Guild tag must be 3 characters long!");
            return;
        }

        g.setTag(tag);
        g.save();

        MessageUtil.sendUpdate(player, "Guild tag changed to " + tag + "!");
    }

    @Subcommand("invite")
    @Description("Invite a player to your guild.")
    public void cmdInvite(Player player, OnlinePlayer invite, @Optional String role) {
        Guild g;
        if((g = Guild.getGuildByMember(player.getUniqueId())) == null) {
            MessageUtil.sendError(player, "You are not in a guild!");
            return;
        }

        GuildMember gm = g.getMember(player.getUniqueId());

        if(!gm.hasPermission(GuildPermission.MEMBER_INVITE)) {
            MessageUtil.sendError(player, "You don't have permission to do that!");
            return;
        }

        GuildRole gr = g.getRole(role != null ? role : "recruit");
        if(gr == null) {
            MessageUtil.sendError(player, "That role does not exist!");
            return;
        }

        // Player logout race condition
        g.addMember(invite.getPlayer(), gr).save().addListener(() -> {
            for(Player p : g.getMembers().stream().map(GuildMember::getPlayer).collect(Collectors.toList()))
                MessageUtil.sendUpdate(p, invite.getPlayer().getName() + " has been added to the guild!");

            MessageUtil.sendUpdate(invite.getPlayer(), "You have been added to [" + g.getName() + "]!");
        }, GuildController.getInstance().getScheduler()::async);
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}