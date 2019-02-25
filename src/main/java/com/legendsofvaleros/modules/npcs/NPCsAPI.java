package com.legendsofvaleros.modules.npcs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.core.Skin;
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
        Promise<List<NPCData>> findNPCs();
        Promise<List<Skin>> findSkins();

        Promise<Boolean> saveNPC(NPCData npc);
    }

    private RPC rpc;

    private NPCRegistry registry;

    private HashMap<String, Class<? extends LOVTrait>> traitTypes = new HashMap<>();

    private HashMap<String, NPCData> npcs = new HashMap<>();

    private Map<String, Skin> skins = new HashMap<>();
    public Skin getSkin(String id) { return skins.get(id); }

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

        APIController.getInstance().getGsonBuilder().registerTypeAdapter(LOVTrait[].class, (JsonSerializer<LOVTrait[]>) (val, typeOfT, context) -> {
            JsonObject obj = new JsonObject();
            for (LOVTrait trait : val)
                obj.add(trait.id, context.serialize(trait));
            return obj;
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
        return rpc.findNPCs().onSuccess(val -> {
            npcs.clear();

            val.orElse(ImmutableList.of()).stream().forEach(npc ->
                    npcs.put(npc.id, npc));

            LootController.getInstance().getLogger().info("Loaded " + npcs.size() + " NPCs.");
        }).onFailure(Throwable::printStackTrace)
        .next(rpc::findSkins)
        .onSuccess(val -> {
            skins.clear();

            val.orElse(ImmutableList.of()).stream().forEach(skin ->
                    skins.put(skin.id, skin));

            LootController.getInstance().getLogger().info("Loaded " + skins.size() + " skins.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Promise<Boolean> saveNPC(TraitLOV traitLOV) {
        return rpc.saveNPC(traitLOV.npcData);
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

    public NPC getNPC(LivingEntity entity) { return CitizensAPI.getNPCRegistry().getNPC(entity); }

    public boolean isNPC(LivingEntity entity) { return CitizensAPI.getNPCRegistry().isNPC(entity); }

    public boolean isStaticNPC(LivingEntity entity) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        return npc != null && npc.getOwningRegistry() == CitizensAPI.getNPCRegistry();
    }
}
