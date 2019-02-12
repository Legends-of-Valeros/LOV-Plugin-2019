package com.legendsofvaleros.modules.npcs;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.core.Skin;
import com.legendsofvaleros.modules.npcs.core.Skins;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCsAPI extends ModuleListener {
    public interface RPC {
        Promise<NPCData[]> findNPCs();

        Promise<Boolean> saveNPC(NPCData npc);
    }

    private RPC rpc;

    private NPCRegistry registry;

    private HashMap<String, Class<? extends LOVTrait>> traitTypes = new HashMap<>();

    private HashMap<String, NPCData> npcs = new HashMap<>();

    private static Map<String, Skin> skins = new HashMap<>();
    public static Skin getSkin(String id) { return skins.get(id); }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        Skins.onEnable();

        this.registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraitLOV.class).withName(TraitLOV.TRAIT_NAME));

        APIController.getInstance().getGsonBuilder().registerTypeAdapter(LOVTrait[].class, (JsonDeserializer<LOVTrait[]>) (json, typeOfT, context) -> {
            JsonObject obj = json.getAsJsonObject();
            List<LOVTrait> traits = new ArrayList<>();
            for (Map.Entry<String, JsonElement> elem : obj.entrySet()) {
                if (!traitTypes.containsKey(elem.getKey()))
                    throw new JsonParseException("Trait with that ID is not registered. Offender: " + elem.getKey());

                try {
                    traits.add(context.deserialize(elem.getValue(), traitTypes.get(elem.getKey())));
                } catch (Exception e) {
                    MessageUtil.sendException(this, "Failed to load trait. Offender: " + elem.getKey() + " (" + elem.getValue().toString() + ")");
                    throw e;
                }
            }
            return traits.toArray(new LOVTrait[0]);
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

    public Promise<NPCData[]> loadAll() {
        return rpc.findNPCs().onSuccess(val -> {
            npcs.clear();

            for(NPCData npc : val)
                npcs.put(npc.npcId, npc);

            LootController.getInstance().getLogger().info("Loaded " + npcs.size() + " NPCs.");
        }).onFailure(Throwable::printStackTrace);
    }

    public void saveNPC(TraitLOV traitLOV) {
        rpc.saveNPC(traitLOV.npcData);
    }

    public void registerTrait(String id, Class<? extends LOVTrait> trait) {
        traitTypes.put(id, trait);
    }

    public NPC createNPC(EntityType type, String s) {
        return registry.createNPC(type, s);
    }

    public boolean isNPC(String id) {
        return npcs.containsKey(id);
    }

    public NPCData getNPC(String id) {
        return npcs.get(id);
    }

    public NPC getNPC(LivingEntity entity) { return registry.getNPC(entity); }

    public boolean isNPC(LivingEntity entity) { return registry.isNPC(entity); }

    public boolean isStaticNPC(LivingEntity entity) {
        NPC npc = registry.getNPC(entity);
        return npc != null && npc.getOwningRegistry() == registry;
    }
}
