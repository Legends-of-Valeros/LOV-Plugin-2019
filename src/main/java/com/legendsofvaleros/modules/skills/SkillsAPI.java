package com.legendsofvaleros.modules.skills;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class SkillsAPI extends ModuleListener {
    public interface RPC {
        Promise<Map<Integer, String>> getSkillBar(CharacterId characterId);
        Promise<Boolean> saveSkillBar(CharacterId characterId, Map<Integer, String> map);
        Promise<Boolean> deleteSkillBar(CharacterId characterId);
    }

    private RPC rpc;

    private Table<CharacterId, Integer, String> skills = HashBasedTable.create();
    public String getSkillBarSlot(PlayerCharacter pc, int slot) {
        return skills.get(pc.getUniqueCharacterId(), slot);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
    }

    private Promise<Map<Integer, String>> onLogin(PlayerCharacter pc) {
        return rpc.getSkillBar(pc.getUniqueCharacterId()).onSuccess(map ->
                skills.row(pc.getUniqueCharacterId()).putAll(map.orElse(ImmutableMap.of())));
    }

    private Promise<Boolean> onLogout(PlayerCharacter pc) {
        return rpc.saveSkillBar(pc.getUniqueCharacterId(), skills.row(pc.getUniqueCharacterId())).onSuccess(map -> {
            skills.row(pc.getUniqueCharacterId()).clear();
        });
    }

    private Promise<Boolean> onDelete(PlayerCharacter pc) {
        return rpc.deleteSkillBar(pc.getUniqueCharacterId()).on(() -> {
            skills.row(pc.getUniqueCharacterId()).clear();
        });
    }

    public void updateSkillBar(PlayerCharacter pc, int slot, String skillId) {
        pc.getPlayer().sendMessage(skillId);

        if (skillId == null) {
            skills.remove(pc.getUniqueCharacterId(), slot);
            return;
        }

        skills.put(pc.getUniqueCharacterId(), slot, skillId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerCharacterStartLoadingEvent e) {
            PhaseLock lock = e.getLock("Skillbar");

            onLogin(e.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent e) {
            PhaseLock lock = e.getLock("Skillbar");

            onLogout(e.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent e) {
            onDelete(e.getPlayerCharacter());
        }
    }
}
