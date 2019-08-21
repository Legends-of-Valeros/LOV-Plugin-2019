package com.legendsofvaleros.modules.zones.core;

import com.codingforcookies.ambience.Sound;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.zones.api.IZone;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.stream.Collectors;

public class Zone implements IZone {
    @SerializedName("_id")
    public String id;

    public String name;

    public Section[] sections;

    public transient Set<CharacterId> playersInZone = new HashSet<>();
    public transient boolean isActive = false;
    public transient long timeWithoutPlayers = 0;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public boolean isInside(Location loc) {
        for(Section section : sections) {
            if(section.isInside(loc))
                return true;
        }

        return false;
    }

    @Override
    public Optional<Section> getSection(Location loc) {
        for(Section section : sections) {
            if(section.isInside(loc))
                return Optional.of(section);
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Zone(id=" + id + ", name=" + name + ", sections=" + String.join(", ", Arrays.stream(sections).map(Section::toString).collect(Collectors.toList())) + "))";
    }

    public class Section {
        public String name;

        public Material material;
        public boolean pvp;

        public Sound[] ambience;

        public Zone getZone() {
            return Zone.this;
        }

        public boolean isInside(Location loc) {
            if (loc == null) {
                return false;
            }
            Block b = loc.getWorld().getBlockAt(loc.getBlockX(), 0, loc.getBlockZ());
            return b.getType() == material;
        }

        public boolean isInside(PlayerCharacter playerCharacter) {
            return playersInZone.contains(playerCharacter.getUniqueCharacterId());
        }

        public boolean isInside(CharacterId characterId) {
            return playersInZone.contains(characterId);
        }

        @Override
        public String toString() {
            return "Zone(material=" + material + ", pvp=" + pvp + ", ambience=Sounds(length=" + ambience.length + "))";
        }
    }
}