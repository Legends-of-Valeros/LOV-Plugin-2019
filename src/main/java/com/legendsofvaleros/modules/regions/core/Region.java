package com.legendsofvaleros.modules.regions.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuest;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Region implements IRegion {
    @SerializedName("_id")
    private String id;
    private String slug;

    public World world;
    private RegionBounds bounds;
    public boolean allowAccess = false;
    public boolean allowHearthstone = true;
    public List<IQuest> quests = new ArrayList<>();

    public String msgEnter;
    public String msgExit;
    public String msgError = "You cannot go there, yet.";

    public Region(String id, World world, RegionBounds bounds) {
        this.id = id;
        this.world = world;
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
        return world;
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
        return quests;
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