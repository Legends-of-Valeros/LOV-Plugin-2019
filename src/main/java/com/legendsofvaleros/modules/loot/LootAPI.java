package com.legendsofvaleros.modules.loot;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializer;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.loot.api.ILootTable;
import com.legendsofvaleros.modules.quests.api.IQuest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootAPI extends Module {
    public interface RPC {
        Promise<List<LootTable>> findLootTables();
    }

    private RPC rpc;

    private Map<String, LootTable> tables = new HashMap<>();

    public LootTable getTable(String id) {
        return tables.get(id);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(ILootTable.class, new TypeAdapter<ILootTable>() {
                    @Override
                    public void write(JsonWriter write, ILootTable loot) throws IOException {
                        write.value(loot.getId());
                    }

                    @Override
                    public ILootTable read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        return tables.get(read.nextString());
                    }
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

    public Promise<List<LootTable>> loadAll() {
        return rpc.findLootTables().onSuccess(val -> {
            tables.clear();

            val.orElse(ImmutableList.of()).forEach(table ->
                    tables.put(table.getId(), table));

            LootController.getInstance().getLogger().info("Loaded " + tables.size() + " loot tables.");
        }).onFailure(Throwable::printStackTrace);
    }
}
