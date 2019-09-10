package com.legendsofvaleros.modules.npcs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.ISkin;

import java.util.Random;

public class Skin implements ISkin {
    private static final Random RAND = new Random();

    @SerializedName("_id")
    private String id;
    private String slug;

    public Texture[] textures;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public Texture getTexture() {
        return textures.length > 0 ? textures[RAND.nextInt(textures.length)] : null;
    }

    @Override
    public String toString() {
        return "Skin(id=" + id + ", textures=" + textures + ")";
    }
}