package com.legendsofvaleros.modules.fast_travel;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.npcs.NPCsController;

@DependsOn(CombatEngine.class)
@DependsOn(BankController.class)
@DependsOn(Characters.class)
@DependsOn(NPCsController.class)
@ModuleInfo(name = "FastTravel", info = "")
public class FastTravelController extends FastTravelAPI {
    private static FastTravelController instance;
    public static FastTravelController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;
    }
}