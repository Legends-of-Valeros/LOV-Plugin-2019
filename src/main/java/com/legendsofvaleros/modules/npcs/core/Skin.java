package com.legendsofvaleros.modules.npcs.core;

public class Skin {
    public String id;

    public String uuid;
    public String username;
    public String signature;
    public String data;

    @Override
    public String toString() {
        return "Skin(id=" + id + ", username=" + username + ")";
    }
}