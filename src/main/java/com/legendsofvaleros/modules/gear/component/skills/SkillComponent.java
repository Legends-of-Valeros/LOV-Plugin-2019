package com.legendsofvaleros.modules.gear.component.skills;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.trigger.UseTrigger;
import org.bukkit.ChatColor;

public class SkillComponent extends GearComponent<Void> {
    @Override public GearComponentOrder getOrder() {
        return GearComponentOrder.EXTRA;
    }

    public String id;
    public int level = 1;

    public transient Skill skill;

    @Override
    public Void onInit() {
        if (id != null && skill == null)
            skill = Skill.getSkillById(id);
        return null;
    }

    @Override public double getValue(Gear.Instance item, Void persist) {
        //TODO check if correct value
        return 0;
    }

    @Override
    protected void onGenerateItem(Gear.Instance item, Void persist, ItemBuilder builder) {
        builder.addLore(ChatColor.AQUA + "Skillbound: " + (skill == null ? "Unknown" : skill.getUserFriendlyName(level)));
    }

    @Override
    public Boolean test(Gear.Instance item, Void persist, GearTrigger t) {
        if (!t.equals(UseTrigger.class)) return null;
        return skill != null;
    }

    @Override
    public Void fire(Gear.Instance item, Void persist, GearTrigger t) {
        if (!t.equals(UseTrigger.class)) return null;

        UseTrigger trigger = (UseTrigger) t;

        skill.onSkillUse(trigger.getEntity().getLivingEntity().getWorld(), trigger.getEntity(), level);

        return null;
    }
}