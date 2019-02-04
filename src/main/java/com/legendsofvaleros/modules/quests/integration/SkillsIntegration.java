package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.objective.skills.SkillBindObjective;
import com.legendsofvaleros.modules.quests.objective.skills.SkillUseObjective;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillsIntegration extends Integration implements Listener {
    public SkillsIntegration() {
        RegionController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("skill_bind", SkillBindObjective.class);
        QuestObjectiveFactory.registerType("skill_use", SkillUseObjective.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSkillUsed(SkillUsedEvent event) {
        if (!event.getCombatEntity().isPlayer()) return;

        Player p = (Player) event.getLivingEntity();

        if (!Characters.isPlayerCharacterLoaded(p)) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(p));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBound(BindSkillEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}
