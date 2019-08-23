package com.legendsofvaleros.modules.playerdata;

import java.util.UUID;

public class PlayerData {
    public final UUID uuid;
    public String username;
    public String resourcePack;
    public boolean resourcePackForced;

    PlayerData(UUID uuid) {
        this.uuid = uuid;
    }
}