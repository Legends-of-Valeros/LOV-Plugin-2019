package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Disguises an entity as an animal, prevents them from using skills or interacting with the world,
 * and gives them a speed penalty.
 * <p>
 * Relies on LibsDisguises to disguise entities.
 */
public class Polymorph {

    private static final double SPEED_MULTIPLIER = 0.5;
    private static final double HIT_MULTIPLIER = 0;
    private static final EntityType DISGUISE_TYPE = EntityType.SHEEP;

    private static final Multimap<UUID, ValueModifier> MODS = HashMultimap.create();

    // Uses a generic type to avoid ClassNotFoundExceptions if LibsDisguises is not enabled
    private static Map<UUID, Object> DISGUISES = new HashMap<>();
    private static boolean libsFound;

    static {
        new NoInteractionListener(MODS.keySet());
        libsFound = LegendsOfValeros.getInstance().getServer().getPluginManager().isPluginEnabled("LibsDisguises");
    }

    public static void apply(CombatEntity entity) {
        remove(entity);

        LivingEntity le = entity.getLivingEntity();
        if (le != null) {
            MODS.put(entity.getUniqueId(), entity.getStats().newStatModifierBuilder(Stat.SPEED)
                    .setModifierType(ModifierType.MULTIPLIER).setValue(SPEED_MULTIPLIER).build());
            MODS.put(entity.getUniqueId(), entity.getStats().newStatModifierBuilder(Stat.HIT_CHANCE)
                    .setModifierType(ModifierType.MULTIPLIER).setValue(HIT_MULTIPLIER).build());

            if (libsFound) {
                Disguise disguise = new MobDisguise(DisguiseType.getType(DISGUISE_TYPE));

                String name =
                        (le.getType() == EntityType.PLAYER ? le.getName() : le.getCustomName());

                if (name != null && disguise.getWatcher() != null) {
                    disguise.getWatcher().setCustomName(name);
                    disguise.getWatcher().setCustomNameVisible(true);
                }

                if (le.getType() == EntityType.PLAYER) {
                    disguise.setViewSelfDisguise(true);
                }

                DISGUISES.put(entity.getUniqueId(), disguise);
                DisguiseAPI.disguiseEntity(le, disguise);
            }
        }
    }

    public static void remove(CombatEntity entity) {
        Collection<ValueModifier> mods = MODS.removeAll(entity.getUniqueId());

        if (mods != null) {
            for (ValueModifier mod : mods) {
                if (mod != null) {
                    mod.remove();
                }

            }
            if (!mods.isEmpty() && libsFound) {
                Object disguise = DISGUISES.remove(entity.getUniqueId());
                if (disguise != null) {
                    ((Disguise) disguise).removeDisguise();
                }
            }
        }
    }

}
