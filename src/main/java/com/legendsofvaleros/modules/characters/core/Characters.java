package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.CharactersAPI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.config.BukkitConfig;
import com.legendsofvaleros.modules.characters.config.CharactersConfig;
import com.legendsofvaleros.modules.characters.creation.PlayerCreation;
import com.legendsofvaleros.modules.characters.loading.PlayerLock;
import com.legendsofvaleros.modules.characters.skilleffect.PersistingEffects;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffects;
import com.legendsofvaleros.modules.characters.stat.PersistentRegeneratingStats;
import com.legendsofvaleros.modules.characters.testing.Test;
import com.legendsofvaleros.modules.characters.ui.*;
import com.legendsofvaleros.modules.characters.ui.loading.ProgressView;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Characters allows players to make multiple playable characters, each with their own race, class,
 * and progression.
 */
@DependsOn(NPCsController.class)
@DependsOn(CombatEngine.class)
@DependsOn(LevelArchetypes.class)
@DependsOn(PlayerMenu.class)
@ModuleInfo(name = "Characters", info = "")
public class Characters extends Module implements CharactersAPI {
    private static Characters instance;

    public static Characters getInstance() throws IllegalStateException {
        return instance;
    }

    private CharactersConfig config;
    private CharactersUiManager uiManager;

    private CombatStatusTracker combatStatusTracker;
    private PlayerLoader loader;
    private PersistentRegeneratingStats persistentRegenStats;
    private SkillEffects skillEffects;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new CharacterCommands());

        // configuration
        config = new BukkitConfig();

        // default ui manager
        uiManager = new NoUiManager();

        // misc utilities
        LoginTime.onEnable();

        // combat listeners
        new BasicCombat(config);
        combatStatusTracker = new CombatStatusTracker(config);

        // initializes core player-character data
        PlayerCharacterData.onEnable();

        // initializes persistent health, mana, and energy data
        persistentRegenStats = new PersistentRegeneratingStats();

        // player loading
        PlayerLock.onEnable();
        loader = new PlayerLoader(combatStatusTracker);

        PlayerCreation.onEnable();

        // changing stats on leveling up
        new LevelingListener(config);

        // persistent duration-based effects from skills/spells/etc.
        this.skillEffects = new SkillEffects();
        PersistingEffects.onEnable(skillEffects);

        // provides player-characters' levels to other plugins
        LevelArchetypes.getInstance().registerLevelProvider(new PlayerCharacterLevelProvider(),
                EntityType.PLAYER);

        new Test();
    }

    public static void openCharacterSelection(Player p) {
        getInstance().uiManager.openCharacterSelection(getInstance().getCharacters(p), getInstance().loader);
    }

    public static boolean isPlayerCharacterLoaded(UUID uuid) {
        return instance.isCharacterLoaded(Bukkit.getPlayer(uuid));
    }

    public static PlayerCharacter getPlayerCharacter(UUID uuid) {
        return instance.getCurrentCharacter(Bukkit.getPlayer(uuid));
    }

    public static PlayerCharacter getPlayerCharacter(CharacterId id) {
        return instance.getCharacter(id);
    }

    public static boolean isPlayerCharacterLoaded(CharacterId id) {
        if (instance.getCharacter(id) == null) {
            return false;
        }
        return instance.getCharacter(id).isCurrent();
    }

    public static boolean isPlayerCharacterLoaded(Player player) {
        return instance.isCharacterLoaded(player);
    }

    public static PlayerCharacter getPlayerCharacter(Player player) {
        return instance.getCurrentCharacter(player);
    }

    @Override
    public PlayerCharacter getCurrentCharacter(Player player) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null.");

        PlayerCharacters characters = PlayerCharacterData.getPlayerCharacters(player.getUniqueId());
        if (characters == null) throw new NullPointerException(player.getDisplayName() + " has no character data.");

        return characters.getCurrentCharacter();
    }

    @Override
    public boolean isCharacterLoaded(Player player) {
        if (player != null) {
            PlayerCharacters characters = PlayerCharacterData.getPlayerCharacters(player.getUniqueId());
            if (characters != null) {
                return characters.isCharacterLoaded();
            }
        }
        return false;
    }

    @Override
    public PlayerCharacter getCharacter(CharacterId uniqueId) {
        if (uniqueId != null) {
            PlayerCharacters characters = PlayerCharacterData.getPlayerCharacters(uniqueId.getPlayerId());
            if (characters != null) {
                return characters.getForId(uniqueId);
            }
        }
        return null;
    }

    @Override
    public PlayerCharacters getCharacters(Player player) {
        if (player == null) {
            return null;
        }
        return PlayerCharacterData.getPlayerCharacters(player.getUniqueId());
    }

    public boolean isInCombat(Player p) {
        return combatStatusTracker.isInCombat(p);
    }

    /**
     * Gets the skill-effect manager, which coordinates the different kinds of timed effects that
     * skills/spells can apply.
     * @return The skill-effect manager.
     * @see SkillEffects
     */
    public SkillEffects getSkillEffectManager() {
        return skillEffects;
    }

    /**
     * Gets the main Characters config.
     * @return The Characters main config.
     */
    public CharactersConfig getCharacterConfig() {
        return config;
    }

    /**
     * Gets Characters' current user-interface manager, which handles displaying info to player and
     * letting them select options.
     * @return The Characters UI manager.
     */
    public CharactersUiManager getUiManager() {
        return uiManager;
    }

    /**
     * Sets Characters' user-interface manager, which provides and interprets user interfaces for
     * various Characters functions.
     * @param uiManager The new UI manager.
     */
    public void setUiManager(CharactersUiManager uiManager) {
        if (uiManager != null) {
            this.uiManager = uiManager;
        }
    }

    public PlayerLoader getPlayerLoader() {
        return loader;
    }

    /**
     * A placeholder user-interface manager that has no functionality.
     */
    private class NoUiManager implements CharactersUiManager {
        @Override
        public ProgressView getProgressView(Player player) {
            return null;
        }

        @Override
        public void forceCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener) {
        }

        @Override
        public void openCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener) {
        }

        @Override
        public void startCharacterCreation(Player player, int number, CharacterCreationListener listener) {
        }

        @Override
        public void openCharacterCreation(Player player) {
        }

        @Override
        public AbilityStatChangeListener getAbilityStatInterface(PlayerCharacter playerCharacter) {
            return null;
        }

        @Override
        public SkillEffectListener getCharacterEffectInterface(PlayerCharacter playerCharacter) {
            return null;
        }
    }
}
