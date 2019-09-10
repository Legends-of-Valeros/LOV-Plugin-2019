package com.legendsofvaleros.modules.gear.listener;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.window.WindowYesNo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.VanillaDamageCancelledEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.GearType;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.trigger.*;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger.TriggerEvent;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemListener implements Listener {
    public static final Cache<UUID, UUID> itemOwner = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEquipItemSound(ItemEquipEvent event) {
        if (!event.isCancelled())
            event.getPlayer().playSound(event.getPlayer().getLocation(), "ui.accessory.equip", 1F, 1F);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Gear.Instance instance = Gear.Instance.fromStack(event.getItemDrop().getItemStack());

        // We should never drop items on the ground.
        event.setCancelled(true);
        //TODO test if the drops should be removed from the event

        if (instance == null) {
            return;
        }

        // Only allow item destruction if the item is tradable
        if (instance.gear.getType().isTradable()) {
            GearController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                // ?Should this be moved to its own class?
                new WindowYesNo("Destroy Item") {
                    @Override
                    public void onAccept(GUI gui, Player p) {
                        gui.close(p);

                        if (!ItemUtil.removeItem(event.getPlayer(), instance)) {
                            MessageUtil.sendError(event.getPlayer(), "Unable to remove that, for some reason...");
                        } else {
                            MessageUtil.sendUpdate(event.getPlayer(), instance.gear.getName() + " has been destroyed.");
                        }
                    }

                    @Override
                    public void onDecline(GUI gui, Player p) {
                        gui.close(p);
                    }

                    @Override
                    public void onClickPlayerInventory(GUI gui, Player p, InventoryClickEvent event) {
                        // Disallow all selections in the inventory for this UI.
                        // This will prevent edge cases where the item gets used
                        // up while this UI is open.

                        event.setCancelled(true);
                    }
                }.open(event.getPlayer());
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;

        event.setCancelled(true);

        if (!Characters.isPlayerCharacterLoaded((Player) event.getEntity())) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player) event.getEntity());
        UUID player = itemOwner.getIfPresent(event.getItem().getUniqueId());

        // Make sure the current owner of the item entity is the same as the entity attempting to pick up said item.
        if (player == null || player.compareTo(pc.getPlayerId()) == 0) {
            event.getItem().remove();

            Gear.Instance instance = Gear.Instance.fromStack(event.getItem().getItemStack());
            if (instance != null) {
                ItemUtil.giveItem(pc, instance);
                // Dont log currency drops since they are logged on their own.
                // TODO: this should made cleaner
                if (instance.gear.getModelId().toLowerCase().contains("crown")) {
                    return;
                }

                MessageUtil.sendUpdate(pc.getPlayer(), "Picked up " + ChatColor.WHITE + ChatColor.BOLD + instance.gear.getName());
            }
        }
    }

    /**
     * This listens for the vanilla cancellation, rather than the Combat Engine event, so that we can entirely
     * prevent Combat Engine from doing any computations at all.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(VanillaDamageCancelledEvent vEvent) {
        EntityDamageEvent dEvent = vEvent.getCancelledEvent();
        if (!(dEvent instanceof EntityDamageByEntityEvent))
            return;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) dEvent;

        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        CombatEntity ace = CombatEngine.getEntity((LivingEntity) e.getDamager());
        if (ace == null || !ace.isPlayer()) return;

        CombatEntity dce = CombatEngine.getEntity((LivingEntity) e.getEntity());
        if (dce == null) return;

        // Disallow doing damage to any entity if the player is not over their equipped item slot.
        if (((Player) ace.getLivingEntity()).getInventory().getHeldItemSlot() != Hotswitch.HELD_SLOT) {
            vEvent.setCancelled(true);

            // Prevent a message being sent when left clicking while holding a spell
            if (((Player) ace.getLivingEntity()).getInventory().getHeldItemSlot() < Hotswitch.HELD_SLOT) {
                // ?Do we even need a message to be sent?
                MessageUtil.sendError(ace.getLivingEntity(), "You may only attack using your equipped item slot!");
            }
        }
    }

    @EventHandler
    public void onItemCombine(InventoryClickEvent e) {
        if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;

        Gear.Instance agent = Gear.Instance.fromStack(e.getCursor());
        if (agent == null) return;

        Gear.Instance base = Gear.Instance.fromStack(e.getCurrentItem());
        if (base == null) return;

        // Combine is triggered when an item is held on the cursor and another item is clicked in the inventory.

        CombineTrigger trigger = new CombineTrigger(CombatEngine.getEntity(e.getWhoClicked()), base, agent);

        // Disable the event if and only if the trigger has a state of True
        if (Boolean.TRUE.equals(agent.doTest(trigger))) {
            if (agent.doFire(trigger).didChange()) {
                e.setCurrentItem(base.toStack());
                e.getView().setCursor(agent.toStack());
            }

            e.setCancelled(true);
        }
    }

    /**
     * This click event only handles the events for offhand and the held equip slot.
     *
     * ?Should this be moved to a dedicated class?
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryMove(InventoryClickEvent e) {
        if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;
        if (!Characters.isPlayerCharacterLoaded((Player) e.getWhoClicked())) return;

        boolean offhand = e.getSlot() == 40;
        if (e.getSlot() != Hotswitch.HELD_SLOT && !offhand) return;

        Gear.Instance gearInstance;

        if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
            gearInstance = Gear.Instance.fromStack(e.getCursor());
            if (gearInstance == null) {
                // If the item did not decode successfully into a Gear instance, then it no longer exists.
                // Replace it with the error item instance.
                MessageUtil.sendError(e.getWhoClicked(), "Your item has morphed into... something.");

                Gear.Instance instance = GearController.ERROR_ITEM.newInstance();
                instance.amount = e.getView().getCursor().getAmount();
                e.getView().setCursor(instance.toStack());
                e.setCancelled(true);
                return;
            }

            // If the offhand slot is clicked, only allow shields there.
            // If the equip slot is clicked, only allow weapons there.
            if ((!offhand && gearInstance.gear.getType() != GearType.WEAPON)
                    || (offhand && gearInstance.gear.getType() != GearType.SHIELD)) {
                MessageUtil.sendError(e.getWhoClicked(), "You can't wield that item there.");

                e.setCancelled(true);
                return;
            }

            ItemEquipEvent iee = new ItemEquipEvent(Characters.getPlayerCharacter((Player) e.getWhoClicked()), gearInstance, !offhand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            Bukkit.getPluginManager().callEvent(iee);
            if (iee.isCancelled()) {
                MessageUtil.sendError(e.getWhoClicked(), "A mysterious force prevents you from equipping that.");

                e.setCancelled(true);
                return;
            }
        }

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            gearInstance = Gear.Instance.fromStack(e.getCurrentItem());
            if (gearInstance == null) {
                // If the item did not decode successfully into a Gear instance, then it no longer exists.
                // Replace it with the error item instance.
                MessageUtil.sendError(e.getWhoClicked(), "Your item has morphed into.. something.");

                e.setCancelled(true);
                e.setCurrentItem(GearController.ERROR_ITEM.newInstance().toStack());

                Gear.Instance instance = GearController.ERROR_ITEM.newInstance();
                instance.amount = e.getCurrentItem().getAmount();
                e.setCurrentItem(instance.toStack());

                return;
            }

            Bukkit.getPluginManager().callEvent(new ItemUnEquipEvent(Characters.getPlayerCharacter((Player) e.getWhoClicked()), gearInstance, !offhand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND));
        }
    }

    @EventHandler
    public void onEquipItem(ItemEquipEvent event) {
        // Fire the equip trigger in the gear.
        EquipTrigger e = new EquipTrigger(CombatEngine.getEntity(event.getPlayer()));
        if (Boolean.FALSE.equals(event.getGear().doTest(e))) {
            event.setCancelled(true);
            return;
        }
        event.getGear().doFire(e);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnEquipItem(ItemUnEquipEvent event) {
        // Fire the unequip trigger in the gear.
        // ?This event cannot currently be cancelled. Do we want it to be?
        event.getGear().doFire(new UnEquipTrigger(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLivingEntityDamage(CombatEnginePhysicalDamageEvent event) {
        Gear.Instance gearInstance = Gear.Instance.fromStack(event.getAttacker().getLivingEntity().getEquipment().getItemInMainHand());
        if (gearInstance == null) return;

        PhysicalAttackTrigger e = new PhysicalAttackTrigger(event.getAttacker());

        if (Boolean.FALSE.equals(gearInstance.doTest(e))) {
            MessageUtil.sendError(event.getAttacker().getLivingEntity(), "Something prevents you from doing that!");

            event.setCancelled(true);
        } else {
            if (gearInstance.doFire(e) == TriggerEvent.REFRESH_STACK && event.getAttacker().isPlayer())
                event.getAttacker().getLivingEntity().getEquipment().setItemInMainHand(gearInstance.toStack());

            event.newDamageModifierBuilder("GearController")
                    .setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT)
                    .setValue(e.getDamage())
                    .build();
        }
    }

    /**
     * Fires the defend event for all worn armor on damage. This does not include shields.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLivingEntityDamaged(CombatEngineDamageEvent event) {
        DefendTrigger e = new DefendTrigger(event);

        ItemStack[] armor = event.getDamaged().getLivingEntity().getEquipment().getArmorContents();
        Gear.Instance instance;
        for (int i = 0; i < armor.length; i++) {
            instance = Gear.Instance.fromStack(armor[i]);
            if (instance != null)
                if (Boolean.TRUE.equals(instance.doTest(e))) {
                    if (instance.doFire(e).didChange())
                        armor[i] = instance.toStack();
                }
        }
        event.getDamaged().getLivingEntity().getEquipment().setArmorContents(armor);
    }

    /**
     * Handles the item usage trigger for gear.
     */
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Gear.Instance gearInstance = Gear.Instance.fromStack(event.getItem());
            if (gearInstance == null) return;

            UseTrigger e = new UseTrigger(event);

            Boolean test = gearInstance.doTest(e);
            if (test == null) return;

            if (test) {
                if (gearInstance.doFire(e) == TriggerEvent.REFRESH_STACK) {
                    if (event.getHand() == EquipmentSlot.HAND)
                        event.getPlayer().getInventory().setItemInMainHand(gearInstance.toStack());
                    else if (event.getHand() == EquipmentSlot.OFF_HAND)
                        event.getPlayer().getInventory().setItemInOffHand(gearInstance.toStack());
                }
            }
        }
    }

    /**
     * Fires the respective trigger for all armor equipped or unequipped.
     */
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        Gear.Instance gearInstance = Gear.Instance.fromStack(event.getNewArmorPiece());
        if (gearInstance != null) {
            ItemEquipEvent iee = new ItemEquipEvent(Characters.getPlayerCharacter(event.getPlayer()), gearInstance, null);
            Bukkit.getPluginManager().callEvent(iee);
            if (iee.isCancelled()) {
                MessageUtil.sendError(event.getPlayer(), "A mysterious force prevents you from equipping that.");

                event.setCancelled(true);

                return;
            }
        }

        gearInstance = Gear.Instance.fromStack(event.getOldArmorPiece());
        if (gearInstance != null)
            Bukkit.getPluginManager().callEvent(new ItemUnEquipEvent(Characters.getPlayerCharacter(event.getPlayer()), gearInstance, null));
    }

    /**
     * Fires the equip triggers when an entity is created, should they be wearing or holding equipment.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCreated(CombatEntityCreateEvent event) {
        if (!event.getCombatEntity().isPlayer()) return;

        Player p = (Player) event.getLivingEntity();
        EquipTrigger e = new EquipTrigger(event.getCombatEntity());

        Gear.Instance instance;
        for (ItemStack stack : p.getInventory().getArmorContents()) {
            instance = Gear.Instance.fromStack(stack);
            if (instance != null) instance.doFire(e);
        }

        instance = Gear.Instance.fromStack(p.getInventory().getItem(Hotswitch.HELD_SLOT));
        if (instance != null) instance.doFire(e);

        instance = Gear.Instance.fromStack(p.getInventory().getItemInOffHand());
        if (instance != null) instance.doFire(e);
    }

    /**
     * Fires the unequip triggers when a player character logs out.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
        CombatEntity ce = CombatEngine.getEntity(event.getPlayer());

        UnEquipTrigger e = new UnEquipTrigger(ce);

        Gear.Instance instance;
        for (ItemStack stack : event.getPlayer().getInventory().getArmorContents()) {
            instance = Gear.Instance.fromStack(stack);
            if (instance != null) instance.doFire(e);
        }

        instance = Gear.Instance.fromStack(event.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
        if (instance != null) instance.doFire(e);

        instance = Gear.Instance.fromStack(event.getPlayer().getInventory().getItemInOffHand());
        if (instance != null) instance.doFire(e);
    }
}