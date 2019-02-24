package com.legendsofvaleros.modules.skills.listener;

import com.codingforcookies.robert.item.ItemReader;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.hotswitch.event.PlayerSwitchHotbarEvent;
import com.legendsofvaleros.modules.hotswitch.event.PlayerUseHotbarEvent;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map.Entry;

public class HotbarListener implements Runnable, Listener {
    public HotbarListener() {
        Hotswitch.getInstance().registerEvents(this);
        Hotswitch.getInstance().getScheduler().executeInMyCircleTimer(this, 20L, 20L);
    }

    @EventHandler
    public void onSlotChanged(PlayerUseHotbarEvent e) {
        if (e.getSlot() < Hotswitch.SWITCHER_SLOT) {
            if (e.getPlayer().getInventory().getItem(e.getSlot()).isSimilar(Model.EMPTY_SLOT)) return;

            ItemReader reader = new ItemReader(e.getPlayer().getInventory().getItem(e.getSlot()));
            if (reader.nbt == null) return;

            PlayerCharacter pc = Characters.getPlayerCharacter(e.getPlayer());
            Entry<Skill, Integer> skillPair = pc.getSkillSet().get(reader.nbt.getString("skill"));
            SkillsController.castSkill(CombatEngine.getEntity(e.getPlayer()), skillPair.getKey(), skillPair.getValue());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!Characters.isPlayerCharacterLoaded((Player) e.getWhoClicked())) return;

        if (e.getClickedInventory() == null)
            return;

        if (e.getClickedInventory() != e.getWhoClicked().getInventory())
            return;

        if (e.isRightClick() && e.getSlot() < Hotswitch.SWITCHER_SLOT) {
            SkillsController.getInstance().updateSkillBar(Characters.getPlayerCharacter((Player) e.getWhoClicked()), Hotswitch.getInstance().getCurrentHotbar(e.getWhoClicked().getUniqueId()) * Hotswitch.SWITCHER_SLOT + e.getSlot(), null);
            e.getClickedInventory().setItem(e.getSlot(), Model.EMPTY_SLOT);
        }
    }

    @EventHandler
    public void onPlayerSwitchbar(PlayerSwitchHotbarEvent e) {
        PlayerCharacter pc = Characters.getPlayerCharacter(e.getPlayer());
        int newBar = (e.getCurrentHotbar() + 1) % 2;

        fillBar(pc, newBar);
        e.setCurrentHotbar(newBar);
    }

    @EventHandler
    public void onFillInventory(PlayerCharacterInventoryFillEvent e) {
        fillBar(e.getPlayerCharacter(), Hotswitch.getInstance().getCurrentHotbar(e.getPlayer().getUniqueId()));
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!Characters.isPlayerCharacterLoaded(p)) continue;

            PlayerCharacter pc = Characters.getPlayerCharacter(p);
            fillBar(pc, Hotswitch.getInstance().getCurrentHotbar(pc.getPlayer().getUniqueId()));
        }
    }

    public void fillBar(PlayerCharacter pc, int bar) {
        bar = bar * Hotswitch.SWITCHER_SLOT;

        {
            ItemStack stack = pc.getPlayer().getInventory().getItem(Hotswitch.SWITCHER_SLOT);
            if (stack == null || !stack.isSimilar(Model.EMPTY_SLOT))
                pc.getPlayer().getInventory().setItem(Hotswitch.SWITCHER_SLOT, Model.EMPTY_SLOT);
        }

        ItemStack invStack, stack;
        for (int i = 0; i < Hotswitch.SWITCHER_SLOT; i++) {
            invStack = pc.getPlayer().getInventory().getItem(i);

            String skillId = SkillsController.getInstance().getSkillBarSlot(pc, bar + i);
            Skill skill = Skill.getSkillById(skillId);

            if(skill == null) {
                SkillsController.getInstance().updateSkillBar(pc, bar + i, null);
                invStack = null;
            }

            if(invStack == null)
                stack = Model.EMPTY_SLOT;
            else{
                stack = SkillsController.getStackForSkillCooldown(pc,
                        new AbstractMap.SimpleImmutableEntry<>(
                                skill,
                                pc.getSkillSet().getLevel(skillId)));
            }

            if (invStack == null || stack.getAmount() != invStack.getAmount() || !stack.isSimilar(invStack))
                pc.getPlayer().getInventory().setItem(i, stack);
        }
    }
}