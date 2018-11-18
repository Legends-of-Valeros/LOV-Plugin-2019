package com.legendsofvaleros.modules.npcs;

import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.util.cmd.CommandManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPCCommands {
	@CommandManager.Cmd(cmd = "traits", help = "List all existing LOV NPC traits.", permission = "traits.list")
	public static CommandManager.CommandFinished cmdListTraits(CommandSender sender, Object[] args) {
		sender.sendMessage(ChatColor.YELLOW + "LOV Traits:");
		for(String str : NPCs.manager().traitTypes.keySet())
			sender.sendMessage(ChatColor.YELLOW + " " + str);
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "npc bind", args = "<lov-name>", help = "Bind an LOV NPC to a citizens NPC.", permission = "npc.bind")
	public static CommandManager.CommandFinished cmdBindNPC(CommandSender sender, Object[] args) {
		NPCData npcData = NPCs.manager().npcs.get(args[0]);
		if(npcData == null)
			return CommandManager.CommandFinished.CUSTOM.replace("NPC with that ID does not exist.");

		((Player)sender).performCommand("npc select");
		NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
		if(npc == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You must have an NPC selected to do that.");
		
		if(!npc.hasTrait(TraitLOV.class))		
			npc.addTrait(TraitLOV.class);
		npc.getTrait(TraitLOV.class).npcId = (String)args[0];

		npc.setName(UUID.randomUUID().toString());
		
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "npc bound", help = "View the LOV NPC binding ID to a citizens NPC.", permission = "npc.bind.view")
	public static CommandManager.CommandFinished cmdNPCID(CommandSender sender, Object[] args) {
		((Player)sender).performCommand("npc select");
		NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
		if(npc == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You must have an NPC selected to do that.");
		
		if(!npc.hasTrait(TraitLOV.class))
			return CommandManager.CommandFinished.CUSTOM.replace("That NPC is not bound to an LOV NPC.");

		MessageUtil.sendInfo(sender, "LOV ID: " + npc.getTrait(TraitLOV.class).npcId);
		
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "npcs reload", help = "Reload the NPC cache and citizens.", permission = "npc.reload", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdListNPCReload(CommandSender sender, Object[] args) {
		NPCs.manager().reload();

		((Player)sender).performCommand("citizens save");
		((Player)sender).performCommand("citizens reload");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "npc tp", args = "<lov-name>", help = "Teleport to an NPC.", permission = "npc.teleport", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdNPCTeleport(CommandSender sender, Object[] args) {
		for(TraitLOV trait : TraitLOV.all) {
			if(trait.npcId != null && trait.npcId.equals(args[0])) {
				if(trait.getNPC().getStoredLocation() != null) {
					((Player)sender).teleport(trait.getNPC().getStoredLocation());
					return CommandManager.CommandFinished.DONE;
				}else if(trait.getNPC().getEntity() != null
						&& trait.getNPC().getEntity().getLocation() != null) {
					((Player)sender).teleport(trait.getNPC().getEntity().getLocation());
					return CommandManager.CommandFinished.DONE;
				}else if(trait.npcData.loc != null) {
					((Player)sender).teleport(trait.npcData.loc);
					return CommandManager.CommandFinished.DONE;
				}
				
				return CommandManager.CommandFinished.CUSTOM.replace("Unable to teleport to that NPC.");
			}
		}
		
		return CommandManager.CommandFinished.CUSTOM.replace("NPC not attached to Citizens NPC.");
	}
}