package com.legendsofvaleros.modules.tabmenu;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.api.Experience;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.friends.FriendsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Crystall on 07/30/2019
 * Modifies the tab list to display the desired values
 */
public class TabMenuController extends Module {

    private static final int SLOTS_PER_COLUMN = 18;

    @Override
    public void onLoad() {
        super.onLoad();
        Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), this::setupMenu); // Wait until after BungeeTabListPlus loads.
    }

    private void setupMenu() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("BungeeTabListPlus")) {
            MessageUtil.sendException(this, "BungeeTabListPlus not installed. Custom Tab list wont work without it");
        }

        addVariable("guild.0", player -> "");
        addVariable("guild.1", player -> ChatColor.GRAY + "Guilds are in");
        addVariable("guild.2", player -> ChatColor.GRAY + "progress.");
        addVariable("guild.3", player -> ChatColor.GRAY + "");
        addVariable("guild.4", player -> ChatColor.GRAY + "Check back later for updates");
        addVariable("guild.5", player -> ChatColor.GRAY + "");
        addVariable("guild.6", player -> ChatColor.GRAY + "");
        addVariable("guild.7", player -> ChatColor.GRAY + "");
        for (int i = 8; i < SLOTS_PER_COLUMN; i++) {
            addVariable("guild." + i, player -> "");
        }

        // Friends Page.
        for (int i = 0; i < SLOTS_PER_COLUMN; i++) {
            int slot = i;
            addVariable("friends." + slot, player -> {
                List<UUID> friends = FriendsController.getInstance().getFriends(player.getUniqueId());
                if (friends.isEmpty()) {
                    // Player has no friends.
                    return (slot == 0 ? "Type " + ChatColor.GREEN + "/friends add" + ChatColor.GRAY + " to add someone" : (slot == 1 ? "to your friends list." : ""));
                }

                List<String> online = new ArrayList<>();
                for (UUID friend : friends) {
                    if (Characters.isPlayerCharacterLoaded(friend)) {
                        online.add(player.getDisplayName());
                    }
                }

                if (slot >= online.size()) {
                    return slot == 0 ? "No online friends." : "";
                }

                return (ChatColor.GREEN + " ⦿ ") + online.get(slot);
            });
        }


        // Character Page:
        addVariable("playerClass", player -> {
            if (Characters.isPlayerCharacterLoaded(player)) {
                return Characters.getPlayerCharacter(player).getPlayerClass().getUserFriendlyName();
            }
            return "none";
        });
        addNumeric("playerLevel", player -> {
            if (Characters.isPlayerCharacterLoaded(player)) {
                return Characters.getPlayerCharacter(player).getExperience().getLevel();
            }
            return 0;
        });
        addVariable("playerExperience", player -> {
            if (Characters.isPlayerCharacterLoaded(player)) {
                Experience exp = Characters.getPlayerCharacter(player).getExperience();
                return exp.getExperienceForNextLevel() + "/" + exp.getExperienceTowardsNextLevel() + "(" + exp.getPercentageTowardsNextLevel() + ")";
            }
            return "0";
        });
    }

    /**
     * Add a numeric variable that will get sent to BungeeTabListPlus.
     * @param variable
     * @param getter
     */
    public void addNumeric(String variable, Function<Player, Integer> getter) {
        addVariable(variable, player -> String.valueOf(getter.apply(player)));
    }

    /**
     * Register a variable that will get sent to BungeeTabListPlus.
     * @param variable
     * @param getter
     */
    public void addVariable(String variable, Function<Player, String> getter) {
        BungeeTabListPlusBukkitAPI.registerVariable(LegendsOfValeros.getInstance(), new Variable(variable) {
            @Override
            public String getReplacement(Player player) {
                if (!Characters.isPlayerCharacterLoaded(player)) {
                    return "?";
                }
                PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
                return playerCharacter != null ? getter.apply(player) : "?";
            }
        });
    }
}
