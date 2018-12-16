package com.legendsofvaleros.modules.npcs;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;

import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

public class Skins {
    private static Skins instance;

    public static Skins inst() {
        return instance;
    }

    private static final String SKIN_TABLE = "skins";
    private static final String SKIN_ID = "skin_id";
    private static final String SKIN_GROUP = "skin_group";
    private static final String SKIN_NAME = "skin_name";
    private static final String SKIN_UUID = "skin_uuid";
    private static final String SKIN_USERNAME = "skin_username";
    private static final String SKIN_SIGNATURE = "skin_signature";
    private static final String SKIN_DATA = "skin_data";

    private final TableManager manager;

    public final Cache<String, Skin> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public Skins() {
        instance = this;

        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SKIN_TABLE);

        manager.primary(SKIN_ID, "VARCHAR(64)")
                .column(SKIN_GROUP, "VARCHAR(64)")
                .column(SKIN_NAME, "VARCHAR(64)")
                .column(SKIN_UUID, "VARCHAR(36)")
                .column(SKIN_USERNAME, "VARCHAR(32)")
                .column(SKIN_SIGNATURE, "TEXT")
                .column(SKIN_DATA, "TEXT").create();
    }

    public ListenableFuture<Skin> getSkin(String id) {
        return loadSkin(id);
    }

    private ListenableFuture<Skin> loadSkin(String id) {
        SettableFuture<Skin> ret = SettableFuture.create();

        Skin skin = cache.getIfPresent(id);
        if (skin != null) {
            ret.set(skin);
        } else {
            manager.query()
                    .select()
                    .where(SKIN_ID, id)
                    .limit(1)
                    .build()
                    .callback((statement, count) -> {
                        ResultSet result = statement.getResultSet();

                        if (!result.next()) {
                            ret.set(null);
                            return;
                        }

                        Skin s = new Skin();
                        s.uuid = result.getString(SKIN_UUID);
                        s.username = result.getString(SKIN_USERNAME);
                        s.signature = result.getString(SKIN_SIGNATURE);
                        s.data = result.getString(SKIN_DATA);

                        cache.put(result.getString(SKIN_ID), s);

                        ret.set(s);
                    })
                    .execute(true);
        }

        return ret;
    }

    public static class Skin {
        public String uuid;
        public String username;
        public String signature;
        public String data;
    }
}