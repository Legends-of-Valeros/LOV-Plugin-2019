package com.legendsofvaleros.modules.mobs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.ai.AIStuckAction;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.mobs.behavior.StaticAI;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class Mob implements IEntity {
    @SerializedName("_id")
    private String id;
    private String slug;

    private String name;
    private EntityType type;
    private ISkin skin;

    private EntityRarity rarity;
    private String archetype;
    @SerializedName("class")
    private EntityClass entityClass;

    private Mob.EquipmentMap equipment;
    private Mob.StatsMap stats;
    private boolean invincible = false;

    private int experience = 0;
    public Loot[] loot;

    public String riding;
    public Boolean ghost = false;
    public Distance distance = new Distance();

    public transient Map<CharacterId, Long> leashed = new HashMap<>();

    private transient Set<SpawnArea> spawns;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public ISkin getSkin() {
        return skin;
    }

    @Override
    public EntityRarity getRarity() {
        return rarity;
    }

    @Override
    public String getArchetype() {
        return archetype;
    }

    @Override
    public EntityClass getEntityClass() {
        return entityClass;
    }

    @Override
    public int getExperience() {
        return experience;
    }

    @Override
    public Loot[] getLoot() {
        return loot;
    }

    @Override
    public boolean isInvincible() {
        return invincible;
    }

    @Override
    public boolean isGhost() {
        return ghost;
    }

    @Override
    public StatsMap getStats() {
        if (stats == null)
            stats = new StatsMap();
        return stats;
    }

    @Override
    public Integer getStat(Enum e) {
        return getStats().get(e);
    }

    @Override
    public EquipmentMap getEquipment() {
        if (equipment == null)
            equipment = new EquipmentMap();
        return equipment;
    }

    @Override
    public Distance getDistance() { return distance; }

    @Override
    public Map<CharacterId, Long> getLeashed() { return leashed; }

    @Override
    public Set<SpawnArea> getSpawns() {
        if (spawns == null)
            spawns = new HashSet<>();
        return spawns;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mob) {
            Mob mob = (Mob) obj;
            return this == mob || this.id.equals(mob.id);
        } else if (obj instanceof Instance) {
            IEntity entity = ((Instance)obj).entity;
            return entity != null && (this == entity || this.id.equals(entity.getId()));
        }

        return false;
    }

    public static class Instance {
        private static final Map<UUID, Instance> INSTANCES = new HashMap<>();
        public Boolean active = null;
        public final IEntity entity;
        public final SpawnArea home;
        public final int level;
        public CombatEntity ce;
        public NPC npc;

        public Instance(IEntity entity, SpawnArea home, int level) {
            this.entity = entity;
            this.home = home;
            this.level = level;
        }

        public static Instance get(UUID uuid) {
            return INSTANCES.get(uuid);
        }

        public static Instance get(Entity entity) {
            return get(entity.getUniqueId());
        }

        public void spawn(Location loc) {
            if (active == Boolean.FALSE)
                throw new IllegalStateException("Mob instance already destroyed.");
            active = true;

            npc = NPCsController.getInstance().createNPC(entity.getType(), "");
            npc.getNavigator().getLocalParameters().updatePathRate(20)
                    .useNewPathfinder(true)
                    .stuckAction(AIStuckAction.INSTANCE)
                    .avoidWater(false);

            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
            npc.data().setPersistent(NPC.DAMAGE_OTHERS_METADATA, true);
            npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            npc.data().setPersistent(NPC.LEASH_PROTECTED_METADATA, true);
            npc.data().setPersistent(NPC.DROPS_ITEMS_METADATA, false);
            npc.data().setPersistent(NPC.TARGETABLE_METADATA, false);
            npc.data().setPersistent(NPC.COLLIDABLE_METADATA, true);

            if (entity.getType() == EntityType.PLAYER) {
                if (entity.getSkin() != null) {
                    try {
                        ISkin skin = entity.getSkin();

                        ISkin.Texture texture = skin.getTexture();

                        npc.data().setPersistent("cached-skin-uuid", texture.getUUID());
                        npc.data().setPersistent("cached-skin-uuid-name", texture.getUsername().toLowerCase());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, texture.getUsername().toLowerCase());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, texture.getSignature());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, texture.getData());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_USE_LATEST, false);
                    } catch (Exception e) {
                        MessageUtil.sendException(MobsController.getInstance(), e);
                    }
                }
            }

            Equipment equipment = npc.getTrait(Equipment.class);
            for (Equipment.EquipmentSlot slot : Equipment.EquipmentSlot.values()) {
                Item item = getRandomEquipment(slot);

                if (item != null)
                    equipment.set(slot, item.getStack());
            }

            npc.addTrait(new MobTrait(this));
            npc.spawn(loc);
            INSTANCES.put(npc.getEntity().getUniqueId(), this);
            ce = CombatEngine.getEntity((LivingEntity) npc.getEntity());

            if (ce != null) {
                MobsController.ai().bind(ce, StaticAI.AGGRESSIVE);
            } else {
                System.out.println("isNpc : " + NPCsController.getInstance().isStaticNPC((LivingEntity) npc.getEntity()));
            }

            home.getEntities().add(this);
        }

        public void destroy() {
            active = false;
            npc.destroy();

            home.getEntities().remove(this);

            INSTANCES.remove(npc.getUniqueId());
        }

        private Item getRandomEquipment(Equipment.EquipmentSlot slot) {
            Item[] items = entity.getEquipment().get(slot);
            if (items == null || items.length == 0)
                return null;

            // Compute the total weight of all items together
            double totalWeight = 0.0d;
            for (Item i : items)
                totalWeight += i.getChance();

            // Now choose a random item
            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < items.length; ++i) {
                random -= items[i].getChance();
                if (random <= 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            return items[randomIndex];
        }
    }
}