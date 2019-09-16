package com.legendsofvaleros.features.gui.item;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTEditor {
    NbtCompound nbt;

    public NBTEditor(ItemStack stack) {
        this.nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(CraftItemStack.asCraftCopy(stack)));
    }

    public boolean getBoolean(String name) {
        try {
            if (nbt != null)
                return Boolean.valueOf("" + nbt.getObject(name));
        } catch (Exception e) {
        }
        return false;
    }

    public byte getByte(String name) {
        try {
            if (nbt != null)
                return nbt.getByte(name);
        } catch (Exception e) {
        }
        return 0;
    }

    public int getInteger(String name) {
        try {
            if (nbt != null)
                return nbt.getInteger(name);
        } catch (Exception e) {
        }
        return 0;
    }

    public double getDouble(String name) {
        try {
            if (nbt != null)
                return nbt.getDouble(name);
        } catch (Exception e) {
        }
        return 0;
    }

    public float getFloat(String name) {
        try {
            if (nbt != null)
                return nbt.getFloat(name);
        } catch (Exception e) {
        }
        return 0;
    }

    public String getString(String name) {
        try {
            if (nbt != null)
                return nbt.getString(name);
        } catch (Exception e) {
        }
        return null;
    }

    public Object getObject(String name) {
        try {
            if (nbt != null)
                return nbt.getObject(name);
        } catch (Exception e) {
        }
        return null;
    }

    public NBTEditor setBoolean(String name, boolean value) {
        if (nbt != null)
            nbt.putObject(name, value ? 1 : 0);
        return this;
    }

    public NBTEditor setByte(String name, byte value) {
        if (nbt != null)
            nbt.put(name, value);
        return this;
    }

    public NBTEditor setInteger(String name, int value) {
        if (nbt != null)
            nbt.put(name, value);
        return this;
    }

    public NBTEditor setDouble(String name, double value) {
        if (nbt != null)
            nbt.put(name, value);
        return this;
    }

    public NBTEditor setFloat(String name, float value) {
        if (nbt != null)
            nbt.put(name, value);
        return this;
    }

    public NBTEditor setString(String name, String value) {
        if (nbt != null)
            nbt.put(name, value);
        return this;
    }

    public NBTEditor setObject(String name, Object value) {
        if (nbt != null)
            nbt.putObject(name, value);
        return this;
    }

    public ItemStack apply(ItemStack stack) {
        stack = CraftItemStack.asCraftCopy(stack);
        NbtFactory.setItemTag(stack, nbt);
        return stack;
    }
}