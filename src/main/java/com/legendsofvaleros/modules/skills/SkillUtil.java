package com.legendsofvaleros.modules.skills;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.gear.component.trigger.SpellAttackTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import org.bukkit.entity.Player;

public class SkillUtil {
    public static double getSpellDamage(CombatEntity attacker, SpellType type, double baseDamage) {
        GearItem.Instance held = null;
        if (attacker.isPlayer())
            if (((Player) attacker.getLivingEntity()).getInventory().getHeldItemSlot() == Hotswitch.HELD_SLOT)
                held = GearItem.Instance.fromStack(attacker.getLivingEntity().getEquipment().getItemInMainHand());

        if (held != null) {
            SpellAttackTrigger t = new SpellAttackTrigger(attacker);
            if (Boolean.FALSE.equals(held.doTest(t))) {
                Skills.getInstance().getLogger().info("false");
            } else {
                if (held.doFire(t).didChange()) {
                    // XXX: Update stack
                }

                baseDamage = t.getDamage();
            }
        }

        return baseDamage;
    }
}