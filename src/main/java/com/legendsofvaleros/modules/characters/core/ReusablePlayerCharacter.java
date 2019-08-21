package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.api.AbilityStats;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.config.RaceConfig;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.skill.PlayerSkillSet;
import com.legendsofvaleros.modules.characters.skill.SkillSet;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.cooldowns.CooldownsController;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * An implementation of a player character that can be logged into/out of multiple times within a
 * single player's session on the server.
 */
public class ReusablePlayerCharacter implements PlayerCharacter {

    private final CharacterId id;
    private final WeakReference<Player> player;

    private final EntityRace playerRace;
    private final RaceConfig configRace;

    private final EntityClass playerClass;

    private Location savedLoc;

    private CharacterExperience experience;
    private CharacterAbilityStats abilityStats;

    private InventoryData inventoryData;
    private SkillSet skillSet;

    private volatile boolean current;

    ReusablePlayerCharacter(Player player, int characterNumber, EntityRace playerRace,
                            EntityClass playerClass, Location startingLocation, CharacterExperience experience, InventoryData inventoryData,
                            List<String> skillSet) {
        if (player == null || playerRace == null || playerClass == null || startingLocation == null
                || experience == null) {
            throw new IllegalArgumentException("params cannot be null!");
        }

        this.id = new CharacterId(player.getUniqueId(), characterNumber);
        this.player = new WeakReference<>(player);

        this.playerRace = playerRace;
        this.configRace = Characters.getInstance().getCharacterConfig().getRaceConfig(playerRace);
        this.playerClass = playerClass;

        this.savedLoc = startingLocation;

        this.experience = experience;
        experience.setParent(this);

        this.inventoryData = inventoryData;

        this.skillSet = new PlayerSkillSet(this, skillSet);
    }

    @Override
    public boolean isNPC() {
        return false;
    }

    @Override
    public CharacterId getUniqueCharacterId() {
        return id;
    }

    @Override
    public int getCharacterNumber() {
        return id.getCharacterNumber();
    }

    @Override
    public UUID getPlayerId() {
        return id.getPlayerId();
    }

    @Override
    public Player getPlayer() {
        return player.get();
    }

    @Override
    public boolean isCurrent() {
        return current;
    }

    @Override
    public Location getLocation() {
        Player p;
        if (current && (p = player.get()) != null) {
            return p.getLocation();
        } else {
            return savedLoc.clone();
        }
    }

    @Override
    public Zone getCurrentZone() {
        return ZonesController.getInstance().getZone(getPlayer()).getZone();
    }

    @Override
    public EntityRace getPlayerRace() {
        return playerRace;
    }

    @Override
    public EntityClass getPlayerClass() {
        return playerClass;
    }

    @Override
    public CharacterExperience getExperience() {
        return experience;
    }

    @Override
    public AbilityStats getAbilityStats() {
        return abilityStats;
    }

    @Override
    public InventoryData getInventoryData() {
        return inventoryData;
    }

    @Override
    public SkillSet getSkillSet() {
        return skillSet;
    }

    @Override
    public Cooldowns getCooldowns() {
        return CooldownsController.getInstance().getCooldowns(id);
    }

    /**
     * Gets whether this object contains any differences from when it was constructed.
     * @return <code>true</code> if this has changes that should be written to the database, else
     * <code>false</code>.
     */
	/*boolean hasChanged() {
		if (experience.hasChanged()) {
			return true;
		} else if (!locFromDb.equals(getLocation())) {
			return true;
		}
		return false;
	}*/

    void setCurrent(boolean current) {
        if (this.current != current) {
            this.current = current;

            Player p = player.get();
            if (p == null) {
                return;
            }

            if (current) { // on set to current
                p.teleport(savedLoc);

                // currently this happens before this character's CombatEntity is initialized, so we have
                // nothing to pass in here. The class stats class will be given the CombatEntity when it is
                // initialized.
                this.abilityStats = new CharacterAbilityStats(this, null);

            } else { // on set to no longer current
                if (abilityStats != null) {
                    this.abilityStats.onInvalidated();
                }
                if (inventoryData != null) {
                    inventoryData.onInvalidated(this);
                }

                savedLoc = p.getLocation();
            }
        }
    }

    void onCombatEntityCreate(CombatEntity combatEntity) {
        if (abilityStats != null) {
            abilityStats.onCombatEntityCreate(combatEntity);
        }

        for (StatModifierModel mod : configRace.getModifiers()) {
            combatEntity.getStats().newStatModifierBuilder(mod.getStat())
                    .setModifierType(mod.getModifierType())
                    .setValue(mod.getValue())
                    .build();
        }
    }

    void onDeath(PlayerDeathEvent event) {
        if (abilityStats != null) {
            abilityStats.onDeath();
        }
        if (inventoryData != null) {
            inventoryData.onDeath(this);
        }
    }
}
