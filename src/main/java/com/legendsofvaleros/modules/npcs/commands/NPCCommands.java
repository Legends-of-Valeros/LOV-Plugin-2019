package com.legendsofvaleros.modules.npcs.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.LOVNPC;
import com.legendsofvaleros.modules.npcs.trait.TraitHelper;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("npcs|lov npcs")
public class NPCCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the NPC cache and citizens.")
	@CommandPermission("npcs.reload")
	public void cmdListNPCReload(CommandSender sender) throws Throwable {
		Bukkit.dispatchCommand(sender, "citizens save");
		Bukkit.dispatchCommand(sender, "citizens reload");
	}

	@Subcommand("activate")
	@Description("Activate an NPC remotely.")
	@CommandPermission("npcs.activate")
	public void cmdActivateNPC(Player player, String npcId, String side) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		LOVNPC npc = NPCsController.getInstance().getNPC(npcId);

		if(side.equalsIgnoreCase("left"))
			TraitHelper.onLeftClick(npc.getName(), player, npc.traits);
		else if(side.equalsIgnoreCase("right"))
			TraitHelper.onRightClick(npc.getName(), player, npc.traits);
		else
			MessageUtil.sendError(player, "Side argument must be 'left' or 'right'!");
	}

	@Subcommand("bind")
	@Description("Bind an LOV NPC to a citizens NPC.")
	@CommandPermission("npcs.bind")
	public void cmdBindNPC(Player player, String npcId) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		LOVNPC lovNPC = NPCsController.getInstance().getNPC(npcId);
		if(lovNPC == null) {
			MessageUtil.sendError(player, "NPC with that ID does not exist.");
			return;
		}

		player.performCommand("npc select");
		NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
		if(npc == null) {
			MessageUtil.sendError(player, "You must have an NPC selected to do that.");
			return;
		}
		
		if(!npc.hasTrait(TraitLOV.class))		
			npc.addTrait(TraitLOV.class);
		npc.getTrait(TraitLOV.class).npcId = npcId;

		npc.setName(UUID.randomUUID().toString());
	}

	@Subcommand("bound")
	@Description("View the LOV NPC binding ID to a citizens NPC.")
	@CommandPermission("npcs.bind.view")
	public void cmdNPCID(Player player) {
		player.performCommand("npc select");

		NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
		if(npc == null) {
			MessageUtil.sendError(player, "You must have an NPC selected to do that.");
			return;
		}
		
		if(!npc.hasTrait(TraitLOV.class)) {
			MessageUtil.sendError(player, "That NPC is not bound to an LOV NPC.");
			return;
		}

		MessageUtil.sendInfo(player, "LOV ID: " + npc.getTrait(TraitLOV.class).npcId);
	}

	@Subcommand("tp")
	@Description("Teleport to an NPC.")
	@CommandPermission("npcs.tp")
	public void cmdNPCTeleport(Player player, String id) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		for(TraitLOV trait : TraitLOV.all) {
			if(trait.npcId != null && trait.npcId.equals(id)) {
				if(trait.getNPC().getStoredLocation() != null) {
					player.teleport(trait.getNPC().getStoredLocation());
					return;
				}else if(trait.getNPC().getEntity() != null
						&& trait.getNPC().getEntity().getLocation() != null) {
					player.teleport(trait.getNPC().getEntity().getLocation());
					return;
				}else if(trait.getLovNPC().getLocation() != null) {
					player.teleport(trait.getLovNPC().getLocation());
					return;
				}

				MessageUtil.sendError(player, "Unable to teleport to that NPC.");
				return;
			}
		}
		
		MessageUtil.sendError(player, "NPC not attached to Citizens NPC.");
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}