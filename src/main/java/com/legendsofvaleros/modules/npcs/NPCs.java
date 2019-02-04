package com.legendsofvaleros.modules.npcs;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.fast_travel.FastTravel;
import com.legendsofvaleros.modules.hearthstones.Hearthstones;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.mount.Mounts;
import com.legendsofvaleros.modules.npcs.integration.*;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.trait.core.TraitTitle;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.skills.Skills;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;

@IntegratesWith(module = AuctionController.class, integration = AuctionsIntegration.class)
@IntegratesWith(module = Bank.class, integration = BankIntegration.class)
@IntegratesWith(module = FastTravel.class, integration = FastTravelIntegration.class)
@IntegratesWith(module = Hearthstones.class, integration = HearthstonesIntegration.class)
@IntegratesWith(module = MailboxController.class, integration = MailboxIntegration.class)
@IntegratesWith(module = Mounts.class, integration = MountIntegration.class)
@IntegratesWith(module = PvP.class, integration = PvPIntegration.class)
@IntegratesWith(module = QuestController.class, integration = QuestsIntegration.class)
@IntegratesWith(module = Skills.class, integration = SkillsIntegration.class)
public class NPCs extends ModuleListener {
    private static NPCs instance;
    public static NPCs getInstance() { return instance; }

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

        registerTrait("title", TraitTitle.class);
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