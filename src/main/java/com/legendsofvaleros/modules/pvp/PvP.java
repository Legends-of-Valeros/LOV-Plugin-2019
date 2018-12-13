package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.Currency;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.SkillTargetEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.dueling.Dueling;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.parties.Parties;
import com.legendsofvaleros.modules.pvp.traits.TraitHonorTrader;
import com.legendsofvaleros.modules.zones.Zones;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(Bank.class)
public class PvP extends ModuleListener {
    public static String HONOR_ID = "honor";
    public static Currency HONOR = new Currency() {
        @Override public String getName() { return "Honor"; }
        @Override
        public String getDisplay(long amount) {
            return (amount == 0 ? null : ChatColor.BOLD + "" + ChatColor.BLUE + "✝ " + amount);
        }
    };

    public static final float DAMAGE_MULTIPLIER = 0.6f;

    private static PvP instance;
    public static PvP getInstance() { return instance; }

    private boolean enabled;
    private int honorReward;
    private int honorCooldown;
    private int honorMaxLevelDifference;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        this.enabled = getConfig().getBoolean("world-pvp", false);

        ConfigurationSection honor = getConfig().getConfigurationSection("honor");
        this.honorReward = honor.getInt("reward", 25);
        this.honorCooldown = honor.getInt("cooldown", 3 * 60);
        this.honorMaxLevelDifference = honor.getInt("max-level-difference", 5);

        Bank.registerCurrency(HONOR_ID, HONOR);

        NPCs.registerTrait("honor-trader", TraitHonorTrader.class);
    }

    private boolean isPvPAllowed(boolean allow, Player p1, Player p2) {
        if(this.enabled) {
            // If PvP is disabled in the zone
            if (Modules.isLoaded(Zones.class)) {
                if (!Zones.manager().getZone(p1).pvp
                        || !Zones.manager().getZone(p2).pvp) {
                    allow = false;
                }
            }

            if (Modules.isLoaded(Parties.class)) {
                // Disable PvP within parties
            }
        }else
            allow = false;

        if(Modules.isLoaded(Dueling.class)) {
            // If they're in a duel with each other, enable pvp.
            if(Dueling.getInstance().getDuel(p1, p2) != null)
                allow = true;

                // If either player is in a duel, cancel damage.
            else if(Dueling.getInstance().getDuel(p1) != null
                    || Dueling.getInstance().getDuel(p2) != null)
                allow = false;
        }

        return allow;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        if (!event.getAttacker().isPlayer() || !event.getDamaged().isPlayer()) return;

        /*if(!attackerToggle.isEnabled() || !targetToggle.isEnabled() || attackerToggle.getPriority() != targetToggle.getPriority()) {
            event.setCancelled(true);
            return;
        }*/

        Player p1 = (Player)event.getAttacker().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p1)) { event.setCancelled(true); return; }

        Player p2 = (Player)event.getDamaged().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p2)) { event.setCancelled(true); return; }

        event.setCancelled(isPvPAllowed(event.isCancelled(), p1, p2));

        if(!event.isCancelled())
            event.newDamageModifierBuilder("PvP")
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(PvP.DAMAGE_MULTIPLIER)
                .build();
    }

    @EventHandler
    public void onEntityTargetted(SkillTargetEvent event) {
        // Always allow "good" spells.
        if(Boolean.TRUE.equals(event.isGood())) {
            event.setCancelled(true);
            return;
        }

        if (!event.getUser().isPlayer() || !event.getTarget().isPlayer()) return;

        Player p1 = (Player)event.getUser().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p1)) { event.setCancelled(true); return; }

        Player p2 = (Player)event.getTarget().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p2)) { event.setCancelled(true); return; }

        event.setCancelled(isPvPAllowed(event.isCancelled(), p1, p2));
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
                Bank.getBank(killerPC).addCurrency(HONOR_ID, honorReward);
            }
        }
    }
}