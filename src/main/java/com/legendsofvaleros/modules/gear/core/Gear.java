package com.legendsofvaleros.modules.gear.core;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.item.NBTEditor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.component.ComponentMap;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.PersistMap;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Gear implements IGear {
    @SerializedName("_id")
    private String id;
    private String slug;

    private final int version;

    private String name;
    private GearType type;
    private String modelId;
    private byte maxAmount = 1;
    private GearRarity rarity;
    private ComponentMap components;

    public Gear(int version, String id) {
        this.version = version;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getVersion() {
        return version;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public GearType getType() {
        return type;
    }


    @Override
    public String getModelId() {
        return modelId;
    }

    public Model getModel() {
        return Model.get(modelId);
    }


    @Override
    public byte getMaxAmount() {
        return maxAmount;
    }

    @Override
    public GearRarity getRarityLevel() {
        return rarity;
    }

    @Override
    public int getSeed() {
        int seed = 0;
        for (int i = 0; i < id.length(); i++) {
            seed += (int) id.charAt(i);
        }
        return seed;
    }

    /**
     * Instances a new instance of this Gear with default component persistent data, and an amount of 1.
     */
    @Override
    public Gear.Instance newInstance() {
        Gear.Instance instance = new Gear.Instance(this, UUID.randomUUID());
        instance.amount = 1;

        for (Entry<String, GearComponent<?>> entry : components.entrySet()) {
            try {
                instance.persists.put(entry.getKey(), entry.getValue().onInit());
            } catch (Exception e) {
                GearController.getInstance().getLogger().severe("Failed to load component " + entry.getKey() + " on item " + id);
                MessageUtil.sendException(GearController.getInstance(), e);
                return null;
            }
        }

        return instance;
    }

    @Override
    public boolean isSimilar(ItemStack stack) {
        return isSimilar(Gear.Instance.fromStack(stack));
    }

    @Override
    public boolean isSimilar(Gear.Instance gearInstance) {
        return isSimilar(gearInstance.gear);
    }

    @Override
    public boolean isSimilar(IGear gear) {
        return gear != null && (this == gear || this.id.equals(gear.getId()));
    }

    @Override
    public String toString() {
        return "GearController(version=" + version + ", id=" + id + ", name=" + name + ", type=" + type + ", model=" + modelId + ", rarity=" + rarity + ", components=" + components + ")";
    }

    public static Gear fromId(String id) {
        return GearController.getInstance().getGear(id);
    }

    /**
     * Gear.Instance is the true internal definition of an item, its components, and their respective persistent data.
     */
    public static class Instance {
        private static final Cache<String, Gear.Instance> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build();

        public final Gear gear;
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

        public Instance(Gear gear, UUID uuid) {
            this.gear = gear;
            this.version = gear.version;
            this.uuid = uuid;
        }

        /**
         * Needed when creating two instances of an item from one instance. This prevents
         * their "amounts" from being synchronized when making copies.
         * <p>
         * This should only be used where ABSOLUTELY needed. It bypasses the instance
         * caching system. And doesn't understand instanciating persist data.
         */
        public Instance copy() {
            Gear.Instance copy = new Gear.Instance(gear, UUID.randomUUID());
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

        /**
         * Fires a trigger in the gear's components.
         * <p/>
         * The fire function returns an object if its persistent data has changed, in which case the NBT of that item
         * must be updated. In this case NBT_UPDATED is returned. All trigger calls are to respect this value.
         * <p/>
         * In some cases, the persistent data is not updated, or persistent data does not exist, but the stack must be
         * refreshed regardless. In this case REFRESH_STACK is returned. All trigger calls are to respect this value.
         * <p/>
         * If the function returns NOTHING, then no action is necessary, and the item is not to be refreshed.
         * <p/>
         * Note that if the item updates any of its data, and does not request the stack be refreshed, the change will
         * be lost once the cache is invalidated.
         */
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

            if (amount != this.amount) {
                changed = true;
            }

            if (trigger.shouldRefreshStack()) {
                return GearTrigger.TriggerEvent.REFRESH_STACK;
            }

            if (changed) {
                return GearTrigger.TriggerEvent.NBT_UPDATED;
            }

            return GearTrigger.TriggerEvent.NOTHING;
        }

        /**
         * Turns a Gear ID into a gear instance.
         */
        public static Gear.Instance fromId(String id) {
            try {
                Gear.Instance instance = new Gear.Instance(Gear.fromId(id), UUID.randomUUID());
                if (instance.gear == null) return null;
                instance.amount = 1;
                instance.persists = new PersistMap();
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Attempts to get a gear instance from an ItemStack.
         */
        public static Gear.Instance fromStack(ItemStack stack) {
            // ?Should we return the error item, here?
            if (stack == null || stack.getType() == Material.AIR)
                return null;

            // Instance the NBT editor, as we store a couple bits of information relating to the gear.
            // lov.name is the gear ID of the item.
            // lov.cache is the cache UUID for the item.
            // lov.version is an integer version of the item. If this does not match the current gear version, it is to be
            //      regenerated when turned back into an ItemStack.
            NBTEditor nbt = new NBTEditor(stack);

            if (nbt.getString("lov.name") == null) {
                // ?Should we return the error item, here?
                return null;
            }

            if (nbt.getString("lov.cache") != null) {
                Gear.Instance data = cache.getIfPresent(nbt.getString("lov.cache"));
                if (data != null) {
                    data.amount = stack.getAmount();
                    return data;
                }
            }

            Gear gear = Gear.fromId(nbt.getString("lov.name"));
            if (gear == null) {
                // ?Should we return the error item, here?
                return null;
            }

            Gear.Instance data = new Gear.Instance(gear, nbt.getString("lov.cache") == null ? UUID.randomUUID() : UUID.fromString(nbt.getString("lov.cache")));
            data.version = nbt.getInteger("lov.version");
            data.amount = stack.getAmount();
            data.persists = APIController.getInstance().getGson().fromJson(nbt.getString("lov.persist"), PersistMap.class);

            return data;
        }

        public Gear.Data getData() {
            Gear.Data data = new Gear.Data();

            data.id = gear.id;
            data.amount = amount;
            data.persist = persists;

            return data;
        }

        /**
         * Converts the Gear Instance into an ItemStack that Minecraft uses for its inventory management.
         */
        public ItemStack toStack() {
            if (amount <= 0) {
                return new ItemStack(Material.AIR);
            }

            try {
                ItemBuilder builder = gear.getModel().toStack();

                // Add the rarity to the lore
                if (gear.type.isRarityable() && gear.rarity != null)
                    builder.addLore(gear.rarity.getChatColor() + gear.rarity.getUserFriendlyName());

                if (gear.components != null && gear.components.size() > 0) {
                    // If the versions do not match, persistent data is regenerated.
                    // ?Is this what we want to do?
                    // ?Shouldn't this be in fromStack, rather than toStack?
                    if (version != gear.version) {
                        persists.clear();
                    }

                    // Loop through each of the components and initialize their persistent data if necessary.
                    for (Entry<String, GearComponent<?>> entry : gear.components.entrySet()) {
                        if (!persists.containsKey(entry.getKey()) || persists.get(entry.getKey()) == null) {
                            persists.put(entry.getKey(), entry.getValue().onInit());
                        }
                    }

                    // Loop through each of the components and let them alter the item as they deem necessary.
                    for (GearComponentOrder currentOrder : GearComponentOrder.values()) {
                        boolean added = false;
                        int loreSize = builder.getLore().size();

                        for (Entry<String, GearComponent<?>> entry : gear.components.entrySet()) {
                            if (entry.getValue().getOrder() != currentOrder) {
                                continue;
                            }

                            entry.getValue().doGenerate(this, persists.get(entry.getKey()), builder);

                            if (loreSize != builder.getLore().size()) {
                                loreSize = builder.getLore().size();
                                added = true;
                            }
                        }

                        if (added || currentOrder.shouldForceSpace()) {
                            builder.addLore("");
                        }
                    }

                    // Trim off empty lines.
                    builder.trimLore();

                    // If we're in a verbose environment, include some additional debug data.
                    if (LegendsOfValeros.getMode().isVerbose()) {
                        builder.addLore("");
                        builder.addLore(ChatColor.GOLD + "" + ChatColor.ITALIC + this.getValue() + " V");
                    }
                }

                //lore.add(String.format(ChatColor.GOLD + "%siP", (int)(statItem.getItemPower() * 100) / 100D));

                if (!gear.type.isTradable()) {
                    builder.addLore(ChatColor.RED + "Untradable");
                }

                // Set the lov.name tag to the ID of the gear for loading in the future.
                builder.setTag("lov.name", gear.id);

                // Update the cache UUID.
                builder.setTag("lov.cache", uuid.toString());

                // Turn the persistent data into a JSON string
                // ?Should we store persistent data in raw NBT?
                String json = APIController.getInstance().getGson().toJson(persists);
                builder.setTag("lov.persist", json);

                // Hide all special vanilla attributes.
                builder.hideAttributes();
                builder.setName((gear.rarity != null ? gear.rarity.getChatColor() : ChatColor.RESET) + gear.name);

                // TODO: When we swap back to using vanilla durability, remove this.
                builder.unbreakable();
                builder.setAmount(amount);

                // Revalidate the cache with the current gear instance.
                cache.put(uuid.toString(), this);

                return builder.create();
            } catch (Exception e) {
                MessageUtil.sendException(GearController.getInstance(), e);

                // If an error occurs during processing, replace it with the error item.
                Gear.Instance instance = GearController.ERROR_ITEM.newInstance();
                instance.amount = amount;
                return instance.toStack();
            }
        }
    }

    /**
     * Gear.Data is the data structure used for storing the information necessary to reconstruct a Gear Instance in
     * a database.
     */
    public static class Data {
        @SerializedName("_id")
        private String id;
        private String slug;

        public int version;
        public int amount;
        public PersistMap persist = new PersistMap();

        public Gear.Instance toInstance() {
            Gear gear = Gear.fromId(id);
            if (gear == null) return null;

            Gear.Instance instance = new Gear.Instance(gear, UUID.randomUUID());
            instance.version = version;
            instance.amount = amount;
            instance.persists = persist;
            return instance;
        }

        public ItemStack toStack() {
            Gear.Instance instance = toInstance();
            if (instance == null) return null;
            return instance.toStack();
        }

        @Override
        public String toString() {
            return APIController.getInstance().getGson().toJson(this);
        }
    }
}