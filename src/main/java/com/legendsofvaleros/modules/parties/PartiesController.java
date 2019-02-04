package com.legendsofvaleros.modules.parties;

import co.aikar.commands.contexts.OnlinePlayer;
import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.parties.commands.PartyCommands;
import com.legendsofvaleros.modules.parties.core.IParty;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.modules.parties.integration.PvPIntegration;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(ChatController.class)
@IntegratesWith(module = PlayerMenu.class, integration = PvPIntegration.class)
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
@ModuleInfo(name = "Parties", info = "")
public class PartiesController extends Module {
    private static PartiesController instance;
    public static PartiesController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        PartyManager.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new PartyCommands());
    }

    public void onChat(Player p, BaseComponent[] bc) {
        PlayerParty party = (PlayerParty) PartyManager.getPartyByMember(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        if (party == null) {
            MessageUtil.sendError(p, "You are not in a party.");
            return;
        }

        for (Player pl : party.getOnlineMembers()) {
            pl.spigot().sendMessage(bc);
        }
    }

}