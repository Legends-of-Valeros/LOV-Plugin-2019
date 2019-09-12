package com.legendsofvaleros.modules.npcs.trait;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.npcs.core.LOVNPC;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import java.util.ArrayList;
import java.util.List;

public class CitizensTraitLOV extends Trait implements CommandConfigurable {
    public static final String TRAIT_NAME = "lov";
    public static final List<CitizensTraitLOV> all = new ArrayList<>();
    private boolean updatedSkin = false;

    @Persist
    public String npcSlug;

    @Persist(value = "")
    public String npcId;

    private LOVNPC lovNPC;
    private LOVTrait[] traits = new LOVTrait[0];

    public Nameplates nameplates;
    public TextLine nameLine;

    public CitizensTraitLOV() {
        super(TRAIT_NAME);
    }

    @Override
    public void configure(CommandContext ctx) {
        CommandSender p = null;
        Location loc;
        try {
            Player foundP = null;
            loc = ctx.getSenderLocation();
            if (loc == null)
                p = Bukkit.getConsoleSender();
            else {
                List<Entity> entities = new ArrayList<>(loc.getWorld().getNearbyEntities(loc, 3, 3, 3));
                for (Entity e : entities)
                    if (e.getType() == EntityType.PLAYER) {
                        foundP = (Player) e;
                        break;
                    }
                p = foundP;
            }
        } catch (CommandException e) {
            MessageUtil.sendException(NPCsController.getInstance(), p, e);
            return;
        }

        String id = ctx.getString(1).toLowerCase();

        this.npcId = id;
        MessageUtil.sendUpdate(p, "Setting NPC LOV ID to '" + id + "'.");
    }

    @Override
    public void onAttach() {
        super.onAttach();

        all.add(this);
    }

    @Override
    public void onRemove() {
        all.remove(this);

        traits = new LOVTrait[0];

        super.onRemove();
    }

    @Override
    public void onSpawn() {
        super.onSpawn();

        nameplates = Nameplates.get(npc);
        nameLine = nameplates.getOrAdd(Nameplates.BASE).appendTextLine(npc.getName());

        npc.data().setPersistent(NPC.SHOULD_SAVE_METADATA, true);

        npc.data().setPersistent(NPC.COLLIDABLE_METADATA, true);
        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
        npc.getEntity().setCustomNameVisible(false);

        if (npc.getEntity().getType() == EntityType.PLAYER) {
            Snowball s = (Snowball) npc.getEntity().getWorld().spawnEntity(npc.getEntity().getLocation(), EntityType.SNOWBALL);
            s.setSilent(true);
            npc.getEntity().addPassenger(s);
        }

        if((npcId == null && npcSlug == null)) {
            if(lovNPC != null) {
                /*for (LOVTrait trait : traits) {
                    trait.npc_id = npcId;
                    trait.npc = npc;
                    trait.trait = this;
                    trait.onSpawn();
                }*/
            }

            return;
        }

        Promise promise;

        if (npcId == null) {
            promise = NPCsController.getInstance().getNPCIDFromSlug(npcSlug).onSuccess(v -> {
                this.npcId = v.get();
                this.npcSlug = null;
            });
        }else
            promise = Promise.make(true);

        promise.onSuccess(() -> {
            NPCsController.getInstance().getNPC(npcId).onSuccess(val -> {
                this.lovNPC = val.get();

                NPCsController.getInstance().getScheduler().sync(() -> {
                    if (this.lovNPC == null) {
                        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, true);
                        npc.getEntity().setCustomNameVisible(true);
                        MessageUtil.sendException(NPCsController.getInstance(), "No NPC with that ID exists in the cache. Offender: " + npcId + " on " + npc.getId());
                        return;
                    }

                    if (this.lovNPC.getName() != null) {
                        if (!npc.getName().equals(this.lovNPC.getName())) {
                            npc.setName(this.lovNPC.getName());
                        }
                    } else {
                        MessageUtil.sendException(NPCsController.getInstance(), "NPC has a null name. Offender: " + npcId + " on " + npc.getId());
                    }

                    nameLine.setText(lovNPC.getName());

                    if (this.lovNPC.getSkin() != null && !updatedSkin && npc.getEntity() instanceof SkinnableEntity) {
                        updatedSkin = true;

                        try {
                            ISkin skin = this.lovNPC.getSkin();
                            if(skin != null) {
                                ISkin.Texture texture = skin.getTexture();

                                if (texture != null) {
                                    npc.data().setPersistent("cached-skin-uuid", texture.getUUID());
                                    npc.data().setPersistent("cached-skin-uuid-name", texture.getUsername().toLowerCase());
                                    npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, texture.getUsername().toLowerCase());
                                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, texture.getSignature());
                                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, texture.getData());
                                    npc.data().setPersistent(NPC.PLAYER_SKIN_USE_LATEST, false);

                                    SkinnableEntity se = (SkinnableEntity) npc.getEntity();
                                    se.getSkinTracker().notifySkinChange(false);
                                }
                            }
                        } catch (Exception e) {
                            MessageUtil.sendException(NPCsController.getInstance(), null, e);
                        }
                    }

                    if(this.lovNPC.traits != null) {
                        traits = this.lovNPC.traits;

                        for (LOVTrait trait : traits) {
                            trait.npc_id = npcId;
                            trait.npc = npc;
                            trait.trait = this;
                            trait.onSpawn();
                        }
                    }
                });
            });
        });
    }

    @Override
    public void onDespawn() {
        for (LOVTrait trait : traits) {
            trait.onDespawn();
        }

        if (nameplates != null) {
            nameplates.remove();
            nameplates = null;
        }

        super.onDespawn();
    }

    public void onLeftClick(Player player) {
        TraitHelper.onLeftClick(npc.getName(), player, traits);
    }

    public void onRightClick(Player player) {
        TraitHelper.onRightClick(npc.getName(), player, traits);
    }

    public INPC getLovNPC() {
        return lovNPC;
    }

    public NPC getCitizen() {
        return super.npc;
    }
}