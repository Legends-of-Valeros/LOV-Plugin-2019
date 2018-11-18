package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.config.ExperienceConfig;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterExperienceChangeEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLevelUpEvent;
import com.legendsofvaleros.modules.characters.api.Experience;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;

/**
 * Tracks listener levels and the progress towards the next level for a player character.
 * <p>
 * Given an amount from the database on login which it tracks just relative changes to. On logout,
 * these relative edits should be saved to the database.
 */
public class CharacterExperience implements Experience {

	private final ExperienceConfig config;
	private PlayerCharacter playerCharacter;

	private int levelFromDb;
	private long xpFromDb;

	private int levelDifference;
	private long xpDifference;

	private long xpToLevel;
	private double multiplier;
	private int numZeroMultipliers;

	CharacterExperience(int levelFromDb, long xpFromDb) {
		this.config = Characters.inst().getConfig();

		this.levelFromDb = levelFromDb;
		this.xpFromDb = xpFromDb;
		this.multiplier = 1;

		refreshXpToLevel();
	}

	@Override
	public int getLevel() {
		return levelFromDb + levelDifference;
	}
	
	@Override
	public long getExperienceForNextLevel() {
		return xpToLevel;
	}

	@Override
	public long getExperienceTowardsNextLevel() {
		return xpFromDb + xpDifference;
	}

	@Override
	public double getPercentageTowardsNextLevel() {
		if(xpToLevel == 0) return 0;
		return (double) getExperienceTowardsNextLevel() / (double) xpToLevel;
	}

	@Override
	public void addExperience(long add, boolean ignoresMultipliers) {
		if (getLevel() >= config.getMaxLevel() || (!ignoresMultipliers && numZeroMultipliers > 0)) {
			return;
		}

		PlayerCharacterExperienceChangeEvent event =
				new PlayerCharacterExperienceChangeEvent(playerCharacter, add, ignoresMultipliers ? 1.0
						: multiplier);
		Bukkit.getPluginManager().callEvent(event);

		xpDifference += event.getChange();
		checkLevelUp();
	}

	@Override
	public ExperienceMultiplier addMultiplier(double amount) {
		return new CharacterExperienceMultiplier(amount);
	}

	@Override
	public ListenableFuture<Experience> refresh() {
		final SettableFuture<Experience> ret = SettableFuture.create();

		final ListenableFuture<Integer> levelRequest =
				PlayerCharacterData.getExperienceLevel(playerCharacter.getUniqueCharacterId());
		final ListenableFuture<Long> xpRequest =
				PlayerCharacterData.getExperienceTowardsNextLevel(playerCharacter.getUniqueCharacterId());

		Runnable listener = new Runnable() {
			private boolean jobDone = false;

			@Override
			public synchronized void run() {
				if (jobDone || !(levelRequest.isDone() && xpRequest.isDone())) {
					return;
				}
				jobDone = true;

				// syncs to main thread, delivers refreshed values, and calls back
				Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> {
                    try {
                        levelFromDb = levelRequest.get();
                    } catch (Exception e) {
                        MessageUtil.sendException(LegendsOfValeros.getInstance(), playerCharacter.getPlayer(), e, false);
                    }
                    try {
                        xpFromDb = xpRequest.get();
                    } catch (Exception e) {
                        MessageUtil.sendException(LegendsOfValeros.getInstance(), playerCharacter.getPlayer(), e, false);
                    }

                    refreshXpToLevel();
                    checkLevelUp();

                    ret.set(CharacterExperience.this);
                });
			}
		};

		levelRequest.addListener(listener, Utilities.asyncExecutor());
		xpRequest.addListener(listener, Utilities.asyncExecutor());

		return ret;
	}

	@Override
	public void setLevel(int setTo) {
		if (setTo < 0) {
			throw new IllegalArgumentException("level cannot be negative");
		}
		if (setTo > config.getMaxLevel())
			throw new IllegalArgumentException("level cannot be larger than max level");
		levelDifference = setTo - levelFromDb;
		refreshXpToLevel();
	}

	@Override
	public void setExperienceTowardsNextLevel(long setTo) {
		if (setTo < 0) {
			throw new IllegalArgumentException("xp cannot be negative");
		}
		xpDifference = setTo - xpFromDb;
		checkLevelUp();
	}

	/**
	 * Gets whether this object contains any differences from what is known of the corresponding
	 * database record.
	 * 
	 * @return <code>true</code> if this has changes that should be written to the database, else
	 *         <code>false</code>.
	 */
	boolean hasChanged() {
		return xpDifference != 0 || levelDifference != 0;
	}

	/**
	 * Gets the difference between the in memory version of listener towards the next level and what
	 * is known of the database record's version.
	 * 
	 * @return The amount that the database's listener-to-next-level datum should be edited by when
	 *         they log out and this object becomes disused.
	 */
	long getExperienceDifference() {
		return xpDifference;
	}

	/**
	 * Gets the difference between the in-memory version of the listener level and what is known of
	 * the database record's version.
	 * 
	 * @return The amount that the database's level for the player character should be edited by when
	 *         they log out and this object becomes disused.
	 */
	int getLevelDifference() {
		return levelDifference;
	}

	void setParent(PlayerCharacter parent) {
		this.playerCharacter = parent;
	}

	private void checkLevelUp() {
		while (xpToLevel <= getExperienceTowardsNextLevel() && getLevel() < config.getMaxLevel()) {

			levelDifference++;

			// may cause xpDifference to be negative in order to potentially update the xp amount in the
			// db to be less than it currently is (because the level is now higher)
			xpDifference -= xpToLevel;

			refreshXpToLevel();

			PlayerCharacterLevelUpEvent event = new PlayerCharacterLevelUpEvent(playerCharacter, getLevel());
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	private void refreshXpToLevel() {
		xpToLevel = config.getExperienceBetweenLevels(getLevel(), getLevel() + 1);
	}

	/**
	 * A multiplier for incoming listener.
	 */
	private class CharacterExperienceMultiplier implements Experience.ExperienceMultiplier {

		private boolean active;
		private double amount;

		private CharacterExperienceMultiplier(double amount) {
			this.amount = amount;
			this.active = true;

			if (amount == 0.0) {
				numZeroMultipliers++;
			} else {
				multiplier *= amount;
			}
		}

		@Override
		public void remove() {
			if (!active) {
				return;
			}
			active = false;

			if (amount == 0.0) {
				numZeroMultipliers--;

			} else {
				multiplier /= amount;
			}
		}

	}

}
