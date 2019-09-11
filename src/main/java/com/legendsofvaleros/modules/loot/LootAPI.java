package com.legendsofvaleros.modules.loot;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.loot.api.ILootTable;

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

        InterfaceTypeAdapter.register(ILootTable.class,
                obj -> obj.getId(),
                id -> Promise.make(tables.get(id)));
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
        });
    }
}
