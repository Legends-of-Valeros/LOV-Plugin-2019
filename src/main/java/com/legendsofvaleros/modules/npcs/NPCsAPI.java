package com.legendsofvaleros.modules.npcs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.loot.api.ILootTable;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.core.Skin;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<NPCData>> findNPCs();

        Promise<List<Skin>> findSkins();

        Promise<Boolean> saveNPC(NPCData npc);
    }

    protected RPC rpc;
    protected HashMap<String, NPCData> npcs = new HashMap<>();
    NPCRegistry registry;
    HashMap<String, Class<? extends LOVTrait>> traitTypes = new HashMap<>();
    Map<String, Skin> skins = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        this.registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraitLOV.class).withName(TraitLOV.TRAIT_NAME));

        APIController.getInstance().getGsonBuilder().registerTypeAdapter(LOVTrait[].class, (JsonDeserializer<LOVTrait[]>) (json, typeOfT, context) -> {
            JsonObject obj = json.getAsJsonObject();
            List<LOVTrait> traits = new ArrayList<>();
            for (Map.Entry<String, JsonElement> elem : obj.entrySet()) {
                if (!traitTypes.containsKey(elem.getKey()))
                    MessageUtil.sendException(this, "Trait with that ID is not registered. Offender: " + elem.getKey());

                try {
                    LOVTrait trait = context.deserialize(elem.getValue(), traitTypes.get(elem.getKey()));
                    trait.id = elem.getKey();
                    traits.add(trait);
                } catch (Exception e) {
                    MessageUtil.sendException(this, "Failed to load trait. Offender: " + elem.getKey() + " (" + elem.getValue().toString() + ")");
                }
            }
            return traits.toArray(new LOVTrait[0]);
        });

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(LOVTrait[].class, (JsonSerializer<LOVTrait[]>) (val, typeOfT, context) -> {
                    JsonObject obj = new JsonObject();
                    for (LOVTrait trait : val)
                        obj.add(trait.id, context.serialize(trait));
                    return obj;
                })
                .registerTypeAdapter(ISkin.class, (JsonDeserializer<ISkin>) (json, typeOfT, context) -> {
                    // If we reference the interface, then the type should be a string, and we return the stored object.
                    // Note: it must be loaded already, else this returns null.
                    return skins.get(json.getAsString());
                });
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise loadAll() {
        return rpc.findNPCs()
                .onSuccess(val -> {
                    npcs.clear();

                    val.orElse(ImmutableList.of()).forEach(npc -> {
                        npcs.put(npc.getId(), npc);
                    });

                    LootController.getInstance().getLogger().info("Loaded " + npcs.size() + " NPCs.");
                })
                .onFailure(Throwable::printStackTrace)
                .next(rpc::findSkins)
                .onSuccess(val -> {
                    skins.clear();

                    val.orElse(ImmutableList.of()).forEach(skin -> {
                        skins.put(skin.getId(), skin);
                    });

                    LootController.getInstance().getLogger().info("Loaded " + skins.size() + " skins.");
                })
                .onFailure(Throwable::printStackTrace);
    }

    public void saveNPC(TraitLOV traitLOV) {
        getScheduler().executeInMyCircle(() -> {
            rpc.saveNPC(traitLOV.getNpcData()).onSuccess(() -> {
                MessageUtil.sendInfo(Bukkit.getConsoleSender(), "Successfully saved npc " + traitLOV.npcId + " at " + traitLOV.getNPC().getStoredLocation());
            }).onFailure(Throwable::printStackTrace);
        });
    }

}
