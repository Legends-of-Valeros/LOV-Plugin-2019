package com.legendsofvaleros.modules.npcs;

import com.codingforcookies.doris.sql.TableManager;
import com.google.gson.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class NPCManager implements Listener {
    private static final String NPC_TABLE = "npcs";
    private static final String NPC_ID = "npc_id";
    private static final String NPC_GROUP = "npc_group";
    private static final String NPC_NAME = "npc_name";
    private static final String NPC_WORLD = "npc_world";
    private static final String NPC_POSITION = "npc_position";
    private static final String NPC_SKIN = "npc_skin";
    private static final String NPC_TRAITS = "npc_traits";

    private final Gson gson;

    private final TableManager managerNPCs;

    public NPCRegistry registry;

    protected HashMap<String, NPCData> npcs = new HashMap<>();

    protected HashMap<String, Class<? extends LOVTrait>> traitTypes = new HashMap<>();

    public Set<String> getTraitIDs() {
        return traitTypes.keySet();
    }

    public NPCManager() {
        new Skins();

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraitLOV.class).withName(TraitLOV.TRAIT_NAME));

        NPCs.getInstance().registerEvents(this);

        gson = new GsonBuilder().registerTypeAdapter(LOVTrait[].class, new JsonDeserializer<LOVTrait[]>() {
            @Override
            public LOVTrait[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                List<LOVTrait> traits = new ArrayList<>();
                for (Entry<String, JsonElement> elem : obj.entrySet()) {
                    if (!traitTypes.containsKey(elem.getKey()))
                        throw new JsonParseException("Trait with that ID is not registered. Offender: " + elem.getKey());

                    try {
                        traits.add(gson.fromJson(elem.getValue(), traitTypes.get(elem.getKey())));
                    } catch (Exception e) {
                        MessageUtil.sendException(NPCs.getInstance(), null, new Exception("Failed to load trait. Offender: " + elem.getKey() + " (" + elem.getValue().toString() + ")"), true);
                        throw e;
                    }
                }
                return traits.toArray(new LOVTrait[0]);
            }
        }).create();

        managerNPCs = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), NPC_TABLE);

        managerNPCs.primary(NPC_ID, "VARCHAR(32)")
                .column(NPC_GROUP, "VARCHAR(64)")
                .column(NPC_NAME, "VARCHAR(32)")
                .column(NPC_WORLD, "VARCHAR(64)")
                .column(NPC_POSITION, "VARCHAR(64)")
                .column(NPC_SKIN, "VARCHAR(32)")
                .column(NPC_TRAITS, "TEXT").create();

        registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

        // Tasks are run after the server is successfully loaded.
        // This gives plugins time to register traits before the
        // NPCs are actually loaded.
        Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), this::reload, 0);
    }

    public void reload() {
        npcs.clear();

        managerNPCs.query()
                .all()
                .callback((result) -> {
                    while (result != null && result.next()) {
                        try {
                            NPCData data = new NPCData();
                            data.npcId = result.getString(NPC_ID);
                            data.name = result.getString(NPC_NAME);
                            data.skin = result.getString(NPC_SKIN);
                            data.traits = gson.fromJson(result.getString(NPC_TRAITS), LOVTrait[].class);

                            String[] locS = result.getString(NPC_POSITION).split(",");
                            data.loc = new Location(Bukkit.getWorld(result.getString(NPC_WORLD)), Double.parseDouble(locS[0]), Double.parseDouble(locS[1]), Double.parseDouble(locS[2]));

                            npcs.put(result.getString(NPC_ID), data);
                        } catch (Exception e) {
                            MessageUtil.sendError(Bukkit.getConsoleSender(), "Error while loading NPC: " + result.getString(NPC_ID));
                            MessageUtil.sendException(NPCs.getInstance(), null, e, true);
                        }
                    }
                })
                .execute(false);
    }

    public void updateNPC(TraitLOV lov, NPC npc) {
        managerNPCs.query()
                .insert()
                .values(NPC_ID, lov.npcId,
                        NPC_WORLD, npc.getEntity().getLocation().getWorld().getName(),
                        NPC_POSITION, npc.getEntity().getLocation().getX() + "," + npc.getEntity().getLocation().getY() + "," + npc.getEntity().getLocation().getZ())
                .onDuplicateUpdate(NPC_WORLD, NPC_POSITION)
                .build()
                .execute(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeftClick(NPCLeftClickEvent event) {
        if (!event.getNPC().hasTrait(TraitLOV.class)) return;
        event.getNPC().getTrait(TraitLOV.class).onLeftClick(event.getClicker());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().hasTrait(TraitLOV.class)) return;
        event.getNPC().getTrait(TraitLOV.class).onRightClick(event.getClicker());
    }
}