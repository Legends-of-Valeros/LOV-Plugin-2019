package com.legendsofvaleros.modules.guilds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.ServerMode;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.guilds.guild.GuildMember;
import com.legendsofvaleros.modules.guilds.guild.GuildPermission;
import com.legendsofvaleros.modules.guilds.guild.GuildRole;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("guild|lov guild")
public class GuildCommands extends BaseCommand {
    // I really don't like this whole system. I need to make a wrapper
    // that does guild and permission verification instead of doing it
    // all manually.
    @Subcommand("create")
    @Description("Create a new guild")
    public void cmdCreate(Player player, String name) {
        if(LegendsOfValeros.getMode() != ServerMode.DEV) return;

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

                    GuildMember member = guild.addMember(player);
                    member.setRole(role);
                    member.save().addListener(() -> {
                        Guild.track(guild);
                        MessageUtil.sendUpdate(player, "Guild created successfully!");
                    }, GuildController.getInstance().getScheduler()::async);
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
        if(!Characters.isPlayerCharacterLoaded(player)) return;

        Guild g;
        if((g = Guild.getGuildByMember(player.getUniqueId())) == null) {
            MessageUtil.sendError(player, "You are not in a guild!");
            return;
        }

        PlayerCharacter pc = Characters.getPlayerCharacter(player);
        GuildMember gm = g.getMember(player.getUniqueId());

        // Add guild tag verification. Two guilds should never have the same tag(?)

        if(!gm.hasPermission(GuildPermission.GUILD_ADMIN)) {
            MessageUtil.sendError(player, "You don't have permission to do that!");
            return;
        }

        if(tag.length() != 3) {
            MessageUtil.sendError(player, "Guild tag must be 3 characters long!");
            return;
        }

        g.setTag(tag);
        g.save();
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}