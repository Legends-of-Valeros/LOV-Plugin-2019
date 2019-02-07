package com.legendsofvaleros.modules.characters.entityclass;

import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.ui.AbilityStatChangeListener;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.EditableValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies class stats' effects to a player.
 * <p>
 * Must be updated everytime the class stats of the player change.
 */
public class AbilityStatApplicator implements AbilityStatChangeListener {

  private final Map<AbilityStat, AbilityStatModifierSet> modSets;

  public AbilityStatApplicator(CombatEntity entity, Multimap<AbilityStat, StatModifierModel> mods) {

    modSets = new HashMap<>();

    for (AbilityStat abilityStat : mods.keySet()) {
      AbilityStatModifierSet set = new AbilityStatModifierSet();
      modSets.put(abilityStat, set);

      for (StatModifierModel model : mods.get(abilityStat)) {

        ValueModifierBuilder builder =
            entity.getStats().newStatModifierBuilder(model.getStat())
                .setModifierType(model.getModifierType());

        // starts with modifiers with values that will not do anything.
        if (model.getModifierType() == ValueModifierBuilder.ModifierType.MULTIPLIER) {
          builder.setValue(1.0);
        } else {
          builder.setValue(0.0);
        }

        try {
          set.addModifier((EditableValueModifier) builder.build(), model.getValue());
        } catch (ClassCastException ex) {
			MessageUtil.sendSevereException(Characters.getInstance(), entity.getLivingEntity() instanceof Player ? (Player)entity.getLivingEntity() : null, ex);
          System.out
              .println("Class Stat modifiers rely on EditableValueModifiers! A modifier that could not be cast to that type had to be ignored and was not used!");
        }
      }
    }
  }

  @Override
  public void onAbilityStatChange(AbilityStat changed, double newValue, double previousValue) {
    AbilityStatModifierSet set = modSets.get(changed);
    if (set != null) {
      set.update(newValue);
    }
  }

  /**
   * Tracks a set of modifiers for a class stat. Edits the modifiers based on the current value of
   * the class stat.
   */
  private class AbilityStatModifierSet {
    private final Map<EditableValueModifier, Double> valuesPerPoint;

    private AbilityStatModifierSet() {
      valuesPerPoint = new HashMap<>();
    }

    private void update(double newValue) {
      for (Map.Entry<EditableValueModifier, Double> ent : valuesPerPoint.entrySet()) {

        // if a multiplier, adds it to 1, which is the expected behavior
        double newModifier =
            newValue * ent.getValue() + (ent.getKey().getType() == ValueModifierBuilder.ModifierType.MULTIPLIER ? 1 : 0);

        // sets the value of the modifier to (newAbilityStatPoints * statEditPerPoint)
        ent.getKey().setValue(newModifier);
      }
    }

    private void addModifier(EditableValueModifier modifier, double valuePerPoint) {
      valuesPerPoint.put(modifier, valuePerPoint);
    }
  }

}
