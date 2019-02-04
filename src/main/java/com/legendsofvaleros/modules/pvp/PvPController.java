package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Currency;
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
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.modules.pvp.integration.BankIntegration;
import com.legendsofvaleros.modules.pvp.listener.PvPListener;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(BankController.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
public class PvPController extends Module {
    public static final float DAMAGE_MULTIPLIER = 0.6f;

    public static String HONOR_ID = "honor";
    public static Currency HONOR = new Currency() {
        @Override public String getName() { return "Honor"; }
        @Override
        public String getDisplay(long amount) {
            return (amount == 0 ? null : ChatColor.BOLD + "" + ChatColor.BLUE + "✝ " + amount);
        }
    };

    private static PvPController instance;
    public static PvPController getInstance() { return instance; }

    private boolean enabled;
    public boolean isPvPEnabled() { return enabled; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        this.enabled = getConfig().getBoolean("world-pvp", false);

        registerEvents(new PvPListener());
    }
}