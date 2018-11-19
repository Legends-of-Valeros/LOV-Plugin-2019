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

public class PvP extends Module {

    public static final float DAMAGE_MULTIPLIER = 0.6f;
    private static PvP instance;

    private PvPToggles toggles;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
        toggles = new PvPToggles();

        // REGISTER EVENTS
        registerEvents(new DamageHandler());
        registerEvents(new MiscHandler());
        registerEvents(new DuelHandler());

        LegendsOfValeros.getInstance().getCommand("pvpdebug").setExecutor(new CommandDebug());

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
