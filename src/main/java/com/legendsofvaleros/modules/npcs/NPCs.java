package com.legendsofvaleros.modules.npcs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;

public class NPCs extends ListenerModule {
    private static NPCs instance;
    public static NPCs getInstance() {
        return instance;
    }

    private static NPCManager manager;
    public static NPCManager manager() {
        return manager;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new NPCCommands());

        manager = new NPCManager();
    }

    public static void registerTrait(String id, Class<? extends LOVTrait> trait) {
        manager.traitTypes.put(id, trait);
    }

    public static boolean isNPC(String id) {
        return manager.npcs.containsKey(id);
    }

    public static NPCData getNPC(String id) {
        return manager.npcs.get(id);
    }

    public static boolean isNPC(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    public static boolean isStaticNPC(LivingEntity entity) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        return npc != null && npc.getOwningRegistry() == CitizensAPI.getNPCRegistry();
    }
}