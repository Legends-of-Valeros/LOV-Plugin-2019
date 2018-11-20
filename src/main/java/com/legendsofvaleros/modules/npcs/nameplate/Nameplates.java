package com.legendsofvaleros.modules.npcs.nameplate;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.legendsofvaleros.LegendsOfValeros;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Nameplates {
    public static final String BASE = "name";

    private static ConcurrentHashMap<UUID, Nameplates> bound = new ConcurrentHashMap<>();

    public static Nameplates get(NPC entity) {
        return get(entity.getEntity());
    }

    public static Nameplates get(Entity entity) {
        if (!bound.containsKey(entity.getUniqueId()))
            bound.put(entity.getUniqueId(), new Nameplates(entity));
        return bound.get(entity.getUniqueId());
    }

    private final BukkitRunnable runnable;

    private boolean active = true;

    private final Entity entity;
    public final Map<String, Hologram> holograms = new LinkedHashMap<>();

    public Hologram add(String id) {
        Hologram holo = HologramsAPI.createHologram(LegendsOfValeros.getInstance(), entity.getLocation().add(0, entity.getHeight() + .25D, 0));
        holograms.put(id, holo);
        return holo;
    }

    public Hologram get(String id) {
        return holograms.get(id);
    }

    public Hologram getOrAdd(String id) {
        if (!holograms.containsKey(id)) add(id);
        return get(id);
    }

    public Nameplates(NPC npc) {
        this(npc.getEntity());
    }

    public Nameplates(Entity entity) {
        this.entity = entity;

        add(BASE);

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    remove();
                    cancel();
                    return;
                }

                double y = entity.getHeight() + .25D;
                for (Hologram holo : holograms.values()) {
                    y += holo.getHeight();

                    holo.teleport(entity.getLocation().add(0, y, 0));
                }
            }
        };
        runnable.runTaskTimer(LegendsOfValeros.getInstance(), 1L, 2L);
    }

    public boolean isActive() {
        return active && !entity.isDead();
    }

    public void remove() {
        active = false;

        for (Hologram holo : holograms.values()) {
            if (!holo.isDeleted())
                holo.delete();
        }

        holograms.clear();

        bound.remove(entity.getUniqueId());
    }
}