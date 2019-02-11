package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;

import java.util.HashMap;
import java.util.Map;

public class LootAPI {
    @ModuleRPC("loottable")
    public interface RPC {
        Promise<LootTable[]> find();
    }

    private final RPC rpc;

    private Map<String, LootTable> tables = new HashMap<>();
    public LootTable getTable(String id) { return tables.get(id); }

    public LootAPI() {
        this.rpc = APIController.create(HearthstoneController.getInstance(), RPC.class);
    }

    public Promise<LootTable[]> loadAll() {
        return rpc.find().onSuccess((loots) -> {
            tables.clear();

            for(LootTable table : loots)
                tables.put(table.id, table);

            LootController.getInstance().getLogger().info("Loaded " + tables.size() + " loot tables.");
        }).onFailure(Throwable::printStackTrace);
    }
}
