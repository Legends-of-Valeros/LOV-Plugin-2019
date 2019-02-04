package com.legendsofvaleros.modules.pvp.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Currency;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class BankIntegration extends Integration {
    public static String HONOR_ID = "honor";
    public static Currency HONOR = new Currency() {
        @Override public String getName() { return "Honor"; }
        @Override
        public String getDisplay(long amount) {
            return (amount == 0 ? null : ChatColor.BOLD + "" + ChatColor.BLUE + "✝ " + amount);
        }
    };

    private int honorReward;
    private int honorCooldown;
    private int honorMaxLevelDifference;

    public BankIntegration() {
        ConfigurationSection honor = PvPController.getInstance().getConfig().getConfigurationSection("honor");
        this.honorReward = honor.getInt("reward", 25);
        this.honorCooldown = honor.getInt("cooldown", 3 * 60);
        this.honorMaxLevelDifference = honor.getInt("max-level-difference", 5);

        BankController.registerCurrency(HONOR_ID, HONOR);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPvPDeath(CombatEngineDeathEvent event) {
        CombatEntity killer = event.getKiller();
        CombatEntity target = event.getDied();

        if(killer == null || !killer.isPlayer() || target == null || !target.isPlayer()) return;
        if (!Characters.isPlayerCharacterLoaded(killer.getUniqueId())) return;
        if (!Characters.isPlayerCharacterLoaded(target.getUniqueId())) return;

        PlayerCharacter killerPC = Characters.getPlayerCharacter(killer.getUniqueId());
        PlayerCharacter targetPC = Characters.getPlayerCharacter(target.getUniqueId());

        if(Math.abs(killerPC.getExperience().getLevel() - targetPC.getExperience().getLevel()) <= honorMaxLevelDifference) {
            if(killerPC.getCooldowns().offerCooldown("honor:" + target.getUniqueId(), Cooldowns.CooldownType.CALENDAR_TIME, honorCooldown * 1000) != null) {
                MessageUtil.sendUpdate(killerPC.getPlayer(), "You received " + HONOR.getDisplay(honorReward));

                BankController.getBank(killerPC).addCurrency(HONOR_ID, honorReward);
            }
        }
    }
}
