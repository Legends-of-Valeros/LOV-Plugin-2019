package com.legendsofvaleros.modules.characters.ui.window;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SlotCharacter implements ISlotAction {
	private PlayerCharacters characters;
	private CharacterSelectionListener listener;
	private boolean forced;
	
	private int characterId;
	
	public SlotCharacter(int characterId, PlayerCharacters characters, CharacterSelectionListener listener, boolean forced) {
		this.characters = characters;
		this.listener = listener;
		this.forced = forced;
		
		this.characterId = characterId;
	}
	
	public void doAction(GUI gui, Player p, InventoryClickEvent event) {
		if(event.getClick() == ClickType.LEFT) {
			if(listener.onCharacterSelected(p, characters.getForNumber(characterId).getUniqueCharacterId()))
				gui.close(p, GUI.Flag.NO_PARENTS);
		}else if(event.getClick() == ClickType.RIGHT) {
			new WindowCharacterInformation(characters, listener, characterId, forced).open(p);
		}
	}
}