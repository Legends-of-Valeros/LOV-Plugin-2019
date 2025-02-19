package com.legendsofvaleros.modules.npcs.trait;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.core.Skin;
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

public class TraitLOV extends Trait implements CommandConfigurable {
    public static final String TRAIT_NAME = "lov";
    public static final List<TraitLOV> all = new ArrayList<>();
    private boolean updatedSkin = false;

    @Persist(required = true)
    public String npcId;

    private NPCData npcData;
    private LOVTrait[] traits = new LOVTrait[0];

    public Nameplates nameplates;
//    public TextLine nameLine;

    public TraitLOV() {
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

        if (!NPCsController.getInstance().isNPC(id)) {
            MessageUtil.sendException(NPCsController.getInstance(), p, new Exception("No NPC with that ID exists in the cache. Offender: " + id));
            return;
        }

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

        if (npcId == null) {
            return;
        }

        getNPC().data().setPersistent(NPC.SHOULD_SAVE_METADATA, true);

        npcData = NPCsController.getInstance().getNPC(npcId);
        if (npcData == null) {
            getNPC().data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, true);
            getNPC().getEntity().setCustomNameVisible(true);
            MessageUtil.sendException(NPCsController.getInstance(), "No NPC with that ID exists in the cache. Offender: " + npcId + " on " + getNPC().getId());
            return;
        }

        getNPC().data().setPersistent(NPC.COLLIDABLE_METADATA, true);
        getNPC().data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
        getNPC().getEntity().setCustomNameVisible(false);

        if (getNPC().getEntity().getType() == EntityType.PLAYER) {
            Snowball s = (Snowball) getNPC().getEntity().getWorld().spawnEntity(getNPC().getEntity().getLocation(), EntityType.SNOWBALL);
            s.setSilent(true);
            getNPC().getEntity().addPassenger(s);
        }

        if (npcData.name != null) {
            if (!getNPC().getName().equals(npcData.name)) {
                getNPC().setName(npcData.name);
                return;
            }
        } else {
            MessageUtil.sendException(NPCsController.getInstance(), "NPC has a null name. Offender: " + npcId + " on " + getNPC().getId());
        }

        nameplates = Nameplates.get(getNPC());
        nameplates.getOrAdd(Nameplates.BASE).appendTextLine(npcData.name);

        if (npcData.skin != null && npcData.skin.trim().length() > 0 && !updatedSkin && getNPC().getEntity() instanceof SkinnableEntity) {
            updatedSkin = true;

            try {
                Skin skin = NPCsController.getInstance().getSkin(npcData.skin);
                if (skin == null) {
                    throw new Exception("No skin with that ID. Offender: " + npcData.skin + " on " + npcData.id);
                }

                npc.data().setPersistent("cached-skin-uuid", skin.uuid);
                npc.data().setPersistent("cached-skin-uuid-name", skin.username.toLowerCase());
                npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, skin.username.toLowerCase());
                npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, skin.signature);
                npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, skin.data);
                npc.data().setPersistent(NPC.PLAYER_SKIN_USE_LATEST, false);

                SkinnableEntity se = (SkinnableEntity) getNPC().getEntity();
                se.getSkinTracker().notifySkinChange(false);

                return;
            } catch (Exception e) {
                MessageUtil.sendException(NPCsController.getInstance(), null, e);
            }
        }

        if (npcData.getLocation() == null || npcData.world == null
                || npcData.world != getNPC().getEntity().getLocation().getWorld()
                || getNPC().getEntity().getLocation().distance(npcData.getLocation()) > 2) {
            npcData.setLocation(getNPC());
            NPCsController.getInstance().saveNPC(this);
        }

        npcId = npcData.id;
        traits = npcData.traits;

        for (LOVTrait trait : traits) {
            trait.npc_id = npcId;
            trait.npc = getNPC();
            trait.trait = this;
            trait.onSpawn();
        }
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
        TraitHelper.onLeftClick(getNPC().getName(), player, traits);
    }

    public void onRightClick(Player player) {
        TraitHelper.onRightClick(getNPC().getName(), player, traits);
    }

    public NPCData getNpcData() {
        return npcData;
    }

    public String getNpcId() {
        return npcId;
    }
}