package com.legendsofvaleros.modules.zones.core;

import org.bukkit.Material;

public class MaterialWithData {
    public Material type;
    public Integer data;

    public MaterialWithData(String str) {
        if(!str.contains(":"))
            this.type = Material.valueOf(str);
        else {
            String[] strs = str.split(":");
            this.type = Material.valueOf(strs[0]);
            this.data = Integer.valueOf(strs[1]);
        }
    }

    @Override
    public String toString() {
        return type.name() + (data != null ? ":" + data : "");
    }
}
