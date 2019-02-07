package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.ai.AIStuckAction;
import com.legendsofvaleros.modules.mobs.behavior.StaticAI;
import com.legendsofvaleros.modules.mobs.trait.MobTrait;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.Skins;
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

@Table(name = "entities")
public class Mob {
    @Column(primary = true, name = "entity_id", length = 64)
    private String id;

    public String getId() {
        return id;
    }

    @Column(name = "entity_group", length = 64)
    private String group;

    public String getGroup() {
        return group;
    }

    @Column(name = "entity_name", length = 64)
    private String name;

    public String getName() {
        return name;
    }

    @Column(name = "entity_type")
    private EntityType type;

    public EntityType getEntityType() {
        return type;
    }

    @Column(name = "entity_rarity")
    private EntityRarity rarity;

    public EntityRarity getRarity() {
        return rarity;
    }

    @Column(name = "entity_archetype", length = 32)
    private String archetype;

    public String getArchetype() {
        return archetype;
    }

    @Column(name = "entity_class")
    private EntityClass entityClass;

    public EntityClass getEntityClass() {
        return entityClass;
    }

    @Column(name = "entity_experience")
    private int experience = 0;

    public int getExperience() {
        return experience;
    }

    @Column(name = "entity_invincible")
    private boolean invincible = false;

    public boolean isInvincible() {
        return invincible;
    }

    @Column(name = "entity_stats")
    private Mob.StatsMap stats;

    public StatsMap getStats() {
        if (stats == null)
            stats = new StatsMap(new StatsMap.StatData[0]);
        return stats;
    }

    public Integer getStat(Enum e) {
        return getStats().get(e);
    }

    @Column(name = "entity_equipment")
    private Mob.EquipmentMap equipment;

    public EquipmentMap getEquipment() {
        if (equipment == null)
            equipment = new EquipmentMap();
        return equipment;
    }

    @Column(name = "entity_options")
    private Mob.Options options;

    public Options getOptions() {
        if (options == null)
            options = new Options();
        return options;
    }

    private Set<SpawnArea> spawns;

    public Set<SpawnArea> getSpawns() {
        if (spawns == null)
            spawns = new HashSet<>();
        return spawns;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mob) {
            Mob mob = (Mob) obj;
            return mob != null && (this == mob || this.id.equals(mob.id));
        } else if (obj instanceof Instance) {
            Mob mob = ((Instance) obj).mob;
            return mob != null && (this == mob || this.id.equals(mob.id));
        }

        return false;
    }

    public static class StatsMap extends HashMap<Object, Integer> {
        public static class StatData {
            public String id;
            public double change;
        }

        private boolean locked;

        private final StatData[] stats;

        public StatData[] getData() {
            return stats;
        }

        public StatsMap(StatData[] stats) {
            this.stats = stats;

            for (StatData stat : stats) {
                Object s = null;

                for (Stat sstat : Stat.values()) {
                    if (sstat.name().equals(stat.id)) {
                        s = sstat;
                        break;
                    }
                }

                if (s == null)
                    for (RegeneratingStat rstat : RegeneratingStat.values()) {
                        if (rstat.name().equals(stat.id)) {
                            s = rstat;
                            break;
                        }
                    }

                if (s == null)
                    for (AbilityStat astat : AbilityStat.values()) {
                        if (astat.name().equals(stat.id)) {
                            s = astat;
                            break;
                        }
                    }

                if (s != null)
                    put(s, (int) stat.change);
            }

            locked = true;
        }

        @Override
        public Integer put(Object key, Integer value) {
            if (locked)
                throw new RuntimeException("Mob stats are immutable.");
            return super.put(key, value);
        }
    }

    public static class EquipmentMap extends HashMap<Equipment.EquipmentSlot, Item[]> {
    }

    public static class Options {
        public LootData[] loot;
        public String riding;
        public String skin;
        public Distance distance = new Distance();
        public Boolean ghost = false;

        public static class LootData {
            public String id;
            public Double chance;
            public int amount;
            public String connect;
        }

        public static class Distance {
            public Integer detection = 5;
            public Integer chase = 10;
        }
    }

    public static class Item {
        private String id;

        private int amount = 1;
        private double chance = 1;

        public ItemStack getStack() {
            ItemStack itemStack = Gear.fromID(id).newInstance().toStack();
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

        public static Instance get(UUID uuid) {
            return INSTANCES.get(uuid);
        }

        public static Instance get(Entity entity) {
            return get(entity.getUniqueId());
        }

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

        public void spawn(Location loc) {
            if (active == Boolean.FALSE)
                throw new IllegalStateException("Mob instance already destroyed.");
            active = true;

            npc = NPCsController.manager().registry.createNPC(mob.type, "");

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
                if (mob.getOptions().skin != null) {
                    ListenableFuture<Skins.Skin> future = Skins.inst().getSkin(mob.getOptions().skin);
                    Skins.Skin skin;
                    try {
                        skin = future.get();

                        if (skin == null)
                            throw new Exception("No skin with that ID. Offender: " + mob.getOptions().skin + " on " + mob.id);

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

            MobsController.ai().bind(ce, StaticAI.AGGRESSIVE);

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