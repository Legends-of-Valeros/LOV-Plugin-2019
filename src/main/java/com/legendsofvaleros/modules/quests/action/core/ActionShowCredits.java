package com.legendsofvaleros.modules.quests.action.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutGameStateChange;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionShowCredits extends AbstractQuestAction {
    private static class PacketListener extends PacketAdapter {
        private final Map<UUID, Next> waiting = new HashMap<>();

        public PacketListener() {
            super(LegendsOfValeros.getInstance(), PacketType.Play.Server.RESPAWN);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (waiting.containsKey(event.getPlayer().getUniqueId())) {
                Next next = waiting.remove(event.getPlayer().getUniqueId());
                next.go();
                event.setCancelled(true);
            }
        }
    }

    private static final PacketListener listener = new PacketListener();

    static {
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }

    @Override
    public void play(PlayerCharacter pc, Next next) {
        EntityPlayer nms = ((CraftPlayer) pc.getPlayer()).getHandle();
        nms.viewingCredits = true;
        nms.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 1F));

        listener.waiting.put(pc.getPlayerId(), next);
    }
}