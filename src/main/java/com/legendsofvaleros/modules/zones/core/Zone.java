package com.legendsofvaleros.modules.zones.core;

import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;

public class Zone {
    public String id;

    public String channel;
    public String name;
    public String subname;

    public MaterialWithData material;
    public boolean pvp;
    public Sound[] ambience;

    public ArrayList<CharacterId> playersInZone = new ArrayList<>();
    public boolean isActive = false;
    public long timeWithoutPlayers = 0;

    @SuppressWarnings("deprecation")
    public boolean isInZone(Location loc) {
        if (loc == null) return false;
        Block b = loc.getWorld().getBlockAt(loc.getBlockX(), 0, loc.getBlockZ());
        return b.getType() == material.type
                && (material.data != null && b.getData() == material.data);
    }

    public boolean isInZone(PlayerCharacter playerCharacter) {
        return playersInZone.contains(playerCharacter.getUniqueCharacterId());
    }

    public boolean isInZone(CharacterId characterId) {
        return playersInZone.contains(characterId);
    }

    @Override
    public String toString() {
        return "Zone(id=" + id + ", name=" + name + ", subname=" + subname + ", material=" + material + ", pvp=" + pvp + ", ambience=Sounds(length=" + ambience.length + "))";
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}