package com.legendsofvaleros.modules.gear.component.skills;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.trigger.UseTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.item.NoPersist;
import com.legendsofvaleros.modules.skills.SkillTree.SpecializedTree;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map.Entry;

public class SkillResetComponent extends GearComponent<NoPersist> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.EXTRA; }
	@Override public NoPersist onInit() { return null; }

	@Override public double getValue(Gear.Instance item, NoPersist persist) {
		//TODO check if correct value
		return 0;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, NoPersist persist, ItemBuilder builder) {
		builder.addLore(ChatColor.LIGHT_PURPLE + "* Resets your skill points");
	}
	
	@Override
	public Boolean test(Gear.Instance item, NoPersist persist, GearTrigger trigger) {
		if(trigger.equals(UseTrigger.class)) {
			UseTrigger t = (UseTrigger)trigger;
			if(t.getEntity().isPlayer()) {
				PlayerCharacter pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());
				
				for(SpecializedTree tree : SkillsController.skillTrees[pc.getPlayerClass().ordinal()].getSpecializedTrees())
					for(String skill : tree.skills)
						if(pc.getSkillSet().getCharacterSkill(skill) != null)
							return true;
			}
		}
		
		return null;
	}
	
	@Override
	public NoPersist fire(Gear.Instance item, NoPersist persist, GearTrigger trigger) {
		if(trigger.equals(UseTrigger.class)) {
			UseTrigger t = (UseTrigger)trigger;
			if(t.getEntity().isPlayer()) {
				PlayerCharacter pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());
				
				Entry<Skill, Integer> skillObj;
				for(String skill : SkillsController.skillTrees[pc.getPlayerClass().ordinal()].getCoreSkills()) {
					if((skillObj = pc.getSkillSet().removeCharacterSkill(skill)) != null) {
						pc.getSkillSet().addCharacterSkill(skill);
						MessageUtil.sendUpdate(pc.getPlayer(), "You've suddenly forgotten your training for " + skillObj.getKey().getUserFriendlyName(skillObj.getValue()) + ".");
					}
				}
				
				for(SpecializedTree tree : SkillsController.skillTrees[pc.getPlayerClass().ordinal()].getSpecializedTrees())
					for(String skill : tree.skills) {
						if((skillObj = pc.getSkillSet().removeCharacterSkill(skill)) != null)
							MessageUtil.sendUpdate(pc.getPlayer(), "You have forgotten how to use " + skillObj.getKey().getUserFriendlyName(skillObj.getValue()));
					}
			}
			
			item.amount--;
		}
		
		return null;
	}
}