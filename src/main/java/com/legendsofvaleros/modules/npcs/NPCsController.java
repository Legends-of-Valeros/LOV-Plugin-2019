package com.legendsofvaleros.modules.npcs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.fast_travel.FastTravelController;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.mount.MountsController;
import com.legendsofvaleros.modules.npcs.commands.NPCCommands;
import com.legendsofvaleros.modules.npcs.core.NPCData;
import com.legendsofvaleros.modules.npcs.core.Skin;
import com.legendsofvaleros.modules.npcs.integration.*;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.npcs.trait.core.TraitTitle;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.queue.QueueController;
import com.legendsofvaleros.modules.skills.SkillsController;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@IntegratesWith(module = QueueController.class, integration = QueueIntegration.class)
@IntegratesWith(module = AuctionController.class, integration = AuctionsIntegration.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = FastTravelController.class, integration = FastTravelIntegration.class)
@IntegratesWith(module = HearthstoneController.class, integration = HearthstonesIntegration.class)
@IntegratesWith(module = MailboxController.class, integration = MailboxIntegration.class)
@IntegratesWith(module = MountsController.class, integration = MountIntegration.class)
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
@IntegratesWith(module = QuestController.class, integration = QuestsIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@ModuleInfo(name = "NPCs", info = "")
public class NPCsController extends NPCsAPI {
    private static NPCsController instance;

    public static NPCsController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new NPCCommands());

        registerTrait("title", TraitTitle.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeftClick(NPCLeftClickEvent event) {
        if (!event.getNPC().hasTrait(TraitLOV.class)) return;
        event.getNPC().getTrait(TraitLOV.class).onLeftClick(event.getClicker());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().hasTrait(TraitLOV.class)) return;
        event.getNPC().getTrait(TraitLOV.class).onRightClick(event.getClicker());
    }

    public void registerTrait(String id, Class<? extends LOVTrait> trait) {
        traitTypes.put(id, trait);
    }

    public NPC createNPC(EntityType type, String s) {
        return registry.createNPC(type, s);
    }

    public boolean isNPC(String id) {
        return npcs.containsKey(id);
    }

    public NPCData getNPC(String id) {
        return npcs.get(id);
    }

    public NPC getNPC(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity);
    }

    public boolean isNPC(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    public boolean isStaticNPC(LivingEntity entity) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        return npc != null && npc.getOwningRegistry() == CitizensAPI.getNPCRegistry();
    }

    public Skin getSkin(String id) {
        return skins.get(id);
    }
}