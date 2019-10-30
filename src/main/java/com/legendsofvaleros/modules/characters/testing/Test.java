package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.features.playermenu.InventoryManager;
import com.legendsofvaleros.features.playermenu.options.PlayerOptions;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;

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