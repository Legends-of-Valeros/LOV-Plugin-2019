package com.legendsofvaleros.modules.characters.skilleffect;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.ui.SkillEffectListener;
import com.legendsofvaleros.modules.npcs.NPCsController;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * An instance of an entity being affected by a skill or spell.
 * <p>
 * Allows a client to define an arbitrary generic meta data object to be tracked with this instance.
 */
public class MetaEffectInstance<T> implements SkillEffectInstance {

	private final SkillEffect<T> effect;
	private UUID affectedId;
	private WeakReference<LivingEntity> affected;
	private final UUID appliedById;
	private final WeakReference<LivingEntity> appliedBy;
	private final CharacterId appliedByCharacterId;

	public int level;
	public long duration;
	private long started;

	private SkillEffectListener ui;

	private BukkitRunnable expirationTask;

	private T meta;

	public MetaEffectInstance(SkillEffect<T> effect) { this(effect, null, null, 1, 100, System.currentTimeMillis()); }
	MetaEffectInstance(SkillEffect<T> effect, LivingEntity affected, LivingEntity appliedBy,
			int level, long durationMillis, long started) {
		this.effect = effect;
		if(affected != null) {
			this.affectedId = affected.getUniqueId();
			this.affected = new WeakReference<>(affected);

			if (affected.getType() == EntityType.PLAYER && !NPCsController.isNPC(affected)) {
				PlayerCharacter pc = Characters.getPlayerCharacter((Player) affected);
				ui = Characters.getInstance().getUiManager().getCharacterEffectInterface(pc);
			}
		}

		if (appliedBy != null) {
			this.appliedById = appliedBy.getUniqueId();
			PlayerCharacter pc;
			if (appliedBy.getType() == EntityType.PLAYER && !NPCsController.isNPC(affected)
					&& (pc = Characters.getPlayerCharacter((Player) appliedBy)) != null) {
				appliedByCharacterId = pc.getUniqueCharacterId();
			} else {
				appliedByCharacterId = null;
			}
		} else {
			this.appliedById = null;
			this.appliedByCharacterId = null;
		}
		this.appliedBy = new WeakReference<>(appliedBy);

		this.level = level;
		this.duration = durationMillis;
		this.started = started;

		long durationTicks = getRemainingDurationMillis() / 50;
		if (durationTicks < 0) {
			throw new IllegalArgumentException("remaining duration cannot be negative");
		}
	}

	@Override
	public SkillEffect<T> getEffect() {
		return effect;
	}

	@Override
	public UUID getAffectedId() {
		return affectedId;
	}

	@Override
	public LivingEntity getAffected() {
		return affected.get();
	}

	@Override
	public UUID getAppliedById() {
		return appliedById;
	}

	@Override
	public CharacterId getAppliedByCharacterId() {
		return appliedByCharacterId;
	}

	@Override
	public LivingEntity getAppliedBy() {
		if (appliedBy != null) {
			return appliedBy.get();
		}
		return null;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public long getRemainingDurationMillis() {
		return duration - getElapsedDurationMillis();
	}

	@Override
	public long getElapsedDurationMillis() {
		return System.currentTimeMillis() - started;
	}

	/**
	 * Gets any meta data stored with this effect instance.
	 * 
	 * @return Metadata stored with this effect instance, if any.
	 */
	public T getMeta() {
		return meta;
	}

	/**
	 * Sets the metadata stored with this effect instance.
	 * 
	 * @param meta The metadata to store with this effect instance. Can be <code>null</code> to clear
	 *        any previous metadata.
	 */
	public void setMeta(T meta) {
		this.meta = meta;
	}

	/**
	 * Schedules a task to automatically remove this effect at the end of its lifetime.
	 * <p>
	 * Does nothing if the task is already scheduled.
	 * 
	 * @param informUi <code>true</code> if any present user interface should be informed of this
	 *        addition, else <code>false</code>.
	 */
	void scheduleTask(boolean informUi) {
		if (expirationTask == null) {
			expirationTask = new BukkitRunnable() {
				@Override
				public void run() {
					effect.remove(affectedId, affected.get(), RemovalReason.EXPIRED);
				}
			};
			expirationTask.runTaskLater(LegendsOfValeros.getInstance(), getRemainingDurationMillis() / 50);

			if (ui != null && informUi) {
				ui.onSkillEffectUpdate(effect, System.currentTimeMillis() + getRemainingDurationMillis(),
						level);
			}
		}
	}

	/**
	 * Unschedules this effect's expiration. Does nothing if it has already been unscheduled. Cannot
	 * be reversed.
	 * 
	 * @param reason The reason this is being stopped.
	 */
	void stop(RemovalReason reason) {
		if (expirationTask != null) {
			expirationTask.cancel();

			if ((reason == RemovalReason.EXPIRED || reason == RemovalReason.INTERRUPTED || reason == RemovalReason.OTHER)
					&& ui != null) {
				ui.onSkillEffectRemoved(effect, level, reason);
			}
		}
	}

}
