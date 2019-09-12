package com.legendsofvaleros.modules.regions.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.api.Ref;
import com.legendsofvaleros.modules.quests.api.IQuest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Region implements IRegion {
    @SerializedName("_id")
    private String id;
    private String slug;

    private RegionBounds bounds;
    public boolean allowAccess = false;
    public boolean allowHearthstone = true;
    public List<Ref<IQuest>> quests = new ArrayList<>();

    public String msgEnter;
    public String msgExit;
    public String msgError = "You cannot go there, yet.";

    public Region(String id, RegionBounds bounds) {
        this.id = id;
        this.bounds = bounds;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isInside(Location location) {
        return bounds.isInside(location);
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public RegionBounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isAllowedByDefault() {
        return allowAccess;
    }

    @Override
    public boolean areHearthstonesAllowed() {
        return allowHearthstone;
    }

    @Override
    public List<IQuest> getQuestsTriggered() {
        return quests.stream().map(v -> v.get()).collect(Collectors.toList());
    }

    @Override
    public String getEnterMessage() {
        return msgEnter;
    }

    @Override
    public String getExitMessage() {
        return msgExit;
    }

    @Override
    public String getErrorMessage() {
        return msgError;
    }
}