package com.legendsofvaleros.modules.levelarchetypes.examples;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityPreCreateEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.modules.levelarchetypes.api.LevelProvider;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArchetypeUsageExample implements Listener, LevelProvider {

    private static final String ARCHETYPE_ID = "normal-mob"; // as it appears in the config

    public ArchetypeUsageExample() {
        // registers this class as the class that will tell the level of skeletons to clients who ask.
        LevelArchetypes.getInstance().registerLevelProvider(this, EntityType.SKELETON);
    }

    @EventHandler
    public void onCombatEntityPreCreate(CombatEntityPreCreateEvent event) {
        if (event.getLivingEntity().getType() == EntityType.SKELETON) {

            // starts from the "normal-mob" archetype base stats for whatever level this skeleton is
            Archetype archetype = LevelArchetypes.getInstance().getArchetype(ARCHETYPE_ID);
            int level = LevelArchetypes.getInstance().getLevel(event.getLivingEntity());

            CombatProfile baseStats = archetype.getCombatProfile(level);

            event.setCombatProfile(baseStats);
        }
    }

    @EventHandler
    public void onCombatEntityCreate(CombatEntityCreateEvent event) {
        if (event.getLivingEntity().getType() == EntityType.SKELETON) {
            CombatEntity ce = event.getCombatEntity();

            // increases its speed
            ce.getStats().newStatModifierBuilder(Stat.SPEED).setModifierType(ModifierType.MULTIPLIER)
                    .setValue(1.5);

            // increases its attack damage
            ce.getStats().newStatModifierBuilder(Stat.PHYSICAL_ATTACK)
                    .setModifierType(ModifierType.MULTIPLIER).setValue(1.25);

            // decreases its defense
            ce.getStats().newStatModifierBuilder(Stat.ARMOR).setModifierType(ModifierType.MULTIPLIER)
                    .setValue(0.75);
        }
    }

    @Override
    public int getLevel(LivingEntity entity) {
        // A client is asking for a skeleton's level
        return 0;
        // In this example level is constant, but normally there should be some logic to how levels are
        // defined
    }

}
