package com.legendsofvaleros.modules.skills;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.ResultSet;

public class SkillBarManager implements Listener {
    private static final String SKILLBAR_TABLE = "player_skillbar";
    private static final String CHARACTER_ID = "character_id";
    private static final String SLOT_ID = "slot_id";
    private static final String SLOT_SKILL = "slot_skill";

    private final TableManager manager;

    private Table<CharacterId, Integer, String> skills = HashBasedTable.create();

    public String getSlot(PlayerCharacter pc, int slot) {
        return skills.get(pc.getUniqueCharacterId(), slot);
    }

    public SkillBarManager() {
        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SKILLBAR_TABLE);

        manager.primary(CHARACTER_ID, "VARCHAR(38)")
                .primary(SLOT_ID, "INT(5)")
                .column(SLOT_SKILL, "TEXT").create();

        SkillsController.getInstance().registerEvents(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerCharacterStartLoadingEvent e) {
        loadPlayer(e.getPlayerCharacter(), e.getLock("Skillbar"));
    }

    @EventHandler
    public void onPlayerLeave(PlayerCharacterLogoutEvent e) {
        skills.row(e.getPlayerCharacter().getUniqueCharacterId()).clear();
    }

    @EventHandler
    public void onPlayerRemoved(PlayerCharacterRemoveEvent e) {
        manager.query()
                .remove()
                .where(CHARACTER_ID, e.getPlayerCharacter().getUniqueCharacterId().toString())
                .build()
                .execute(true);
    }

    private void loadPlayer(final PlayerCharacter pc, final PhaseLock lock) {
        manager.query()
                .select()
                .where(CHARACTER_ID, pc.getUniqueCharacterId().toString())
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    while (result.next()) {
                        int slot = result.getInt(SLOT_ID);
                        String skillId = result.getString(SLOT_SKILL);

                        skills.put(pc.getUniqueCharacterId(), slot, skillId);
                    }

                    lock.release();
                })
                .execute(true);
    }

    public void updateSlot(final PlayerCharacter pc, int slot, String skillId) {
        if (skillId == null) {
            skills.remove(pc.getUniqueCharacterId(), slot);
            manager.query()
                    .remove()
                    .where(CHARACTER_ID, pc.getUniqueCharacterId().toString(),
                            SLOT_ID, slot)
                    .build()
                    .execute(true);
            return;
        }

        skills.put(pc.getUniqueCharacterId(), slot, skillId);

        manager.query()
                .insert()
                .values(CHARACTER_ID, pc.getUniqueCharacterId().toString(),
                        SLOT_ID, slot,
                        SLOT_SKILL, skillId)
                .onDuplicateUpdate(SLOT_SKILL)
                .build()
                .execute(true);
    }
}