package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.config.CharactersConfig;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityPreCreateEvent;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A per-player collection of player characters.
 */
public class PlayerCharacterCollection implements PlayerCharacters {

	// TODO test character ordering. if it does not work, use an ordered map and let characterid
	// implement comparable on the basis of character number

	private final UUID playerId;
	private final WeakReference<Player> player;
	private CharactersConfig config;

	private int maxCharacters = 0;
	private ReusablePlayerCharacter current;
	private Map<CharacterId, ReusablePlayerCharacter> characters;

	private boolean hasQuit;

	PlayerCharacterCollection(Player player, List<ReusablePlayerCharacter> charactersFromDb) {
		this.playerId = player.getUniqueId();
		this.player = new WeakReference<>(player);
		this.config = Characters.getInstance().getCharacterConfig();
		
		for(PermissionAttachmentInfo entry : player.getEffectivePermissions()) {
			if(entry.getPermission().startsWith("character.slots.")) {
				int i = Integer.parseInt(entry.getPermission().split("character.slots.", 2)[1]);
				if(i > maxCharacters)
					maxCharacters = i;
			}
		}

		this.characters = new LinkedHashMap<>();

		for (ReusablePlayerCharacter pc : charactersFromDb) {
			characters.put(pc.getUniqueCharacterId(), pc);
		}

		Characters.getInstance().registerEvents(new PlayerListener());
	}

	@Override
	public UUID getPlayerId() {
		return playerId;
	}

	@Override
	public Player getPlayer() {
		return player.get();
	}
	
	@Override
	public int getMaxCharacters() {
		return maxCharacters;
	}
	
	@Override
	public PlayerCharacter getCurrentCharacter() {
		if(current != null)
			return current;
		throw new RuntimeException("User " + player.get().getName() + " does not have a Character loaded.");
	}
	
	@Override
	public boolean isCharacterLoaded() {
		return current != null;
	}

	@Override
	public int size() {
		return characters.size();
	}

	@Override
	public Set<PlayerCharacter> getCharacterSet() {
		return new LinkedHashSet<>(characters.values());
	}

	public boolean removeCharacter(int characterNumber) {
		PlayerCharacter pc = getForNumber(characterNumber);
		if(pc == null) return false;

		characters.remove(new CharacterId(playerId, characterNumber));
		PlayerCharacterData.remove(playerId, characterNumber);

		return !characters.containsKey(new CharacterId(playerId, characterNumber));
	}

	@Override
	public PlayerCharacter getForNumber(int characterNumber) {
		return characters.get(new CharacterId(playerId, characterNumber));
	}

	@Override
	public PlayerCharacter getForId(CharacterId id) {
		return characters.get(id);
	}

	/**
	 * Adds a newly created character to this collection.
	 * 
	 * @param playerRace The selected race of the new character.
	 * @param playerClass The selected class of the new character.
	 * @return The newly created character.
	 */
	public PlayerCharacter addNewCharacter(int number, EntityRace playerRace, EntityClass playerClass) {
		ReusablePlayerCharacter ret =
				new ReusablePlayerCharacter(player.get(), number, playerRace, playerClass,
						player.get().getLocation(),
						new CharacterExperience(0, 0L), new PlayerInventoryData(), new ArrayList<>());

		characters.put(ret.getUniqueCharacterId(), ret);
		PlayerCharacterData.save(ret);

		return ret;
	}

	/**
	 * Gets which characters in this collection are different from when they were added/constructed.
	 * 
	 * @return The characters who have changes that should be written to the database, if any.
	 */
	Set<ReusablePlayerCharacter> getChanged() {
		Set<ReusablePlayerCharacter> ret = new HashSet<>();

		for (ReusablePlayerCharacter pc : characters.values()) {
			if (pc.hasChanged()) {
				ret.add(pc);
			}
		}

		return ret;
	}

	void onQuit() {
		quit();
	}

	private void quit() {
		if (!hasQuit) {
			hasQuit = true;
			for (ReusablePlayerCharacter character : characters.values()) {
				character.onQuit();
			}
		}
	}

	/**
	 * Listens to events that affect this player's characters.
	 */
	private class PlayerListener implements Listener {

		@EventHandler(priority = EventPriority.LOWEST)
		public void onPlayerCharacterDoneLoading(PlayerCharacterFinishLoadingEvent event) {
			if (event.getPlayer().getUniqueId().equals(playerId)) {

				ReusablePlayerCharacter pc =
						characters.get(event.getPlayerCharacter().getUniqueCharacterId());

				if (pc != null) {
					pc.setCurrent(true);
					current = pc;
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerCharacterLogout(PlayerCharacterLogoutEvent event) {
			if (event.getPlayer().getUniqueId().equals(playerId)) {

				ReusablePlayerCharacter pc =
						characters.get(event.getPlayerCharacter().getUniqueCharacterId());

				if (pc != null) {
					pc.setCurrent(false);
					if (pc.equals(current)) {
						current = null;
					}
				}

				if (event.isServerLogout()) {
					HandlerList.unregisterAll(this);
					quit();
				}
			}
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onCombatEntityCreate(CombatEntityCreateEvent event) {
			if (event.getCombatEntity().getUniqueId().equals(playerId) && current != null) {
				current.onCombatEntityCreate(event.getCombatEntity());
			}
		}

		@EventHandler
		public void onCombatEntityPreCreate(CombatEntityPreCreateEvent event) {
			if (event.getLivingEntity().getUniqueId().equals(playerId) && current != null) {
				try {
					Archetype arch = config.getClassConfig(current.getPlayerClass()).getArchetype();
					CombatProfile profile = arch.getCombatProfile(current.getExperience().getLevel());
					event.setCombatProfile(profile);
				} catch (Exception e) {
					Characters.getInstance().getLogger()
					.severe("Encountered an issue while initializing a player's combat entity.");
				}
			}
		}

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			if (event.getEntity().getUniqueId().equals(playerId) && current != null) {
				current.onDeath(event);
			}
		}
	}

}
