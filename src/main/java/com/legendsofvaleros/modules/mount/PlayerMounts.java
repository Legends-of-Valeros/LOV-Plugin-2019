package com.legendsofvaleros.modules.mount;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.mount.api.IMount;

import java.util.ArrayList;
import java.util.List;

public class PlayerMounts {
    @SerializedName("_id")
    final PlayerCharacter pc;

    final List<IMount> mounts;

    public PlayerMounts(PlayerCharacter pc) {
        this.pc = pc;

        this.mounts = new ArrayList<>();
    }
}