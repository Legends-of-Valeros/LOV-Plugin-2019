package com.legendsofvaleros.modules.mobs.api;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.loot.api.ILootTable;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.core.EntityRarity;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IEntity {
    String getId();

    String getName();

    Set<SpawnArea> getSpawns();

    EntityType getType();

    EntityRarity getRarity();

    String getArchetype();

    EntityClass getEntityClass();

    ISkin getSkin();

    int getExperience();

    boolean isInvincible();

    boolean isGhost();

    StatsMap getStats();

    Integer getStat(Enum e);

    EquipmentMap getEquipment();

    Loot[] getLoot();

    Distance getDistance();

    Map<CharacterId, Long> getLeashed();

    class StatsMap extends HashMap<Object, Integer> { }
    class EquipmentMap extends HashMap<Equipment.EquipmentSlot, Item[]> { }

    class Loot {
        public int tries;
        public double chance;
        public Table[] tables;

        public static class Table {
            public ILootTable table;
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

            public Optional<ILootTable> nextTable() {
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

    class Distance {
        public Integer detection = 5;
        public Integer chase = 10;
    }

    class Item {
        private String id;

        private int amount = 1;
        private double chance = 1;

        public double getChance() {
            return chance;
        }

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
}