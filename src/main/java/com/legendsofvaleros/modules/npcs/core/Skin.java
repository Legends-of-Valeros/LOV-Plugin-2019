package com.legendsofvaleros.modules.npcs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.ISkin;

public class Skin implements ISkin {
    @SerializedName("_id")
    private String id;
    private String slug;

    public String uuid;
    public String username;
    public String signature;
    public String data;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Skin(id=" + id + ", username=" + username + ")";
    }
}