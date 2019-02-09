package com.legendsofvaleros.modules.npcs.core;

import com.codingforcookies.doris.sql.TableManager;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.npcs.NPCsController;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

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

    private static TableManager manager;

    private static Map<String, Skin> skins = new HashMap<>();
    public static Skin getSkin(String id) { return skins.get(id); }

    public static void onEnable() {
        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SKIN_TABLE);

        manager.primary(SKIN_ID, "VARCHAR(64)")
                .column(SKIN_GROUP, "VARCHAR(64)")
                .column(SKIN_NAME, "VARCHAR(64)")
                .column(SKIN_UUID, "VARCHAR(36)")
                .column(SKIN_USERNAME, "VARCHAR(32)")
                .column(SKIN_SIGNATURE, "TEXT")
                .column(SKIN_DATA, "TEXT").create();

        manager.query().all().callback((statement, count) -> {
            ResultSet result = statement.getResultSet();

            while(result.next()) {
                Skin s = new Skin();
                s.uuid = result.getString(SKIN_UUID);
                s.username = result.getString(SKIN_USERNAME);
                s.signature = result.getString(SKIN_SIGNATURE);
                s.data = result.getString(SKIN_DATA);

                skins.put(result.getString(SKIN_ID), s);
            }
        }).onFinished(() -> {
            NPCsController.getInstance().getLogger().info("Loaded " + skins.size() + " skins.");
        }).execute(true);
    }

    public static class Skin {
        public String uuid;
        public String username;
        public String signature;
        public String data;
    }
}