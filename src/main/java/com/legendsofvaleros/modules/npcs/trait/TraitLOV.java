package com.legendsofvaleros.modules.npcs.trait;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.window.ExpandingGUI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.Skins;
import com.legendsofvaleros.modules.npcs.Skins.Skin;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitLOV extends Trait implements CommandConfigurable {
    public static String TRAIT_NAME = "lov";

    public static final List<TraitLOV> all = new ArrayList<>();

    private boolean updatedSkin = false;

    @Persist(required = true)
    public String npcId;

    public NPCData npcData;
    private LOVTrait[] traits = new LOVTrait[0];

    public transient Nameplates nameplates;
    public transient TextLine nameLine;

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
            MessageUtil.sendException(LegendsOfValeros.getInstance(), p, e, false);
            return;
        }

        String npcId = ctx.getString(1).toLowerCase();

        if (!NPCs.isNPC(npcId)) {
            MessageUtil.sendException(LegendsOfValeros.getInstance(), p, new Exception("No NPC with that ID exists in the cache. Offender: " + npcId), false);
            return;
        }

        this.npcId = npcId;
        MessageUtil.sendUpdate(p, "Setting NPC LOV ID to '" + npcId + "'.");
    }

    @Override
    public void onAttach() {
        super.onAttach();

        all.add(this);
    }

    @Override
    public void onRemove() {
        super.onRemove();

        all.remove(this);

        traits = new LOVTrait[0];
    }

    @Override
    public void onSpawn() {
        if (npcId == null) return;

        getNPC().data().setPersistent(NPC.SHOULD_SAVE_METADATA, true);

        npcData = NPCs.getNPC(npcId);
        if (npcData == null) {
            getNPC().data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, true);
            getNPC().getEntity().setCustomNameVisible(true);
            MessageUtil.sendException(LegendsOfValeros.getInstance(), null, new Exception("No NPC with that ID exists in the cache. Offender: " + npcId + " on " + getNPC().getId()), false);
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
            MessageUtil.sendException(LegendsOfValeros.getInstance(), null, new Exception("NPC has a null name. Offender: " + npcId + " on " + getNPC().getId()), false);
        }

        nameplates = Nameplates.get(getNPC());

        nameLine = nameplates.getOrAdd(Nameplates.BASE).appendTextLine(npcData.name);

        if (npcData.skin != null && npcData.skin.trim().length() > 0 && !updatedSkin && getNPC().getEntity() instanceof SkinnableEntity) {
            updatedSkin = true;

            ListenableFuture<Skin> future = Skins.inst().getSkin(npcData.skin);
            Skin skin;
            try {
                skin = future.get();

                if (skin == null)
                    throw new Exception("No skin with that ID. Offender: " + npcData.skin + " on " + npcData.npcId);

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
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, e, false);
            }
        }

        if (npcData.loc == null || npcData.loc.getWorld() == null
                || npcData.loc.getWorld() != getNPC().getEntity().getLocation().getWorld()
                || getNPC().getEntity().getLocation().distance(npcData.loc) > 2) {
            npcData.loc = getNPC().getEntity().getLocation().getBlock().getLocation();
            NPCs.manager().updateNPC(this, getNPC());
        }

        npcId = npcData.npcId;

        traits = npcData.traits;

        super.onSpawn();

        for (LOVTrait trait : traits) {
            trait.npc_id = npcId;
            trait.npc = getNPC();
            trait.trait = this;
            trait.onSpawn();
        }
    }

    @Override
    public void onDespawn() {
        for (LOVTrait trait : traits)
            trait.onDespawn();

        if (nameplates != null) {
            nameplates.remove();
            nameplates = null;
        }

        super.onDespawn();
    }

    public void onLeftClick(Player player) {
        SettableFuture<Boolean> future = SettableFuture.create();
        List<Slot> slots = new ArrayList<>();
        future.addListener(() -> {
            if (slots.size() == 0) return;

            if (slots.size() == 1) {
                slots.get(0).action.doAction(null, player, null);
            } else {
                showSeparated(player, slots);
            }
        }, Utilities.syncExecutor());


        AtomicInteger futuresLeft = new AtomicInteger(traits.length);

        for (LOVTrait trait : traits) {
            SettableFuture<Slot> traitSlot = SettableFuture.create();

            trait.onLeftClick(getNPC(), player, traitSlot);

            traitSlot.addListener(() -> {
                addSlotListener(player, slots, traitSlot);

                if (futuresLeft.decrementAndGet() == 0)
                    future.set(true);
            }, Utilities.asyncExecutor());
        }
    }

    private void addSlotListener(Player player, List<Slot> slots, SettableFuture<Slot> traitSlot) {
        try {
            Slot slot = traitSlot.get();
            if (slot != null)
                slots.add(slot);
        } catch (Exception e) {
            MessageUtil.sendException(LegendsOfValeros.getInstance(), player, e, true);
        }
    }

    public void onRightClick(Player player) {
        SettableFuture<List<Slot>> future = SettableFuture.create();
        future.addListener(() -> {
            try {
                List<Slot> slots = future.get();
                if (slots.size() == 0) return;

                showSeparated(player, slots);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, Utilities.syncExecutor());

        List<Slot> slots = new ArrayList<>();
        AtomicInteger futuresLeft = new AtomicInteger(traits.length);

        for (LOVTrait trait : traits) {
            SettableFuture<Slot> traitSlot = SettableFuture.create();

            trait.onRightClick(getNPC(), player, traitSlot);

            traitSlot.addListener(() -> {
                addSlotListener(player, slots, traitSlot);

                if (futuresLeft.decrementAndGet() == 0)
                    future.set(slots);
            }, Utilities.asyncExecutor());
        }
    }
	
	/*private ListenableFuture<List<LOVTrait>> getActiveTraits(Player player) {
		SettableFuture<List<LOVTrait>> future = SettableFuture.create();
		
		List<LOVTrait> activeTraits = new ArrayList<LOVTrait>();
		AtomicInteger futuresLeft = new AtomicInteger(traits.length);
		
		for(LOVTrait trait : traits) {
			SettableFuture<Boolean> isActive = SettableFuture.create();
			
			trait.isActive(player, isActive);

			isActive.addListener(() -> {
				try {
					if(isActive.get())
						activeTraits.add(trait);
				} catch (Exception e) {
					MessageUtil.sendException(NPCs.inst(), player, e, true);
				}

				if(futuresLeft.decrementAndGet() == 0)
					future.set(activeTraits);
			}, Utilities.asyncExecutor());
		}
		
		return future;
	}*/

    public void showSeparated(Player player, List<Slot> slots) {
        GUI gui = new ExpandingGUI(getNPC().getName(), slots);

        if (slots.size() == 1) {
            slots.get(0).action.doAction(gui, player, null);
        } else {
            // Resync with the main thread.
            Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> gui.open(player));
        }
    }
}