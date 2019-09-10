package com.legendsofvaleros.modules.skills;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.skill.Skill;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.Map;

public class SkillsAPI extends ListenerModule {
    public interface RPC {
        Promise<Map<Integer, String>> getPlayerSkillBar(CharacterId characterId);

        Promise<Object> savePlayerSkillBar(CharacterId characterId, Map<Integer, String> map);

        Promise<Boolean> deletePlayerSkillBar(CharacterId characterId);
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

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(Skill.class, new TypeAdapter<Skill>() {
                    @Override
                    public void write(JsonWriter write, Skill skill) throws IOException {
                        write.value(skill != null ? skill.getId() : null);
                    }

                    @Override
                    public Skill read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        if(read.peek() == JsonToken.NULL) {
                            read.nextNull();
                            return null;
                        }

                        return Skill.getSkillById(read.nextString());
                    }
                });

        registerEvents(new PlayerListener());
    }

    private Promise<Map<Integer, String>> onLogin(PlayerCharacter pc) {
        return rpc.getPlayerSkillBar(pc.getUniqueCharacterId()).onSuccess(map ->
                skills.row(pc.getUniqueCharacterId()).putAll(map.orElse(ImmutableMap.of())));
    }

    private Promise onLogout(PlayerCharacter pc) {
        return rpc.savePlayerSkillBar(pc.getUniqueCharacterId(), skills.row(pc.getUniqueCharacterId())).onSuccess(map -> {
            skills.row(pc.getUniqueCharacterId()).clear();
        });
    }

    private Promise onDelete(PlayerCharacter pc) {
        return rpc.deletePlayerSkillBar(pc.getUniqueCharacterId()).on(() -> {
            skills.row(pc.getUniqueCharacterId()).clear();
        });
    }

    public void updateSkillBar(PlayerCharacter pc, int slot, String skillId) {
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
