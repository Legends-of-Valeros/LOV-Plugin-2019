package com.codingforcookies.robert.item;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemReader {
    public final String name;
    public final int amount;
    public final boolean unbreakable;
    public final boolean enchanted;
    public final List<String> lore;
    public final Set<ItemFlag> flags;
    public final NBTTagCompound nbt;
    public Map<String, Double> attributes;

    public ItemReader(ItemStack stack) {
        if (stack == null) {
            name = null;
            amount = 0;
            unbreakable = false;
            enchanted = false;
            lore = new ArrayList<>();
            flags = new HashSet<>();
            nbt = null;
            return;
        }

        this.amount = stack.getAmount();

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            this.name = meta.getDisplayName();
            this.unbreakable = meta.isUnbreakable();
            this.lore = meta.getLore();
            this.flags = meta.getItemFlags();
        } else {
            name = null;
            unbreakable = false;
            lore = new ArrayList<>();
            flags = new HashSet<>();
        }

        net.minecraft.server.v1_14_R1.ItemStack tagStack = CraftItemStack.asNMSCopy(stack);
        if (tagStack != null) {
            nbt = tagStack.getTag();
            enchanted = nbt != null && nbt.hasKey("ench");
        } else {
            nbt = null;
            enchanted = false;
        }
    }

    public <T> T getTag(String name, Class<T> c) {
        if (nbt == null) return null;
        return (T) nbt.get(name);
    }

    private void loadAttributes() {
        attributes = new HashMap<>();

        NBTTagList modifiers = getTag("AttributeModifiers", NBTTagList.class);
        if (modifiers == null) return;

        for (NBTBase modifier : modifiers) {
            NBTTagCompound tag = (NBTTagCompound) modifier;

            if (tag.get("Amount") instanceof NBTTagInt) {
                attributes.put(tag.getString("AttributeName"), (double) tag.getInt("Amount"));
            } else if (tag.get("Amount") instanceof NBTTagDouble) {
                attributes.put(tag.getString("AttributeName"), tag.getDouble("Amount"));
            } else if (tag.get("Amount") instanceof NBTTagFloat) {
                attributes.put(tag.getString("AttributeName"), (double) tag.getFloat("Amount"));
            }
        }
    }

    public boolean hasAttribute(String name) {
        if (attributes == null) loadAttributes();
        return attributes.containsKey(name);
    }

    public Double getAttribute(String name) {
        if (attributes == null) loadAttributes();
        return attributes.get(name);
    }
}