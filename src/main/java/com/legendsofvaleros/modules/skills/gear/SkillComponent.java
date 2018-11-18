package com.legendsofvaleros.modules.skills.gear;

import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skill.Skill;
import org.bukkit.ChatColor;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.UseTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.NoPersist;

public class SkillComponent extends GearComponent<NoPersist> {
    @Override public GearComponentOrder getOrder() {
        return GearComponentOrder.EXTRA;
    }

    public String id;
    public int level = 1;

    public transient Skill skill;

    @Override
    public NoPersist onInit() {
        if (id != null && skill == null)
            skill = Skill.getSkillById(id);
        return null;
    }

    @Override public double getValue(GearItem.Instance item, NoPersist persist) {
        //TODO check if correct value
        return 0;
    }

    @Override
    protected void onGenerateItem(GearItem.Instance item, NoPersist persist, ItemBuilder builder) {
        builder.addLore(ChatColor.AQUA + "Skillbound: " + (skill == null ? "Unknown" : skill.getUserFriendlyName(level)));
    }

    @Override
    public Boolean test(GearItem.Instance item, NoPersist persist, GearTrigger t) {
        if (!t.equals(UseTrigger.class)) return null;
        return skill != null;
    }

    @Override
    public NoPersist fire(GearItem.Instance item, NoPersist persist, GearTrigger t) {
        if (!t.equals(UseTrigger.class)) return null;

        UseTrigger trigger = (UseTrigger) t;

        skill.onSkillUse(trigger.getEntity().getLivingEntity().getWorld(), trigger.getEntity(), level);

        return null;
    }
}