package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.Currency;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
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
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
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
    public boolean isPvPEnabled() { return enabled; }

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        if(event.isCancelled()) return;

        if (!event.getAttacker().isPlayer() || !event.getDamaged().isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded((Player)event.getDamaged().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player)event.getAttacker().getLivingEntity())) return;

        // If PvP is disabled, cancel it. Duh.
        if(!this.enabled) { event.setCancelled(true); }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvp = new PvPCheckEvent((Player)event.getAttacker().getLivingEntity(), (Player)event.getDamaged().getLivingEntity(), null);
        Bukkit.getPluginManager().callEvent(pvp);

        if(pvp.isCancelled())
            event.setCancelled(true);
        else{
            // If the damage event is not cancelled, add the PvP modifier.
            event.newDamageModifierBuilder("PvP")
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(PvP.DAMAGE_MULTIPLIER)
                    .build();
        }

        /*if(!attackerToggle.isEnabled() || !targetToggle.isEnabled() || attackerToggle.getPriority() != targetToggle.getPriority()) {
            event.setCancelled(true);
            return;
        }*/
    }

    @EventHandler
    public void onEntityTargetted(SkillTargetEvent event) {
        // Ignore "good" spells. We only care about harmful attacks.
        if(event.getSkill().getType() != Skill.Type.HARMFUL)
            return;

        if (!event.getUser().isPlayer() || !event.getTarget().isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded((Player)event.getUser().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player)event.getTarget().getLivingEntity())) return;

        // If PvP is disabled, cancel it. Duh.
        if(!this.enabled) { event.setCancelled(true); }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvp = new PvPCheckEvent((Player)event.getUser().getLivingEntity(), (Player)event.getTarget().getLivingEntity(), event.getSkill());
        Bukkit.getPluginManager().callEvent(pvp);

        // PvP is disabled! Don't target the player!
        if(pvp.isCancelled())
            event.setCancelled(true);
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

                Bank.getBank(killerPC).addCurrency(HONOR_ID, honorReward);
            }
        }
    }
}