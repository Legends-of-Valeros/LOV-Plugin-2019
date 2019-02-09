package com.legendsofvaleros.modules.skills.gui.recharge;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.gui.ItemMorphGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.ItemUtil;
import com.legendsofvaleros.modules.gear.component.skills.GearCharge;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class TraitRecharger extends LOVTrait {
	long cost = 1;

	@Override
	public void onRightClick(Player player, SettableFuture<Slot> slot) {
		if(!Characters.isPlayerCharacterLoaded(player)) {
			slot.set(null);
			return;
		}
		
		slot.set(new Slot(new ItemBuilder(Material.DIAMOND).setName("Charge Foci").create(), (gui, p, event) -> {
			gui.close(p);
			
			new RechargerView(cost).open(p);
		}));
	}
	
	private class RechargerView extends ItemMorphGUI {
		private ItemStack stack;
		@Override public void onOpen(Player p, InventoryView view) { stack = p.getInventory().getItem(17); applyStacked(p); }
		@Override public void onClose(Player p, InventoryView view) { super.onClose(p, view); p.getInventory().setItem(17, stack); }
		public void applyStacked(Player p) { p.getInventory().setItem(17, Model.merge("menu-ui-recharger", stack)); }
		
		double costFactor;
		
		public RechargerView(double costFactor) {
			super("ᚲᚱᛁᛗᛖᚾ ᛗᚨᚷᛁᚲᚨᛖ");
			
			this.costFactor = costFactor;
		}
		
		@Override
		public boolean isBuy() {
			return true;
		}

		@Override
		public boolean isValid(Gear.Instance item) {
			if(!item.hasComponent(GearCharge.Component.class))
				return false;
			return item.getPersist(GearCharge.Component.class).max > 0;
		}

		@Override
		public long getWorth(Gear.Instance item) {
			Integer max = item.getPersist(GearCharge.Component.class).max;
			Integer current = item.getPersist(GearCharge.Component.class).current;
			return (int)Math.ceil((max - current) * costFactor);
		}

		@Override
		public void executeMorph(PlayerCharacter pc, Gear.Instance item) {
			ChargeItemEvent repairEvent = new ChargeItemEvent(pc.getPlayer(), item);
			Bukkit.getPluginManager().callEvent(repairEvent);

			GearCharge.Persist persist = item.getPersist(GearCharge.Component.class);
			persist.current = persist.max;
			
			ItemUtil.giveItem(pc, item);
		}

		@Override
		public void onCompleted(PlayerCharacter pc) {
			applyStacked(pc.getPlayer());

			pc.getPlayer().playSound(pc.getLocation(), "ui.charge", 1F, 1F);
		}
	}
}