package com.legendsofvaleros.modules.characters.ui.window;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.core.GuiFlag;
import com.legendsofvaleros.features.gui.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SlotNewCharacter implements ISlotAction {
    private int number;
    private CharacterSelectionListener listener;

    public SlotNewCharacter(int number, CharacterSelectionListener listener) {
        this.number = number;
        this.listener = listener;
    }

    public void doAction(GUI gui, Player p, InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT) {
            if (listener.onNewCharacterSelected(p, number)) {
                gui.close(p, GuiFlag.NO_PARENTS);
            }
        }
    }
}