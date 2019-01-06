package com.legendsofvaleros.modules.playermenu.settings;

import com.codingforcookies.doris.query.InsertQuery;
import com.codingforcookies.doris.sql.TableManager;
import com.codingforcookies.robert.core.GUI.Flag;
import com.codingforcookies.robert.window.ExpandingGUI;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettings extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;
	public static class Manager implements Listener {
		private static final String SETTINGS_TABLE = "player_settings";
		private static final String PLAYER_ID = "player_id";
		private static final String SETTING_ID = "setting_id";
		private static final String SETTING_VALUE = "setting_value";

		private static TableManager manager;
		
		private static Map<UUID, PlayerSettings> settings = new HashMap<>();
		
		public Manager() {
			manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SETTINGS_TABLE);

			manager.primary(PLAYER_ID, "VARCHAR(36)")
					.primary(SETTING_ID, "VARCHAR(16)")
					.column(SETTING_VALUE, "TEXT").create();
		}
		
		public static ListenableFuture<PlayerSettings> get(Player p) {
			SettableFuture<PlayerSettings> ret = SettableFuture.create();
			
			if(settings.containsKey(p.getUniqueId())) {
				ret.set(settings.get(p.getUniqueId()));
			}else
				manager.query()
							.select()
								.where(PLAYER_ID, p.getUniqueId().toString())
							.build()
						.callback((statement, count) -> {
							ResultSet result = statement.getResultSet();

							PlayerSettings ps = new PlayerSettings();
							
							while(result.next())
								ps.put(result.getString(SETTING_ID), result.getString(SETTING_VALUE));

							settings.put(p.getUniqueId(), ps);
							ret.set(ps);
						})
					.execute(true);
			
			return ret;
		}
		
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerQuit(PlayerQuitEvent event) {
			PlayerSettings ps = settings.get(event.getPlayer().getUniqueId());
			
			if(ps == null || ps.size() == 0) return;
			
			InsertQuery insert = manager.query().insert().onDuplicateUpdate(SETTING_VALUE);
			
			for(Entry<String, String> entry : ps.entrySet()) {
				insert.values(PLAYER_ID, event.getPlayer().getUniqueId().toString(),
								SETTING_ID, entry.getKey(),
								SETTING_VALUE, entry.getValue());
				insert.addBatch();
			}
			
			insert.build().execute(true);
			
			settings.remove(event.getPlayer().getUniqueId());
		}
	}
	
	public static ListenableFuture<PlayerSettings> get(Player p) {
		return Manager.get(p);
	}

	public static void open(Player p) {
		PlayerSettingsOpenEvent event = new PlayerSettingsOpenEvent(p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if(event.isCancelled()) return;
		
		ExpandingGUI gui = new ExpandingGUI(p.getName(), event.getSlots()) {
			private ItemStack stack;
			@Override
			public void onOpen(Player p, InventoryView view) {
				p.getInventory().setItem(17, Model.merge(event.getSlots().size() <= 5 ? "menu-ui-hopper" : "menu-ui-3x3", (stack = p.getInventory().getItem(17))));
			}
			@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
		};
		gui.open(p, Flag.NO_PARENTS);
	}
}
