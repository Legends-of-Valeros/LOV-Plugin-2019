package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;

import java.util.HashMap;
import java.util.Map;

public class LootAPI extends Module {
    public interface RPC {
        Promise<LootTable[]> findLootTables();
    }

    private RPC rpc;

    private Map<String, LootTable> tables = new HashMap<>();
    public LootTable getTable(String id) { return tables.get(id); }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(this, RPC.class);
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

    public Promise<LootTable[]> loadAll() {
        return rpc.findLootTables().onSuccess(val -> {
            tables.clear();

            for(LootTable table : val)
                tables.put(table.id, table);

            LootController.getInstance().getLogger().info("Loaded " + tables.size() + " loot tables.");
        }).onFailure(Throwable::printStackTrace);
    }
}
