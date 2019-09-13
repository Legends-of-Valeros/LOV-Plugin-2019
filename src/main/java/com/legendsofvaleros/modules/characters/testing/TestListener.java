package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.*;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineRegenEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.npcs.trait.CitizensTraitLOV;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.events.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.playermenu.events.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.CustomEntityFirework;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.model.Models;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TestListener implements Listener {
    public TestListener() {
        Characters.getInstance().registerEvents(this);
        Characters.getInstance().registerEvents(new PlayerBookListener());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        event.getPlayer().setExp(0);
        event.getPlayer().setLevel(0);
        event.getPlayer().setHealth(20);
        event.getPlayer().setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMenuOpen(PlayerMenuOpenEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        if (Characters.getInstance().isInCombat(event.getPlayer())) {
            MessageUtil.sendError(event.getPlayer(), "You cannot do that while in combat!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        event.addSlot(Models.stack("menu-characters-button").setName("Character Selection").create(), (gui, p, event1) -> Characters.openCharacterSelection(p));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFillInventory(PlayerCharacterInventoryFillEvent e) {
        InventoryManager.fillInventory(e.getPlayer());
    }

    @EventHandler
    public void onPlayerCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS)) {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
        }

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0F);

    }

    @EventHandler
    public void onPlayerCharacterFinishLoading(final PlayerCharacterFinishLoadingEvent event) {
        event.getPlayer().setLevel(event.getPlayerCharacter().getExperience().getLevel());
        event.getPlayer().setExp((float) event.getPlayerCharacter().getExperience().getPercentageTowardsNextLevel());
    }

    @EventHandler
    public void onPlayerCharacterLogout(final PlayerCharacterLogoutEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1), true);
    }

    @EventHandler
    public void onRegenHealth(CombatEngineRegenEvent event) {
        if (!event.getCombatEntity().isPlayer()) {
            return;
        }
        if (event.getRegenerating() != RegeneratingStat.HEALTH) {
            return;
        }

        if (Characters.getInstance().isInCombat((Player) event.getCombatEntity().getLivingEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNPCLeftClick(NPCLeftClickEvent event) {
        if (!event.getNPC().hasTrait(CitizensTraitLOV.class)) {
            return;
        }
        if (!Characters.getInstance().isInCombat(event.getClicker())) {
            return;
        }

        MessageUtil.sendError(event.getClicker(), "You cannot do that while in combat!");
        event.setCancelled(true);
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().hasTrait(CitizensTraitLOV.class)) {
            return;
        }
        if (!Characters.getInstance().isInCombat(event.getClicker())) {
            return;
        }

        MessageUtil.sendError(event.getClicker(), "You cannot do that while in combat!");
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
        ((Player) event.getEntity()).setSaturation(20F);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onValerosExperienceChange(PlayerCharacterExperienceChangeEvent event) {
        Player player = event.getPlayer();
        MessageUtil.sendActionBar(player, ChatColor.AQUA + "+" + event.getChange() + "xp");

        Characters.getInstance().getScheduler().executeInSpigotCircle(() -> {
            player.setExp((float) event.getPlayerCharacter().getExperience().getPercentageTowardsNextLevel());
        });
    }

    @EventHandler
    public void onValerosLevelUp(PlayerCharacterLevelChangeEvent event) {
        Player player = event.getPlayer();
        MessageUtil.sendActionBar(player, ChatColor.YELLOW + "You have leveled up!");

        player.setLevel(event.getNewLevel());
        player.setExp(0F);

        //send a message to party
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player.getUniqueId());
        PlayerParty party = PartiesController.getInstance().getPartyByMember(playerCharacter.getUniqueCharacterId());
        if (party != null) {
            party.sendMessageToParty(playerCharacter.getPlayer().getDisplayName() + " is now level " + event.getNewLevel());
        }

        // Get firework builder
        FireworkEffect.Builder builder = FireworkEffect.builder();

        // Create a firework effect with the builder
        FireworkEffect effect = builder.flicker(false).trail(false).with(FireworkEffect.Type.BALL).withColor(Color.RED).withFade(Color.BLUE).build();

        // Spawn our firework
        CustomEntityFirework.spawn(player.getLocation(), effect);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
    }

}
