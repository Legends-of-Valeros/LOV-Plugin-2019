package com.legendsofvaleros.modules.fast_travel;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import java.util.ArrayList;
import java.util.List;

public class PlayerFastTravels {
    @SerializedName("_id")
    final PlayerCharacter pc;

    final List<String> locations;

    public PlayerFastTravels(PlayerCharacter pc) {
        this.pc = pc;

        this.locations = new ArrayList<>();
    }
}