package com.legendsofvaleros.modules.mobs.trait;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.AbilityStats;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.Experience;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.InventoryData;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skill.SkillSet;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class NPCPlayerCharacter implements PlayerCharacter {
    static final Experience experience = new Experience() {
        @Override public int getLevel() {
            return 0;
        }

        @Override public long getExperienceForNextLevel() {
            return 0;
        }

        @Override public long getExperienceTowardsNextLevel() {
            return 0;
        }

        @Override public double getPercentageTowardsNextLevel() {
            return 0;
        }

        @Override public void addExperience(long add, boolean ignoresMultipliers) {
        }

        @Override public ExperienceMultiplier addMultiplier(double amount) {
            return null;
        }

        @Override public void setLevel(int setTo) {
        }

        @Override public void setExperienceTowardsNextLevel(long setTo) {
        }
    };
    static final Cooldowns cooldowns = new Cooldowns() {
        @Override public PlayerCharacter getPlayerCharacter() {
            return null;
        }

        @Override public boolean hasCooldown(String key) throws IllegalStateException {
            return false;
        }

        @Override public Cooldown getCooldown(String key) throws IllegalStateException {
            return null;
        }

        @Override public Cooldown offerCooldown(String key, CooldownType type, long durationMillis) throws IllegalStateException {
            return null;
        }

        @Override public Cooldown overwriteCooldown(String key, CooldownType type, long durationMillis) throws IllegalStateException {
            return null;
        }
    };
    static final AbilityStats abilityStats = new AbilityStats() {
        @Override public PlayerCharacter getPlayerCharacter() {
            return null;
        }

        @Override public double getAbilityStat(AbilityStat abilityStat) {
            return 0;
        }

        @Override public ValueModifierBuilder newAbilityStatModifierBuilder(AbilityStat modify) throws IllegalArgumentException {
            return null;
        }
    };
    static final InventoryData inventory = new InventoryData() {
        @Override
        public Promise<Boolean> onInvalidated(PlayerCharacter pc) {
            return Promise.make(false);
        }

        @Override
        public Promise<Boolean> saveInventory(PlayerCharacter pc) {
            return Promise.make((Boolean)null);
        }

        @Override
        public Promise<Boolean> loadInventory(PlayerCharacter pc) {
            return Promise.make(false);
        }

        @Override public void initInventory(PlayerCharacter pc) {  }
        @Override public void onDeath(PlayerCharacter pc) { }
        @Override public String getData() { return null; }
    };
    static final SkillSet skillSet = new SkillSet() {
        @Override public void add(String skillId) { }
        @Override public List<String> getAll() {
            return null;
        }
        @Override public int getLevel(String skillId) {
            return 0;
        }
        @Override public List<Entry<Skill, Integer>> getSkills() {
            return null;
        }
        @Override public boolean has(String skillId) { return false; }
        @Override public Entry<Skill, Integer> get(String skillId) {
            return null;
        }
        @Override public Entry<Skill, Integer> remove(String skillId) {
            return null;
        }
    };

    NPC npc;
    EntityRace race;
    EntityClass clazz;

    public NPCPlayerCharacter(NPC npc, EntityRace race, EntityClass clazz) {
        this.npc = npc;
        this.race = race;
        this.clazz = clazz;
    }

    @Override
    public boolean isNPC() {
        return true;
    }

    @Override
    public CharacterId getUniqueCharacterId() {
        return new CharacterId(npc.getUniqueId(), 0);
    }

    @Override
    public int getCharacterNumber() {
        return 0;
    }

    @Override
    public UUID getPlayerId() {
        return npc.getUniqueId();
    }

    @Override
    public Player getPlayer() {
        return npc.getEntity() instanceof Player ? (Player) npc.getEntity() : null;
    }

    @Override
    public boolean isCurrent() {
        return true;
    }

    @Override
    public Location getLocation() {
        return npc.getEntity().getLocation();
    }

    @Override
    public EntityRace getPlayerRace() {
        return race;
    }

    @Override
    public EntityClass getPlayerClass() {
        return clazz;
    }

    @Override
    public Experience getExperience() {
        return experience;
    }

    @Override
    public Cooldowns getCooldowns() {
        return cooldowns;
    }

    @Override
    public AbilityStats getAbilityStats() {
        return abilityStats;
    }

    @Override
    public InventoryData getInventoryData() {
        return inventory;
    }

    @Override
    public SkillSet getSkillSet() {
        return skillSet;
    }
}