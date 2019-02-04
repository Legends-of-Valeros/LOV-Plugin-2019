package com.legendsofvaleros.modules.loot;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.gear.GearController;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@DependsOn(GearController.class)
public class LootManager extends Module {
    private static final String LOOT_TABLE = "loot";
    private static final String LOOT_ID = "loot_id";
    private static final String LOOT_GROUP = "loot_group";
    private static final String LOOT_NAME = "loot_name";
    private static final String LOOT_CONTENT = "loot_content";

    private Gson gson;
    private TableManager manager;
    private final Map<String, LootTable> tables = new HashMap<>();
    private static LootManager instance;

    public static LootManager getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
        gson = new Gson();

        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), LOOT_TABLE);
        manager.primary(LOOT_ID, "VARCHAR(64)")
                .column(LOOT_GROUP, "VARCHAR(64)")
                .column(LOOT_NAME, "VARCHAR(64)")
                .column(LOOT_CONTENT, "TEXT").create();
    }

    public ListenableFuture<LootTable> getTable(String loot_id) {
        final SettableFuture<LootTable> ret = SettableFuture.create();

        if (tables.containsKey(loot_id)) {
            ret.set(tables.get(loot_id));
        } else {
            manager.query()
                    .select()
                    .where(LOOT_ID, loot_id)
                    .limit(1)
                    .build()
                    .callback((statement, count) -> {
                        ResultSet result = statement.getResultSet();

                        if (!result.next()) {
                            ret.set(null);
                            return;
                        }

                        LootTable loot = gson.fromJson(result.getString(LOOT_CONTENT), LootTable.class);
                        tables.put(result.getString(LOOT_ID), loot);
                        ret.set(loot);
                    }).execute(true);
        }

        return ret;
    }
}