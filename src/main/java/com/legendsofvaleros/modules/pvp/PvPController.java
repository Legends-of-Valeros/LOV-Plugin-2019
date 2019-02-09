package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Currency;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.pvp.integration.BankIntegration;
import com.legendsofvaleros.modules.pvp.listener.PvPListener;
import org.bukkit.ChatColor;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(BankController.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@ModuleInfo(name = "PvP", info = "")
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