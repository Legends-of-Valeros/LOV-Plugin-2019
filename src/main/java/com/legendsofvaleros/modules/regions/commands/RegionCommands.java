package com.legendsofvaleros.modules.regions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.modules.regions.core.RegionSelector;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("regions|lov regions")
public class RegionCommands extends BaseCommand {
    @Subcommand("notify")
    @Description("Notify when entering and exiting a regions.")
    @CommandPermission("regions.notify")
    public void cmdDebug(CommandSender sender) {
        RegionController.REGION_DEBUG = !RegionController.REGION_DEBUG;
        MessageUtil.sendUpdate(sender, "Region debugging is now " + (RegionController.REGION_DEBUG ? "enabled" : "disabled") + ".");
    }

    @Subcommand("wand")
    @Description("Fetch the regions wand.")
    @CommandPermission("regions.wand")
    public void cmdWand(Player player) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        player.getInventory().addItem(new ItemBuilder(Material.ARROW).setName(RegionSelector.ITEM_NAME).create());
        MessageUtil.sendUpdate(player, "There you go.");
    }

    @Subcommand("create")
    @Description("Create a new regions. Default access denied.")
    @CommandPermission("regions.create")
    public void cmdCreate(Player player, String id, @Optional Boolean access) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        if (access == null) access = false;

        if (RegionController.getInstance().getRegion(id) != null) {
            MessageUtil.sendError(player, "A regions with that ID already exists.");
            return;
        }

        Location[] locations = RegionSelector.selection.get(player);
        if (locations == null || locations[0] == null || locations[1] == null) {
            MessageUtil.sendError(player, "You must make a selection before doing that.");
            return;
        }

        Region region = new Region(id, locations[0].getWorld(), new RegionBounds().setBounds(locations[0], locations[1]));
        region.allowAccess = access;

        RegionController.getInstance().addRegion(region);
        RegionController.getInstance().saveRegion(region);

        MessageUtil.sendUpdate(player, "Region created. Default access: " + access);
    }

    @Subcommand("remove")
    @Description("Remove a regions.")
    @CommandPermission("regions.remove")
    public void cmdRemove(CommandSender sender, String regionId) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        if (RegionController.getInstance().getRegion(regionId) == null) {
            MessageUtil.sendError(sender, "A regions with that ID doesn't exist.");
            return;
        }

        RegionController.getInstance().removeRegion(regionId);
        MessageUtil.sendError(sender, "Region removed.");
    }

    @Subcommand("hearthstone")
    @Description("Toggle if a regions allows hearthstones.")
    @CommandPermission("regions.set.hearthstone")
    public void cmdToggleHearthstone(CommandSender sender, String regionId) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        region.allowHearthstone = !region.allowHearthstone;

        RegionController.getInstance().saveRegion(region);
        MessageUtil.sendUpdate(sender, "Region updated. Allows Hearthstones: " + region.allowHearthstone);
    }

    @Subcommand("quests")
    @Description("List the quest triggers in the regions.")
    @CommandPermission("regions.gear.list")
    public void cmdQuestList(CommandSender sender, String regionId) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        MessageUtil.sendUpdate(sender, "'" + regionId + "' triggers: " + String.join(", ", region.getQuestsTriggered().stream().map(IQuest::getName).collect(Collectors.toList())));
    }

    @Subcommand("quests add")
    @Description("Add a quest trigger to the regions.")
    @CommandPermission("regions.gear.modify")
    public void cmdQuestAdd(CommandSender sender, String regionId, String questId) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        if (region.quests.contains(questId)) {
            MessageUtil.sendError(sender, "Quest already attached to that regions.");
            return;
        }

        QuestController.getInstance().getQuest(questId).onSuccess(quest -> {
            region.quests.add(quest.get());

            RegionController.getInstance().saveRegion(region);
            MessageUtil.sendUpdate(sender, "Region updated. Now triggers gear: " + questId);
        }).onFailure(() -> {
            MessageUtil.sendError(sender, "Unknown quest: " + questId);
        });
    }

    @Subcommand("quests del")
    @Description("Delete a quest trigger from the regions.")
    @CommandPermission("regions.gear.modify")
    public void cmdQuestDel(CommandSender sender, String regionId, String questId) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        if (!region.quests.contains(questId)) {
            MessageUtil.sendError(sender, "Quest is not attached to that regions.");
            return;
        }

        region.quests.remove(questId);

        RegionController.getInstance().saveRegion(region);
        MessageUtil.sendUpdate(sender, "Region updated. No longer triggers gear: " + questId);
    }

    @Subcommand("enter")
    @Description("Set a regions enter message.")
    @CommandPermission("regions.set.enter")
    public void cmdSetEnter(CommandSender sender, String regionId, String message) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        region.msgEnter = message;

        RegionController.getInstance().saveRegion(region);
        MessageUtil.sendUpdate(sender, "Region updated. Enter: '" + region.msgEnter + "'");
    }

    @Subcommand("exit")
    @Description("Set a regions exit message.")
    @CommandPermission("regions.set.exit")
    public void cmdSetExit(CommandSender sender, String regionId, String message) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        region.msgExit = message;

        RegionController.getInstance().saveRegion(region);
        MessageUtil.sendUpdate(sender, "Region updated. Exit: '" + region.msgExit + "'");
    }

    @Subcommand("failure")
    @Description("Set a regions enter failure message.")
    @CommandPermission("regions.")
    public void cmdSetFailure(CommandSender sender, String regionId, String message) {
        if (!LegendsOfValeros.getMode().allowEditing()) return;

        Region region = (Region)RegionController.getInstance().getRegion(regionId);
        if (region == null) {
            MessageUtil.sendError(sender, "A regions with that name doesn't exist.");
            return;
        }

        region.msgError = message;

        RegionController.getInstance().saveRegion(region);
        MessageUtil.sendUpdate(sender, "Region updated. Failure: '" + region.msgError + "'");
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}