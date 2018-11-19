package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptions;

public class Test {
    public Test() {
        new TestListener();
        Characters.getInstance().setUiManager(new TestUis());
        CombatEngine.getInstance().setUserInterfaceManager(new TestUis());

        InventoryManager.addFixedItem(41, new InventoryManager.InventoryItem(null,
                (p, event) -> {
                    if (event.isRightClick()) {
                        PlayerOptions.open(p);
                    } else if (event.isLeftClick()) {
                        p.performCommand("character journal");
                    }
                }));
    }
}