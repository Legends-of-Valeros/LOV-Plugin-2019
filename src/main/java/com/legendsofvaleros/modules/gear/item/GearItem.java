package com.legendsofvaleros.modules.gear.item;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.item.NBTEditor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.gear.component.impl.ComponentMap;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.impl.PersistMap;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.ItemManager;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Table(name = "items")
public class GearItem {
    @Column(primary = true, name = "item_id", length = 64)
    private final String id;

    public String getID() {
        return id;
    }

    @Column(name = "item_group", length = 64)
    private String group;

    public String getGroup() {
        return group;
    }

    @Column(name = "item_version")
    private final int version;

    public int getVersion() {
        return version;
    }

    @Column(name = "item_name", length = 64)
    private String name;

    public String getName() {
        return name;
    }

    @Column(name = "item_type")
    private GearType type;

    public GearType getType() {
        return type;
    }

    @ForeignKey(table = Model.class, name = "model_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.SET_DEFAULT)
    @Column(name = "item_model", length = 48)
    private String modelId;

    public String getModelId() {
        return modelId;
    }

    public Model model;

    public Model getModel() {
        return model;
    }

    @Column(name = "item_amount")
    private byte maxAmount = 1;

    public byte getMaxAmount() {
        return maxAmount;
    }

    @Column(name = "item_rarity")
    private GearRarity rarity;

    public GearRarity getRarityLevel() {
        return rarity;
    }

    @Column(name = "item_components")
    private ComponentMap components;

    public GearItem(int version, String id) {
        this.version = version;
        this.id = id;
    }

    public int getSeed() {
        int seed = 0;
        for (int i = 0; i < id.length(); i++)
            seed += (int) id.charAt(i);
        return seed;
    }

    public GearItem.Instance newInstance() {
        GearItem.Instance instance = new GearItem.Instance(this, UUID.randomUUID());
        instance.amount = 1;

        for (Entry<String, GearComponent<?>> entry : components.entrySet()) {
            try {
                instance.persists.put(entry.getKey(), entry.getValue().onInit());
            } catch (Exception e) {
                LegendsOfValeros.getInstance().getLogger().severe("Failed to load component " + entry.getKey() + " on item " + id);
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, e, true);
                return null;
            }
        }

        return instance;
    }

    public boolean isSimilar(ItemStack stack) {
        return isSimilar(GearItem.Instance.fromStack(stack));
    }

    public boolean isSimilar(GearItem.Instance gear) {
        return gear != null && (this == gear.gear || this.id.equals(gear.gear.id));
    }

    @Override
    public String toString() {
        return "Gear(version=" + version + ", id=" + id + ", group=" + group + ", name=" + name + ", type=" + type + ", model=" + modelId + ", rarity=" + rarity + ", components=" + components + ")";
    }

    public static ListenableFuture<GearItem> fromID(String id) {
        return ItemManager.getItem(id);
    }

    public static class Instance {
        private static final Cache<String, GearItem.Instance> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build();

        public final GearItem gear;
        public int version;
        public UUID uuid;
        public int amount;
        public PersistMap persists = new PersistMap();

        public <T> T getPersist(Class<? extends GearComponent<T>> component) {
            return persists.getPersist(component);
        }

        public <T> void putPersist(Class<? extends GearComponent<T>> component, T obj) {
            persists.putPersist(component, obj);
        }

        public boolean hasComponent(Class<? extends GearComponent<?>> component) {
            return gear.components.getComponent(component) != null;
        }

        public <T extends GearComponent<?>> T getComponent(Class<T> component) {
            return gear.components.getComponent(component);
        }

        public Instance(GearItem gear, UUID uuid) {
            this.gear = gear;
            this.version = gear.version;
            this.uuid = uuid;
        }

        /**
         * Needed when creating two instances of an item from one instance. This prevents
         * their "amounts" from being syncronized when making copies.
         * <p>
         * This should only be used where ABSOLUTELY needed. It bypasses the instance
         * caching system. And doesn't understand instanciating persist data.
         */
        public Instance copy() {
            GearItem.Instance copy = new GearItem.Instance(gear, UUID.randomUUID());
            copy.amount = amount;
            copy.persists = persists;
            cache.put(copy.uuid.toString(), copy);
            return copy;
        }

        public double getValue() {
            double result = 0;
            for (Entry<String, GearComponent<?>> entry : gear.components.entrySet())
                result += entry.getValue().doGetValue(this, persists.get(entry.getKey()));
            return result;
        }

        public Boolean doTest(GearTrigger trigger) {
            Boolean result = null;
            Boolean test;
            for (Entry<String, GearComponent<?>> entry : gear.components.entrySet()) {
                test = entry.getValue().doTest(this, persists.get(entry.getKey()), trigger);
                if (test == null) continue;
                if (!test) return false;
                result = true;
            }
            return result;
        }

        public GearTrigger.TriggerEvent doFire(GearTrigger trigger) {
            boolean changed = false;

            int amount = this.amount;

            Object update;
            for (Entry<String, GearComponent<?>> entry : gear.components.entrySet()) {
                update = entry.getValue().doFire(this, persists.get(entry.getKey()), trigger);

                if (update != null) {
                    persists.put(entry.getKey(), update);
                    changed = true;
                }
            }

            if (amount != this.amount) changed = true;

            if (trigger.shouldRefreshStack()) return GearTrigger.TriggerEvent.REFRESH_STACK;
            if (changed) return GearTrigger.TriggerEvent.NBT_UPDATED;
            return GearTrigger.TriggerEvent.NOTHING;
        }

        public static GearItem.Instance fromID(String id) {
            ListenableFuture<GearItem> future = GearItem.fromID(id);
            try {
                GearItem.Instance instance = new GearItem.Instance(future.get(), UUID.randomUUID());
                if (instance.gear == null) return null;
                instance.amount = 1;
                instance.persists = new PersistMap();
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public static GearItem.Instance fromStack(ItemStack stack) {
            if (stack == null || stack.getType() == Material.AIR) return null;

            NBTEditor nbt = new NBTEditor(stack);

            if (nbt.getString("lov.name") == null)
                return null;

            if (nbt.getString("lov.cache") != null) {
                GearItem.Instance data = cache.getIfPresent(nbt.getString("lov.cache"));
                if (data != null) {
                    data.amount = stack.getAmount();
                    return data;
                }
            }

            ListenableFuture<GearItem> future = GearItem.fromID(nbt.getString("lov.name"));
            try {
                GearItem gear = future.get(5L, TimeUnit.SECONDS);
                if (gear == null)
                    return null;

                GearItem.Instance data = new GearItem.Instance(gear, nbt.getString("lov.cache") == null ? UUID.randomUUID() : UUID.fromString(nbt.getString("lov.cache")));
                data.version = nbt.getInteger("lov.version");
                data.amount = stack.getAmount();
                data.persists = ItemManager.gson.fromJson(nbt.getString("lov.persist"), PersistMap.class);
                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public GearItem.Data getData() {
            GearItem.Data data = new GearItem.Data();

            data.id = gear.id;
            data.amount = amount;
            data.persist = persists;

            return data;
        }

        public ItemStack toStack() {
            if (amount == 0) return null;

            try {
                ItemBuilder builder = gear.model.toStack();

                if (gear.type.isRarityable() && gear.rarity != null)
                    builder.addLore(gear.rarity.getChatColor() + gear.rarity.getUserFriendlyName());

                if (gear.components != null && gear.components.size() > 0) {
                    if (version != gear.version)
                        persists.clear();

                    for (Entry<String, GearComponent<?>> entry : gear.components.entrySet())
                        if (!persists.containsKey(entry.getKey()) || persists.get(entry.getKey()) == null)
                            persists.put(entry.getKey(), entry.getValue().onInit());

                    for (GearComponentOrder currentOrder : GearComponentOrder.values()) {
                        boolean added = false;
                        int loreSize = builder.getLore().size();

                        for (Entry<String, GearComponent<?>> entry : gear.components.entrySet()) {
                            if (entry.getValue().getOrder() != currentOrder) continue;

                            entry.getValue().doGenerate(this, persists.get(entry.getKey()), builder);

                            if (loreSize != builder.getLore().size()) {
                                loreSize = builder.getLore().size();
                                added = true;
                            }
                        }

                        if (added || currentOrder.shouldForceSpace())
                            builder.addLore("");
                    }

                    builder.trimLore();

                    builder.addLore("");
                    builder.addLore(ChatColor.GOLD + "" + ChatColor.ITALIC + this.getValue() + " V");
                }

                //lore.add(String.format(ChatColor.GOLD + "%siP", (int)(statItem.getItemPower() * 100) / 100D));

                if (!gear.type.isTradable())
                    builder.addLore(ChatColor.RED + "Untradable");

                builder.setTag("lov.name", gear.id);

                builder.setTag("lov.cache", uuid.toString());

                String json = ItemManager.gson.toJson(persists);
                builder.setTag("lov.persist", json);

                builder.hideAttributes();

                builder.setName((gear.rarity != null ? gear.rarity.getChatColor() : ChatColor.RESET) + gear.name);
                builder.unbreakable();

                cache.put(uuid.toString(), this);

                builder.setAmount(amount);

                return builder.create();
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, e, true);

                GearItem.Instance instance = Gear.ERROR_ITEM.newInstance();
                instance.amount = amount;
                return instance.toStack();
            }
        }
    }

    public static class Data {
        public int version;
        public String id;
        public int amount;
        public PersistMap persist = new PersistMap();

        public GearItem.Instance toInstance() {
            try {
                GearItem.Instance instance = new GearItem.Instance(GearItem.fromID(id).get(5L, TimeUnit.SECONDS), UUID.randomUUID());
                instance.version = version;
                instance.amount = amount;
                instance.persists = persist;
                return instance;
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, e, true);
            }
            return null;
        }

        public ItemStack toStack() {
            GearItem.Instance instance = toInstance();
            if (instance == null) return null;
            return instance.toStack();
        }

        public static Data loadData(String json) {
            try {
                return ItemManager.gson.fromJson(json, Data.class);
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        public String toString() {
            return ItemManager.gson.toJson(this);
        }
    }
}