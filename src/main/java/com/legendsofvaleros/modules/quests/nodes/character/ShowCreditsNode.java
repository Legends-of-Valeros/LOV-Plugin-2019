package com.legendsofvaleros.modules.quests.nodes.character;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.PacketPlayOutGameStateChange;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShowCreditsNode extends AbstractQuestNode<Boolean> {
    private static class PacketListener extends PacketAdapter {
        private final Map<UUID, Runnable> waiting = new HashMap<>();

        public PacketListener() {
            super(LegendsOfValeros.getInstance(), PacketType.Play.Server.RESPAWN);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (waiting.containsKey(event.getPlayer().getUniqueId())) {
                waiting.remove(event.getPlayer().getUniqueId()).run();
                event.setCancelled(true);
            }
        }
    }

    private static final PacketListener listener = new PacketListener();

    static {
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Execute")
    public IInportTrigger<Boolean> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        EntityPlayer nms = ((CraftPlayer)instance.getPlayer()).getHandle();
        nms.viewingCredits = true;
        nms.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 1F));

        listener.waiting.put(instance.getPlayerCharacter().getPlayerId(), () -> {
            onCompleted.run(instance);
        });

        onCompleted.run(instance);
    });
    
    public ShowCreditsNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @Override
    public void onActivated(IQuestInstance instance, Boolean data) {
        // If we aren't in the credits screen, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // If we have reactivated the quest, and the credits screen was applied, just end it, now.
        onCompleted.run(instance);

        instance.setNodeInstance(this, true);
    }

    @Override
    public void onDeactivated(IQuestInstance instance, Boolean data) {
        // Remove any players currently in the credits
        listener.waiting.remove(instance.getPlayer().getUniqueId());
    }
}