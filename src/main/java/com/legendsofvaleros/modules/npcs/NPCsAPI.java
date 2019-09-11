package com.legendsofvaleros.modules.npcs;

import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.PromiseCache;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.npcs.core.LOVNPC;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCsAPI extends ListenerModule {
    public interface RPC {
        Promise<Skin> getSkin(String id);

        Promise<LOVNPC> getNPC(String id);

        Promise<String> convertNPCSlugToID(String id);
    }

    protected RPC rpc;

    private PromiseCache<String, Skin> skins;
    private PromiseCache<String, LOVNPC> npcs;

    NPCRegistry registry;
    HashMap<String, Class<? extends LOVTrait>> traitTypes = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        this.skins = new PromiseCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(entry -> {
                    getLogger().warning("Skin '" + entry.getKey() + "' removed from the cache: " + entry.getCause());
                })
                .build(), id -> rpc.getSkin(id));

        InterfaceTypeAdapter.register(ISkin.class,
                obj -> obj.getId(),
                id -> skins.get(id).next(v -> Promise.make(v.orElse(null))));

        this.npcs = new PromiseCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(entry -> {
                    getLogger().warning("NPC '" + entry.getKey() + "' removed from the cache: " + entry.getCause());
                })
                .build(), id -> rpc.getNPC(id));

        InterfaceTypeAdapter.register(INPC.class,
                obj -> obj.getId(),
                id -> npcs.get(id).next(v -> Promise.make(v.orElse(null))));

        this.registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraitLOV.class).withName(TraitLOV.TRAIT_NAME));

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(LOVTrait[].class, (JsonDeserializer<LOVTrait[]>) (json, typeOfT, context) -> {
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
                })
                .registerTypeAdapter(LOVTrait[].class, (JsonSerializer<LOVTrait[]>) (val, typeOfT, context) -> {
                    JsonObject obj = new JsonObject();
                    for (LOVTrait trait : val)
                        obj.add(trait.id, context.serialize(trait));
                    return obj;
                });
    }

    public void registerTrait(String id, Class<? extends LOVTrait> trait) {
        traitTypes.put(id, trait);
    }

    public NPC createNPC(EntityType type, String s) {
        return registry.createNPC(type, s);
    }

    public LOVNPC getNPC(String id) {
        return npcs.getAndWait(id).orElse(null);
    }

    public String getNPCIDFromSlug(String slug) {
        try {
            return rpc.convertNPCSlugToID(slug).get();
        } catch (Throwable throwable) {
            return null;
        }
    }
}