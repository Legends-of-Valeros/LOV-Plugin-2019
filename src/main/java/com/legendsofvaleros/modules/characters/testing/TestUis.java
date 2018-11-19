package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.modules.characters.creation.PlayerCreation;
import com.legendsofvaleros.modules.characters.skilleffect.RemovalReason;
import com.legendsofvaleros.modules.combatengine.ui.CombatEngineUiManager;
import com.legendsofvaleros.modules.combatengine.ui.PlayerCombatInterface;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffect;
import com.legendsofvaleros.modules.characters.ui.*;
import com.legendsofvaleros.modules.characters.ui.loading.BossBarView;
import com.legendsofvaleros.modules.characters.ui.loading.ProgressView;
import com.legendsofvaleros.modules.characters.ui.window.WindowCharacterSelect;
import com.legendsofvaleros.modules.characters.util.ShitUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TestUis implements CharactersUiManager, CombatEngineUiManager {
	@Override
	public ProgressView getProgressView(Player player) {
		return new BossBarView(player.getUniqueId(), "Loading, please wait..", BarColor.WHITE, BarStyle.SEGMENTED_10);
	}

	@Override
	public void forceCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener) {
		new WindowCharacterSelect(0, characters, listener, true).open(characters.getPlayer());
	}

	@Override
	public void openCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener) {
		new WindowCharacterSelect(0, characters, listener, false).open(characters.getPlayer());
	}

	@Override
	public void startCharacterCreation(Player player, int number, CharacterCreationListener listener) {
		player.setLevel(0);
		player.setExp(0F);
		
		player.getInventory().clear();

		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
		
		player.teleport(Characters.getInstance().getCharacterConfig().getCreateLocation());
		
		ShitUtil.doShit(player, Characters.getInstance().getCharacterConfig().getCreationStartText(), null);

		PlayerCreation.setCreating(player, number, listener);
	}

	@Override
	public void openCharacterCreation(Player player) {
		PlayerCreation.openCreation(player);
	}

	@Override
	public AbilityStatChangeListener getAbilityStatInterface(PlayerCharacter playerCharacter) {
		return null;
	}

	@Override
	public SkillEffectListener getCharacterEffectInterface(final PlayerCharacter pc) {
		return new SkillEffectListener() {
			@Override
			public void onSkillEffectUpdate(SkillEffect<?> effect, long expiry,
                                            int effectLevel) {
				MessageUtil.sendUpdate(pc.getPlayer(),
										new FancyMessage("You are now affected by ").color(ChatColor.GRAY)
											.then(effect.getUserFriendlyName(pc.getPlayer())).color(ChatColor.DARK_PURPLE)
											.tooltip(effect.getUserFriendlyDetails(pc.getPlayer())));
			}

			@Override
			public void onSkillEffectRemoved(SkillEffect<?> removed, int effectLevel,
                                             RemovalReason reason) {
				if (reason == RemovalReason.EXPIRED) {
					MessageUtil.sendUpdate(pc.getPlayer(), removed.getUserFriendlyName(pc.getPlayer()) + " wore off.");

				} else if (reason == RemovalReason.INTERRUPTED) {
					MessageUtil.sendUpdate(pc.getPlayer(), removed.getUserFriendlyName(pc.getPlayer()) + " was interrupted!");
				}
			}
		};
	}
	
	@Override
	public PlayerCombatInterface getPlayerInterface(Player player) {
		return new TestUiManager(player);
	}
}