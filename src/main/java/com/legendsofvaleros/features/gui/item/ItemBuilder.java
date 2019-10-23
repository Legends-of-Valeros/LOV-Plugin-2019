package com.legendsofvaleros.features.gui.item;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;

public class ItemBuilder {
    private ItemStack stack;
    private int amount;
    private String name;
    private boolean unbreakable = false;
    private List<String> lore;
    private Set<ItemFlag> flags;
    private HashMap<String, Object> nbt;

    public Material getMaterial() {
        return stack.getType();
    }

    public ItemBuilder setMaterial(Material material) {
        stack.setType(material);
        return this;
    }

    public int getDurability() {
        return stack.getDurability();
    }

    public ItemBuilder setDurability(short durability) {
        stack.setDurability(durability);
        return this;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public List<String> getLore() {
        return lore;
    }


    public Set<ItemFlag> getFlags() {
        return flags;
    }


    public HashMap<String, Object> getTags() {
        return nbt;
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(Material material, int i) {
        this(new ItemStack(material, 1, (short) i));
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = (stack != null ? stack.clone() : new ItemStack(Material.BEDROCK));
        this.amount = stack.getAmount();

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            this.unbreakable = meta.isUnbreakable();
            this.name = meta.getDisplayName();
            this.lore = meta.getLore();
            this.flags = meta.getItemFlags();
        }

        if (this.lore == null)
            this.lore = new ArrayList<>();
        if (this.flags == null)
            this.flags = new HashSet<>();
        if (this.nbt == null)
            this.nbt = new HashMap<>();
    }

    public ItemBuilder setName(String name) {
        if (name == null)
            this.name = String.valueOf(ChatColor.RESET);
        else
            this.name = ChatColor.RESET + name;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = Math.min(64, amount);
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        for (String line : lines)
            lore.add(ChatColor.GRAY + line);
        return this;
    }

    public ItemBuilder clearLore() {
        lore.clear();
        return this;
    }

    public void trimLore() {
        while (lore.size() > 0 && ChatColor.stripColor(lore.get(lore.size() - 1).trim()).length() == 0)
            lore.remove(lore.size() - 1);
    }

    public ItemBuilder addFlag(ItemFlag... flags) {
        Collections.addAll(this.flags, flags);
        return this;
    }

    public ItemBuilder clearFlags() {
        flags.clear();
        return this;
    }

    public ItemBuilder hideAttributes() {
        this.flags.add(ItemFlag.HIDE_ATTRIBUTES);
        this.flags.add(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder setTag(String name, Object str) {
        if (nbt == null)
            nbt = new HashMap<>();
        nbt.put(name, str);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getTag(String name, Class<T> c) {
        if (nbt == null) return null;
        return (T) nbt.get(name);
    }

    public ItemBuilder unsetTag(String name) {
        if (nbt == null)
            return this;
        nbt.remove(name);
        return this;
    }

    public ItemBuilder unbreakable() {
        unbreakable = true;
        return this;
    }

    public ItemBuilder setEnchanted(boolean enchanted) {
        if (enchanted)
            setTag("ench", new NBTTagList());
        else
            unsetTag("ench");
        return this;
    }

    public ItemBuilder addAttributeMod(String attribute, Attributes.Operation op, Object value) {
        NBTTagList modifiers = getTag("AttributeModifiers", NBTTagList.class);
        if (modifiers == null) modifiers = new NBTTagList();

        NBTTagCompound tag = new NBTTagCompound();
        tag.set("AttributeName", new NBTTagString(attribute));
        tag.set("Name", new NBTTagString(attribute));

        if (value instanceof Integer)
            tag.set("Amount", new NBTTagInt((Integer) value));
        else if (value instanceof Double)
            tag.set("Amount", new NBTTagDouble((Double) value));
        else if (value instanceof Float)
            tag.set("Amount", new NBTTagFloat((Float) value));
        else
            throw new RuntimeException("Unknown attribute value type.");

        tag.set("Operation", new NBTTagInt(op.ordinal()));
        tag.set("UUIDLeast", new NBTTagInt(894654));
        tag.set("UUIDMost", new NBTTagInt(2872));
        modifiers.add(tag);

        return setTag("AttributeModifiers", modifiers);
    }

    public ItemStack create() {
        ItemStack stack = new ItemStack(this.stack);

        if (nbt != null) {
            net.minecraft.server.v1_14_R1.ItemStack tagStack = CraftItemStack.asNMSCopy(stack);
            if (tagStack == null) {
                this.stack = new ItemStack(Material.BEDROCK);
                return create();
            }
            NBTTagCompound tags = null;
            if (! tagStack.hasTag()) {
                tags = new NBTTagCompound();
                tagStack.setTag(tags);
            }
            if (tags == null) tags = tagStack.getTag();

            for (Entry<String, Object> tag : nbt.entrySet()) {
                if (tag.getValue() instanceof String)
                    tags.setString(tag.getKey(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    tags.setInt(tag.getKey(), (Integer) tag.getValue());

                else if (tag.getValue() instanceof NBTBase)
                    tags.set(tag.getKey(), (NBTBase) tag.getValue());
            }
            tagStack.setTag(tags);

            stack = CraftItemStack.asBukkitCopy(tagStack);
        }

        stack.setAmount(amount);

        ItemMeta meta = stack.getItemMeta();

        if (meta != null) {
            if (name != null) {
                if (name.equals(String.valueOf(ChatColor.RESET))) {
                    if (lore.size() > 0) {
                        meta.setDisplayName(lore.get(0));
                        lore.remove(0);
                    } else
                        meta.setDisplayName(name);
                } else
                    meta.setDisplayName(name);
            }

            meta.setUnbreakable(unbreakable);

            if (lore != null)
                meta.setLore(lore);

            for (ItemFlag flag : flags)
                meta.addItemFlags(flag);

            stack.setItemMeta(meta);
        }

        return stack;
    }

    public static class Attributes {
        public static final String ATTACK_SPEED = "generic.attackSpeed";
        public static final String MAX_HEALTH = "generic.maxHealth";
        public static final String FOLLOW_RANGE = "generic.followRange";
        public static final String ATTACK_DAMAGE = "generic.attackDamage";
        public static final String MOVEMENT_SPEED = "generic.movementSpeed";
        public static final String KNOCKBACK_RESISTANCE = "generic.knockbackResistance";

        public enum Operation {
            ADD_NUMBER,
            MULTIPLY_PERCENTAGE,
            ADD_PERCENTAGE
        }
    }
}