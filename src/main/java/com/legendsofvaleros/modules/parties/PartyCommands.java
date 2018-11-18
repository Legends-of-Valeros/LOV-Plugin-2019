package com.legendsofvaleros.modules.parties;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager.Arg;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommands {
	@CommandManager.Cmd(cmd = "create", help = "Create a new party.", longhelp = "Create a party. You are set as the leader and the only member.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdPartyCreate(CommandSender sender, Object[] args) {
		PlayerCharacter pc = getPlayerCharacter(sender);
		if(getCurrentParty(pc) != null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are already in a party. If you'd like to leave, use /party leave");
		
		PartyManager.addMember(new PlayerParty(), pc.getUniqueCharacterId());
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "join", args = "<player>", argTypes = { CommandManager.Arg.ArgPlayer.class }, help = "Join a party.", longhelp = "Join a party that you have been invited to.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdPartyJoin(CommandSender sender, Object[] args) {
		PlayerCharacter pc = getPlayerCharacter(sender);
		if(getCurrentParty(pc) != null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are already in a party. If you'd like to leave, use /party leave");

		PlayerCharacter tpc = getPlayerCharacter((Player)args[0]);
		PlayerParty pp = getCurrentParty(tpc);
		if(pp == null)
			return CommandManager.CommandFinished.CUSTOM.replace("That player is not in a party.");
		else if(!pp.invitations.contains(pc.getUniqueCharacterId()))
			return CommandManager.CommandFinished.CUSTOM.replace("You have not been invited to that party.");
		else
			PartyManager.addMember(pp, pc.getUniqueCharacterId());
		
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "leave", help = "Leave your party.", longhelp = "Leave your party. If another player is still in the party, they'll be set as the leader.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdPartyLeave(CommandSender sender, Object[] args) {
		PlayerCharacter pc = getPlayerCharacter(sender);
		if(getCurrentParty(pc) == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are not in a party. If you'd like to create one, use /party create");
		
		PartyManager.removeMember(getCurrentParty(pc), pc.getUniqueCharacterId());
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "invite", args = "<player>", argTypes = { CommandManager.Arg.ArgPlayer.class }, help = "Invite a player.", longhelp = "Invite a player to your party!", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdPartyInvite(CommandSender sender, Object[] args) {
		PlayerCharacter pc = getPlayerCharacter(sender);
		if(getCurrentParty(pc) == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are not in a party. If you'd like to create one, use /party create");
		
		PlayerCharacter tpc = getPlayerCharacter((Player)args[0]);
		PlayerParty pp = getCurrentParty(pc);
		if(!pp.invitations.contains(tpc.getUniqueCharacterId())) {
			pp.invitations.add(tpc.getUniqueCharacterId());
			MessageUtil.sendUpdate(sender, "Invited " + tpc.getPlayer().getName() + " to the party.");
			MessageUtil.sendInfo(tpc.getPlayer(), sender.getName() + " has invited you to their party.");
		}else
			return CommandManager.CommandFinished.CUSTOM.replace("You have already invited that player.");
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "kick", args = "<player>", argTypes = { CommandManager.Arg.ArgOfflinePlayer.class }, help = "Kick a player.", longhelp = "Kick a player from your party cuz they've been baaaad.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdPartyKick(CommandSender sender, Object[] args) {
		PlayerCharacter pc = getPlayerCharacter(sender);
		if(getCurrentParty(pc) == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are not in a party. If you'd like to create one, use /party create");
		
		PlayerParty pp = getCurrentParty(pc);
		if(pp.getLeader().getPlayerId().equals(pc.getPlayer().getUniqueId())) {
			CharacterId targetId = null;
    		for(CharacterId id : pp.getMembers())
    			if(id.getPlayerId().equals(((OfflinePlayer)args[0]).getUniqueId())) {
    				targetId = id;
    				break;
    			}
    		
    		if(targetId == null)
    			return CommandManager.CommandFinished.CUSTOM.replace("That player is not in the party.");
    		
    		MessageUtil.sendUpdate(sender, sender.getName() + " has kicked " + Bukkit.getOfflinePlayer(targetId.getPlayerId()).getName() + " from the party.");
    		PartyManager.removeMember(pp, targetId);
		}else
			return CommandManager.CommandFinished.CUSTOM.replace("You are not the party leader.");
		return CommandManager.CommandFinished.DONE;
	}
	
	public static PlayerCharacter getPlayerCharacter(CommandSender sender) {
		if(!Characters.isPlayerCharacterLoaded((Player)sender)) return null;
		return Characters.getPlayerCharacter((Player)sender);
	}
	
	public static PlayerParty getCurrentParty(PlayerCharacter pc) {
		return (PlayerParty)PartyManager.getPartyByMember(pc.getUniqueCharacterId());
	}
}