package com.legendsofvaleros.modules.gear;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.gear.commands.ItemCommands;
import com.legendsofvaleros.modules.gear.component.ComponentMap;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.PersistMap;
import com.legendsofvaleros.modules.gear.component.core.*;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.integration.BankIntegration;
import com.legendsofvaleros.modules.gear.integration.SkillsIntegration;
import com.legendsofvaleros.modules.gear.listener.InventoryListener;
import com.legendsofvaleros.modules.gear.listener.ItemListener;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.features.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;

import java.lang.reflect.Type;
import java.util.Map;

@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(Hotswitch.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@ModuleInfo(name = "Gear", info = "")
public class GearController extends GearAPI {
    private static GearController instance;

    public static GearController getInstance() {
        return instance;
    }

    public static Gear ERROR_ITEM;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ItemCommands());

        registerEvents(new ItemListener());
        registerEvents(new InventoryListener());

        GearRegistry.registerComponent("lore", LoreComponent.class);
        GearRegistry.registerComponent("bind", SoulbindComponent.class);

        GearRegistry.registerComponent("require", RequireComponent.class);
        GearRegistry.registerComponent("damage", GearPhysicalDamage.Component.class);
        GearRegistry.registerComponent("durability", GearDurability.Component.class);
        GearRegistry.registerComponent("usable", GearUsable.Component.class);
        GearRegistry.registerComponent("use_speed", GearUseSpeed.Component.class);

        GearRegistry.registerComponent("stats", GearStats.Component.class);

        APIController.getInstance().getGsonBuilder()
                // This is going to be replaced with a general computational value object, eventually (with the random gen system)
                .registerTypeAdapter(RangedValue.class, RangedValue.JSON)

                // Register the component map deserializer for gear
                .registerTypeAdapter(ComponentMap.class, (JsonDeserializer<ComponentMap>) (json, typeOfT, context) -> {
                    JsonObject obj = json.getAsJsonObject();
                    ComponentMap components = new ComponentMap();
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        try {
                            Class<? extends GearComponent<?>> comp = GearRegistry.getComponent(entry.getKey());
                            if (comp == null)
                                throw new RuntimeException("Unknown component on item: Offender: " + entry.getKey());
                            components.put(entry.getKey(), context.deserialize(entry.getValue(), comp));
                        } catch (Exception e) {
                            MessageUtil.sendException(GearController.getInstance(), new Exception(e + ". Offender: " + entry.getKey() + " " + entry.getValue().toString()));
                        }
                    }
                    return components;
                })

                // Register the persistent map deserializer for gear components
                .registerTypeAdapter(PersistMap.class, (JsonDeserializer<PersistMap>) (json, typeOfT, context) -> {
                    JsonObject obj = json.getAsJsonObject();
                    PersistMap persists = new PersistMap();
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        Type c = GearRegistry.getPersist(entry.getKey());
                        try {
                            persists.put(entry.getKey(), context.deserialize(entry.getValue(), c));
                        } catch (Exception e) {
                            getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
                            e.printStackTrace();
                        }
                    }
                    return persists;
                });
    }
}