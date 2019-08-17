package com.legendsofvaleros.modules.mobs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.loot.LootTable;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.ai.AIStuckAction;
import com.legendsofvaleros.modules.mobs.behavior.StaticAI;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.Skin;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Mob {
    private String id;
    private String group;
    private String name;

    private EntityType type;
    public String skin;

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

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return type;
    }

    public EntityRarity getRarity() {
        return rarity;
    }

    public String getArchetype() {
        return archetype;
    }

    public EntityClass getEntityClass() {
        return entityClass;
    }

    public int getExperience() {
        return experience;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public StatsMap getStats() {
        if (stats == null)
            stats = new StatsMap();
        return stats;
    }

    public Integer getStat(Enum e) {
        return getStats().get(e);
    }

    public EquipmentMap getEquipment() {
        if (equipment == null)
            equipment = new EquipmentMap();
        return equipment;
    }

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
            Mob mob = ((Instance) obj).mob;
            return mob != null && (this == mob || this.id.equals(mob.id));
        }

        return false;
    }

    public static class StatsMap extends HashMap<Object, Integer> { }
    public static class EquipmentMap extends HashMap<Equipment.EquipmentSlot, Item[]> { }

    public static class Loot {
        public int tries;
        public double chance;
        public Table[] tables;

        public static class Table {
            public LootTable table;
            public double weight;
            public int amount;
        }

        public Instance newInstance() {
            return new Instance(this);
        }

        /**
         * An instance is created whenever a query for this drop occurs. i.e. on death.
         */
        public static class Instance {
            final Loot loot;

            double totalWeight = 0D;

            /**
             * Keeps track of how much each table has dropped.
             */
            byte[] dropped;

            /**
             * A dead table is one that is not allowed to drop anymore.
             */
            byte dead = 0;

            public Instance(Loot loot) {
                this.loot = loot;

                for (Mob.Loot.Table table : loot.tables) {
                    this.totalWeight += table.weight;
                }

                this.dropped = new byte[loot.tables.length];
            }

            public Optional<LootTable> nextTable() {
                // If chance fails, return.
                if(Math.random() > loot.chance) return Optional.empty();

                // If all tables are dead, return.
                if(this.dead == this.loot.tables.length) return Optional.empty();

                double random = Math.random() * this.totalWeight;

                for (int i = 0; i < this.loot.tables.length; i++) {
                    // If more has dropped from this than allowed, skip it.
                    if(this.dropped[i] >= this.loot.tables[i].amount) continue;

                    random -= this.loot.tables[i].weight;

                    if (random <= 0D) {
                        this.dropped[i]++;

                        if(this.dropped[i] >= this.loot.tables[i].amount) {
                            // Subtract the weight or we get incorrect chances next time 'round.
                            this.totalWeight -= this.loot.tables[i].weight;
                            this.dead++;
                        }

                        return Optional.of(this.loot.tables[i].table);
                    }
                }

                return Optional.empty();
            }
        }
    }

    public static class Distance {
        public Integer detection = 5;
        public Integer chase = 10;
    }

    public static class Item {
        private String id;

        private int amount = 1;
        private double chance = 1;

        public ItemStack getStack() {
            ItemStack itemStack = Gear.fromId(id).newInstance().toStack();
            if (itemStack.getType() != Material.AIR) {
                itemStack.setAmount(this.amount);
                return itemStack;
            } else
                MobsController.getInstance().getLogger().severe("Item does not exist. Offender: " + id);
            return null;
        }

        @Override
        public String toString() {
            return id + " x" + amount + " - " + chance * 100D + "%";
        }
    }

    public static class Instance {
        private static final Map<UUID, Instance> INSTANCES = new HashMap<>();
        public Boolean active = null;
        public final Mob mob;
        public final SpawnArea home;
        public final int level;
        public CombatEntity ce;
        public NPC npc;

        public Instance(Mob mob, SpawnArea home, int level) {
            this.mob = mob;
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

            npc = NPCsController.getInstance().createNPC(mob.type, "");
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

            if (mob.type == EntityType.PLAYER) {
                if (mob.skin != null) {
                    try {
                        Skin skin = NPCsController.getInstance().getSkin(mob.skin);
                        if (skin == null)
                            throw new Exception("No skin with that ID. Offender: " + mob.skin + " on " + mob.id);

                        npc.data().setPersistent("cached-skin-uuid", skin.uuid);
                        npc.data().setPersistent("cached-skin-uuid-name", skin.username.toLowerCase());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, skin.username.toLowerCase());
                        npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, skin.signature);
                        npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, skin.data);
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
            Item[] items = mob.getEquipment().get(slot);
            if (items == null || items.length == 0)
                return null;

            // Compute the total weight of all items together
            double totalWeight = 0.0d;
            for (Item i : items)
                totalWeight += i.chance;

            // Now choose a random item
            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < items.length; ++i) {
                random -= items[i].chance;
                if (random <= 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            return items[randomIndex];
        }
    }
}