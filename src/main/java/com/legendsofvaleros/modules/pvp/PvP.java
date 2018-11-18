package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.Module;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.Currency;
import com.legendsofvaleros.modules.pvp.command.CommandDebug;
import com.legendsofvaleros.modules.pvp.duel.listener.DuelHandler;
import com.legendsofvaleros.modules.pvp.listener.DamageHandler;
import com.legendsofvaleros.modules.pvp.listener.MiscHandler;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PvP extends Module {

    public static final float DAMAGE_MULTIPLIER = 0.6f;
    private static PvP instance;

    private PvPToggles toggles;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
        toggles = new PvPToggles();

        JavaPlugin plugin = LegendsOfValeros.getInstance();

        // REGISTER EVENTS
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DamageHandler(), plugin);
        pm.registerEvents(new MiscHandler(), plugin);
        pm.registerEvents(new DuelHandler(), plugin);

        plugin.getCommand("pvpdebug").setExecutor(new CommandDebug());

        Bank.registerCurrency("honor", new Currency() {
            @Override
            public String getDisplay(long amount) {
                return amount == 0 ? null : "✝ " + amount;
            }

            @Override
            public String getName() {
                return "Honor";
            }
        });
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    /**
     * Gets the {@link PvPToggles} instance.
     * @return The {@link PvPToggles} instance.
     */
    public PvPToggles getToggles() {
        return toggles;
    }

    public static PvP getInstance() {
        return instance;
    }

}
