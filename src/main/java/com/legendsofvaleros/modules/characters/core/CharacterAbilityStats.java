package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.classes.skills.AbilityStats;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.config.ClassConfig;
import com.legendsofvaleros.modules.classes.stats.AbilityStat;
import com.legendsofvaleros.modules.classes.stats.AbilityStatApplicator;
import com.legendsofvaleros.modules.classes.stats.AbilityStatValue;
import com.legendsofvaleros.modules.characters.ui.AbilityStatChangeListener;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ConstructionListener;
import com.legendsofvaleros.modules.combatengine.modifiers.DecayingValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks and updates the effects of a player character's class stats.
 * <p>
 * Valid only for a single instance of a <code>CombatEntity</code>.
 * <p>
 * Can be constructed before the relevant combat entity has been initialized, as long as this class
 * is later informed when the combat entity is initialized.
 */
public class CharacterAbilityStats implements AbilityStats {

  private final PlayerCharacter playerCharacter;
  private final AbilityStatChangeListener ui;

  private final AbilityStatListener listener;

  private final ClassConfig configClass;
  private final Map<AbilityStat, AbilityStatValue> abilityStats;
  private AbilityStatApplicator applicatorClass;

  private final Set<ValueModifier> removeOnDeath;
  private final Set<ValueModifier> haveTasks;

  /**
   * Class constructor.
   *
   * @param playerCharacter The player character these class stats are for.
   * @param combatEntity The combat entity that represents the player character and will be the
   * target of the effects of these class stats. Can be <code>null</code> if this class is
   * later informed when it is initialized with {@link #onCombatEntityCreate(CombatEntity)}.
   * @throws IllegalArgumentException On a <code>null</code> player character.
   */
  public CharacterAbilityStats(PlayerCharacter playerCharacter, CombatEntity combatEntity) throws IllegalArgumentException {
    if (playerCharacter == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    this.playerCharacter = playerCharacter;
    this.ui = Characters.getInstance().getUiManager().getAbilityStatInterface(playerCharacter);

    this.listener = new AbilityStatListener();

    this.configClass = Characters.getInstance().getCharacterConfig().getClassConfig(playerCharacter.getPlayerClass());
    if (combatEntity != null) {
      this.applicatorClass = configClass.getNewApplicator(combatEntity);
    }

    this.abilityStats = new EnumMap<>(AbilityStat.class);

    //getAbilityStatValue(AbilityStat.STRENGTH).flatEdit(1, true);

    this.removeOnDeath = new HashSet<>();
    this.haveTasks = new HashSet<>();

    // applies baseline class stats
    Archetype arch = configClass.getArchetype();

    for (AbilityStat abilityStat : AbilityStat.values()) {
      double baseLevel =
          arch.getStatValue(abilityStat.name(), playerCharacter.getExperience().getLevel());
      if (baseLevel != 0.0) {
        newAbilityStatModifierBuilder(abilityStat).setValue(baseLevel)
            .setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT).build();
      }
    }
  }

  @Override
  public PlayerCharacter getPlayerCharacter() {
    return playerCharacter;
  }

  @Override
  public double getAbilityStat(AbilityStat abilityStat) {
    AbilityStatValue val = abilityStats.get(abilityStat);
    if (val == null) {
      return 0;
    }
    return val.getFinalValue();
  }

  @Override
  public ValueModifierBuilder newAbilityStatModifierBuilder(AbilityStat modify) throws IllegalArgumentException {
    if (modify == null) {
      throw new IllegalArgumentException("class stat cannot be null");
    }
    AbilityStatValue value = getAbilityStatValue(modify);
    return new ValueModifierBuilder(value, listener);
  }

  /**
   * If no combat entity object was previously provided, informs this class when there is a combat
   * entity which can actually implement the effects of these class stats.
   *
   * @param useThis The combat entity object to affect with the effects of these class stat values.
   */
  void onCombatEntityCreate(CombatEntity useThis) {
    this.applicatorClass = configClass.getNewApplicator(useThis);
    // on creating a new applicator, informs it of any applied changes before now
    for (Map.Entry<AbilityStat, AbilityStatValue> ent : abilityStats.entrySet()) {
      applicatorClass.onAbilityStatChange(ent.getKey(), ent.getValue().getFinalValue(), 0);
    }
  }

  void onInvalidated() {
    // cancels any tasks that are ongoing.
    for (ValueModifier modifier : haveTasks) {
      if (modifier != null) {
        modifier.remove();
      }
    }
    haveTasks.clear();
  }

  void onDeath() {
    for (ValueModifier modifier : removeOnDeath) {
      modifier.remove();
    }
    removeOnDeath.clear();
  }

  private AbilityStatValue getAbilityStatValue(AbilityStat abilityStat) {
    AbilityStatValue value = abilityStats.get(abilityStat);
    if (value == null) {
      value = new AbilityStatValue(abilityStat, listener);
      abilityStats.put(abilityStat, value);
    }
    return value;
  }

  /**
   * Listens to changes in the player character's class stats'.
   */
  private class AbilityStatListener implements ConstructionListener, AbilityStatChangeListener {

    @Override
    public void onConstruction(ValueModifier newModifier) {
      if (newModifier.isRemovedOnDeath()) {
        removeOnDeath.add(newModifier);
      }
      if (newModifier.isRemovedOnDeath() || newModifier.getExpiration() < Long.MAX_VALUE
          || newModifier instanceof DecayingValueModifier) {
        haveTasks.add(newModifier);
      }
    }

    @Override
    public void onAbilityStatChange(AbilityStat changed, double newValue, double previousValue) {
      if (applicatorClass != null) {
        applicatorClass.onAbilityStatChange(changed, newValue, previousValue);
      }

      if (ui != null) {
        ui.onAbilityStatChange(changed, newValue, previousValue);
      }
    }

  }
}
