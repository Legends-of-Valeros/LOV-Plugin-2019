package com.legendsofvaleros.modules.gear.integration;


import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.skills.CastTrigger;
import com.legendsofvaleros.modules.gear.component.skills.GearCharge;
import com.legendsofvaleros.modules.gear.component.skills.SkillComponent;
import com.legendsofvaleros.modules.gear.component.skills.SkillResetComponent;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.skills.Skills;
import com.legendsofvaleros.modules.skills.event.SkillPreUseEvent;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillsIntegration extends Integration implements Listener {
    public SkillsIntegration() {
        GearRegistry.registerComponent("charge", GearCharge.Component.class);
        GearRegistry.registerComponent("skill", SkillComponent.class);
        GearRegistry.registerComponent("skills_reset", SkillResetComponent.class);

        Skills.getInstance().registerEvents(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSkillUse(SkillPreUseEvent e) {
        if (!e.getCombatEntity().isPlayer()) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player) e.getLivingEntity());

        if (e.getSkill().doesRequireFocus()) {
            Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
            if (instance != null && Boolean.TRUE.equals(instance.doTest(new CastTrigger(e.getCombatEntity(), e.getSkill(), e.getLevel()))))
                return;

            MessageUtil.sendError(pc.getPlayer(), "You are unable to focus " + e.getSkill().getStatUsed().getUserFriendlyName() + " for that skill.");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSkillUsed(SkillUsedEvent e) {
        if (!e.getCombatEntity().isPlayer()) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player) e.getLivingEntity());

        if (pc.getPlayerClass().getSkillCostType() == RegeneratingStat.MANA) {
            Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
            if (instance != null) {
                if (instance.doFire(new CastTrigger(e.getCombatEntity(), e.getSkill(), e.getLevel())) == GearTrigger.TriggerEvent.REFRESH_STACK)
                    pc.getPlayer().getInventory().setItem(Hotswitch.HELD_SLOT, instance.toStack());
            }
        }
    }
}