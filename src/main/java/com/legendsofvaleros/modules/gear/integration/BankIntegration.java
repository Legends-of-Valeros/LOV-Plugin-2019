package com.legendsofvaleros.modules.gear.integration;


import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.bank.WorthComponent;
import org.bukkit.event.Listener;

public class BankIntegration extends Integration {
    public BankIntegration() {
        GearRegistry.registerComponent("worth", WorthComponent.class);
    }
}