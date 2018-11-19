package com.legendsofvaleros.modules.fast_travel;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscoveredFastTravels implements Listener {
	@Table(name = "player_fast_travel_found")
	public static class Pair {
		@Column(primary = true, name = "character_id")
		private CharacterId characterId;

		@Column(primary = true, name = "travel_id", length = 32)
		private String travelId;

		public Pair(CharacterId characterId, String travelId) {
			this.characterId = characterId;
			this.travelId = travelId;
		}
	}

	private static ORMTable<Pair> manager;
	
	protected static Multimap<CharacterId, String> fastTravels = HashMultimap.create();
	public static Collection<String> getDiscovered(PlayerCharacter pc) { return fastTravels.get(pc.getUniqueCharacterId()); }
	
	public static void onEnable() {
		manager = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Pair.class);

		FastTravel.getInstance().registerEvents(new PlayerCharacterListener());
	}

	public static void add(PlayerCharacter pc, String travel_id) {
		fastTravels.put(pc.getUniqueCharacterId(), travel_id);
	}

	private static ListenableFuture<Void> loadFoundTravels(PlayerCharacter pc) {
		final SettableFuture<Void> ret = SettableFuture.create();

		manager.query().get(pc.getUniqueCharacterId())
				.forEach((pair) -> fastTravels.put(pc.getUniqueCharacterId(), pair.travelId))
				.onFinished(() -> ret.set(null))
				.execute(true);

		return ret;
	}

	public static void onLogout(CharacterId characterId) {
		List<Pair> travels = new ArrayList<>();

		for(String id : fastTravels.get(characterId))
			travels.add(new Pair(characterId, id));

		if(travels.size() == 0) return;

		manager.saveAll(travels, true)
				.addListener(() -> fastTravels.removeAll(characterId), Utilities.asyncExecutor());
	}
	
	public static void removeAll(PlayerCharacter pc) {
		fastTravels.removeAll(pc.getUniqueCharacterId());
		
		manager.delete(pc.getUniqueCharacterId(), true);
	}

	private static class PlayerCharacterListener implements Listener {
		@EventHandler
		public void onCharacterLoading(PlayerCharacterStartLoadingEvent event) {
			PhaseLock lock = event.getLock();
			loadFoundTravels(event.getPlayerCharacter())
				.addListener(lock::release, Utilities.asyncExecutor());
		}

		@EventHandler
		public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
			onLogout(event.getPlayerCharacter().getUniqueCharacterId());
		}

		@EventHandler
		public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
			removeAll(event.getPlayerCharacter());
		}
	}
}