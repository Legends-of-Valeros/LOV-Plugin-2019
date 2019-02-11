package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;

import java.util.HashMap;
import java.util.Map;

public class LootAPI {
    public interface RPC {
        Promise<LootTable[]> findLootTables();
    }

    private final RPC rpc;

    private Map<String, LootTable> tables = new HashMap<>();
    public LootTable getTable(String id) { return tables.get(id); }

    public LootAPI() {
        this.rpc = APIController.create(LootController.getInstance(), RPC.class);
    }

    public Promise<LootTable[]> loadAll() {
        return rpc.findLootTables().onSuccess(val -> {
            tables.clear();

            for(LootTable table : val)
                tables.put(table.id, table);

            LootController.getInstance().getLogger().info("Loaded " + tables.size() + " loot tables.");
        }).onFailure(Throwable::printStackTrace);
    }
}
