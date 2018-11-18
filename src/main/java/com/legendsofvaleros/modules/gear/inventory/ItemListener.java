package com.legendsofvaleros.modules.gear.inventory;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.window.WindowYesNo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.VanillaDamageCancelledEvent;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.component.trigger.*;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger.TriggerEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemListener implements Listener {
    public static final Cache<UUID, UUID> itemOwner = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public ItemListener(LegendsOfValeros plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEquipItemSound(ItemEquipEvent event) {
        if (!event.isCancelled())
            event.getPlayer().playSound(event.getPlayer().getLocation(), "ui.accessory.equip", 1F, 1F);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        GearItem.Instance instance = GearItem.Instance.fromStack(event.getItemDrop().getItemStack());

        event.getItemDrop().setItemStack(null);
        event.getItemDrop().remove();

        if (instance == null) return;

        if (!instance.gear.getType().isTradable()) {
            ItemUtil.giveItem(Characters.getPlayerCharacter(event.getPlayer()), instance);
        } else {
            Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> new WindowYesNo("Destroy Item") {
                @Override
                public void onOpen(Player p, InventoryView view) {
                    view.setCursor(null);
                }

                @Override
                public void onAccept(GUI gui, Player p) {
                    gui.close(p);
                }

                @Override
                public void onDecline(GUI gui, Player p) {
                    gui.close(p);

                    ItemUtil.giveItem(Characters.getPlayerCharacter(event.getPlayer()), instance);
                }
            }.open(event.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;

        event.setCancelled(true);

        if (!Characters.isPlayerCharacterLoaded((Player) event.getEntity())) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player) event.getEntity());
        UUID player = itemOwner.getIfPresent(event.getItem().getUniqueId());
        if (player == null || player.compareTo(pc.getPlayerId()) == 0) {
            event.getItem().remove();

            GearItem.Instance instance = GearItem.Instance.fromStack(event.getItem().getItemStack());
            if (instance != null)
                ItemUtil.giveItem(pc, instance);
        }
    }

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

        if (((Player) ace.getLivingEntity()).getInventory().getHeldItemSlot() != Hotswitch.HELD_SLOT) {
            vEvent.setCancelled(true);

            MessageUtil.sendError(ace.getLivingEntity(), "You may only attack using your equipped item slot!");
        }
    }

    @EventHandler
    public void onItemCombine(InventoryClickEvent e) {
        if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;

        GearItem.Instance agent = GearItem.Instance.fromStack(e.getCursor());
        if (agent == null) return;

        GearItem.Instance base = GearItem.Instance.fromStack(e.getCurrentItem());
        if (base == null) return;

        CombineTrigger trigger = new CombineTrigger(CombatEngine.getEntity(e.getWhoClicked()), base, agent);

        if (Boolean.TRUE.equals(agent.doTest(trigger))) {
            if (agent.doFire(trigger).didChange()) {
                e.setCurrentItem(base.toStack());
                e.getView().setCursor(agent.toStack());
            }

            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryMove(InventoryClickEvent e) {
        if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;
        if (!Characters.isPlayerCharacterLoaded((Player) e.getWhoClicked())) return;

        boolean offhand = e.getSlot() == 40;
        if (e.getSlot() != Hotswitch.HELD_SLOT && !offhand) return;

        GearItem.Instance gear;

        if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
            gear = GearItem.Instance.fromStack(e.getCursor());
            if (gear == null) {
                if (!Utilities.isOp((Player) e.getWhoClicked())) {
                    MessageUtil.sendError(e.getWhoClicked(), "Your item has morphed into... something.");

                    e.setCancelled(true);
                    GearItem.Instance instance = Gear.ERROR_ITEM.newInstance();
                    instance.amount = e.getView().getCursor().getAmount();
                    e.getView().setCursor(instance.toStack());

                    e.setCancelled(true);
                }

                return;
            }

            if ((!offhand && gear.gear.getType() != GearType.WEAPON)
                    || (offhand && gear.gear.getType() != GearType.SHIELD)) {
                MessageUtil.sendError(e.getWhoClicked(), "You can't wield that item there.");

                e.setCancelled(true);
                return;
            }

            ItemEquipEvent iee = new ItemEquipEvent(Characters.getPlayerCharacter((Player) e.getWhoClicked()), gear, !offhand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            Bukkit.getPluginManager().callEvent(iee);
            if (iee.isCancelled()) {
                MessageUtil.sendError(e.getWhoClicked(), "A mysterious force prevents you from equipping that.");

                e.setCancelled(true);
                return;
            }
        }

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            gear = GearItem.Instance.fromStack(e.getCurrentItem());
            if (gear == null) {
                if (!Utilities.isOp((Player) e.getWhoClicked())) {
                    MessageUtil.sendError(e.getWhoClicked(), "Your item has morphed into.. something.");

                    e.setCancelled(true);
                    e.setCurrentItem(Gear.ERROR_ITEM.newInstance().toStack());

                    GearItem.Instance instance = Gear.ERROR_ITEM.newInstance();
                    instance.amount = e.getCurrentItem().getAmount();
                    e.setCurrentItem(instance.toStack());
                }
                return;
            }

            Bukkit.getPluginManager().callEvent(new ItemUnEquipEvent(Characters.getPlayerCharacter((Player) e.getWhoClicked()), gear));
        }
    }

    @EventHandler
    public void onEquipItem(ItemEquipEvent event) {
        EquipTrigger e = new EquipTrigger(CombatEngine.getEntity(event.getPlayer()));
        if (!Utilities.isOp(event.getPlayer())) {
            if (Boolean.FALSE.equals(event.getGear().doTest(e))) {
                event.setCancelled(true);
                return;
            }
        }
        event.getGear().doFire(e);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnEquipItem(ItemUnEquipEvent event) {
        event.getGear().doFire(new UnEquipTrigger(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLivingEntityDamage(CombatEnginePhysicalDamageEvent event) {
        GearItem.Instance gear = GearItem.Instance.fromStack(event.getAttacker().getLivingEntity().getEquipment().getItemInMainHand());
        if (gear == null) {
            // No weapon? No damage.
            event.setRawDamage(0);
            return;
        }

        PhysicalAttackTrigger e = new PhysicalAttackTrigger(event.getAttacker());

        if (Boolean.FALSE.equals(gear.doTest(e))) {
            MessageUtil.sendError(event.getAttacker().getLivingEntity(), "Something prevents you from doing that!");

            event.setCancelled(true);
        } else {
            if (gear.doFire(e) == TriggerEvent.REFRESH_STACK && event.getAttacker().isPlayer())
                event.getAttacker().getLivingEntity().getEquipment().setItemInMainHand(gear.toStack());
            event.setRawDamage(e.getDamage());
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onLivingEntityDamaged(CombatEngineDamageEvent event) {
        DefendTrigger e = new DefendTrigger(event);

        ItemStack[] armor = event.getDamaged().getLivingEntity().getEquipment().getArmorContents();
        GearItem.Instance instance;
        for (int i = 0; i < armor.length; i++) {
            instance = GearItem.Instance.fromStack(armor[i]);
            if (instance != null)
                if (Boolean.TRUE.equals(instance.doTest(e))) {
                    if (instance.doFire(e).didChange())
                        armor[i] = instance.toStack();
                }
        }
        event.getDamaged().getLivingEntity().getEquipment().setArmorContents(armor);
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            GearItem.Instance gear = GearItem.Instance.fromStack(event.getItem());
            if (gear == null) return;

            UseTrigger e = new UseTrigger(event);

            Boolean test = gear.doTest(e);
            if (test == null) return;
            if (test) {
                if (gear.doFire(e) == TriggerEvent.REFRESH_STACK) {
                    if (event.getHand() == EquipmentSlot.HAND)
                        event.getPlayer().getInventory().setItemInMainHand(gear.toStack());
                    else if (event.getHand() == EquipmentSlot.OFF_HAND)
                        event.getPlayer().getInventory().setItemInOffHand(gear.toStack());
                }
            }
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        GearItem.Instance gear = GearItem.Instance.fromStack(event.getNewArmorPiece());
        if (gear != null) {
            ItemEquipEvent iee = new ItemEquipEvent(Characters.getPlayerCharacter(event.getPlayer()), gear, null);
            Bukkit.getPluginManager().callEvent(iee);
            if (iee.isCancelled()) {
                if (Utilities.isOp(event.getPlayer())) {
                    MessageUtil.sendInfo(event.getPlayer(), "The gods have allowed you to wield that, operator.");
                } else {
                    MessageUtil.sendError(event.getPlayer(), "A mysterious force prevents you from equipping that.");

                    event.setCancelled(true);
                    return;
                }
            }
        }

        gear = GearItem.Instance.fromStack(event.getOldArmorPiece());
        if (gear != null)
            Bukkit.getPluginManager().callEvent(new ItemUnEquipEvent(Characters.getPlayerCharacter(event.getPlayer()), gear));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCreated(CombatEntityCreateEvent event) {
        if (!event.getCombatEntity().isPlayer()) return;

        Player p = (Player) event.getLivingEntity();
        EquipTrigger e = new EquipTrigger(event.getCombatEntity());

        GearItem.Instance instance;
        for (ItemStack stack : p.getInventory().getArmorContents()) {
            instance = GearItem.Instance.fromStack(stack);
            if (instance != null) instance.doFire(e);
        }

        instance = GearItem.Instance.fromStack(p.getInventory().getItem(Hotswitch.HELD_SLOT));
        if (instance != null) instance.doFire(e);

        instance = GearItem.Instance.fromStack(p.getInventory().getItemInOffHand());
        if (instance != null) instance.doFire(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
        CombatEntity ce = CombatEngine.getEntity(event.getPlayer());

        UnEquipTrigger e = new UnEquipTrigger(ce);

        GearItem.Instance instance;
        for (ItemStack stack : event.getPlayer().getInventory().getArmorContents()) {
            instance = GearItem.Instance.fromStack(stack);
            if (instance != null) instance.doFire(e);
        }

        instance = GearItem.Instance.fromStack(event.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
        if (instance != null) instance.doFire(e);

        instance = GearItem.Instance.fromStack(event.getPlayer().getInventory().getItemInOffHand());
        if (instance != null) instance.doFire(e);
    }
}