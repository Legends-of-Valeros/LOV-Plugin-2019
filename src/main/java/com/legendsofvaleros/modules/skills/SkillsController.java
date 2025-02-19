package com.legendsofvaleros.modules.skills;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.features.playermenu.PlayerMenu;
import com.legendsofvaleros.features.playermenu.events.PlayerOptionsOpenEvent;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.skills.core.SkillTree;
import com.legendsofvaleros.modules.skills.core.mage.TreeMage;
import com.legendsofvaleros.modules.skills.core.priest.TreePriest;
import com.legendsofvaleros.modules.skills.core.rogue.TreeRogue;
import com.legendsofvaleros.modules.skills.core.warrior.TreeWarrior;
import com.legendsofvaleros.modules.skills.event.FallDamage;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.SkillPreUseEvent;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.modules.skills.gui.GUISkillsCore;
import com.legendsofvaleros.modules.skills.listener.HotbarListener;
import com.legendsofvaleros.modules.skills.listener.SkillListener;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.RomanNumeral;
import com.legendsofvaleros.util.model.Model;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

@DependsOn(PlayerMenu.class)
@DependsOn(BankController.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(MobsController.class)
@DependsOn(Hotswitch.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Skills", info = "")
public class SkillsController extends SkillsAPI {

  public static final SkillTree[] skillTrees = new SkillTree[]{
      new TreeWarrior(),
      new TreeRogue(),
      new TreeMage(),
      new TreePriest()
  };

  private static SkillsController instance;

  public static SkillsController getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    super.onLoad();

    instance = this;

    new HotbarListener();

    new FallDamage();
    new NextAttack();
    new OnProjectile();

    for (SkillTree tree : skillTrees) {
      if (tree == null) {
        continue;
      }
      tree.initSkills();
    }

    SkillsController.getInstance().registerEvents(new SkillListener());
  }

  @EventHandler
  public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
    event.addSlot(Model.stack("menu-skill-tree-button").setName("Skill Tree").create(),
        (gui, p, type) -> new GUISkillsCore(Characters.getPlayerCharacter(p)).open(p));
  }

  public static void castSkill(CombatEntity caster, Skill skill, int level) {
    if (caster.isPlayer()) {
      PlayerCharacter pc = Characters.getPlayerCharacter((Player) caster.getLivingEntity());

      Cooldowns cooldowns = pc.getCooldowns();
      if (cooldowns.hasCooldown("skill-" + skill.getId())) {
        return;
      }

      if (caster.getStats().getRegeneratingStat(pc.getPlayerClass().getSkillCostType()) < skill.getSkillCost(level)) {
        return;
      }
    }

    if (!caster.getStatusEffects().canUseSkills()) {
      if (caster.isPlayer()) {
        MessageUtil.sendError(caster.getLivingEntity(), "You are unable to use skills right now!");
      }
    } else {
      SkillPreUseEvent event = new SkillPreUseEvent(caster, skill, level);
      Bukkit.getPluginManager().callEvent(event);

      if (!event.isCancelled()) {
        if (skill.onSkillUse(caster.getLivingEntity().getWorld(), caster, level)) {
          Bukkit.getPluginManager().callEvent(new SkillUsedEvent(caster, skill, level));
        }
      }
    }
  }

  public static int getPointCount(Player p) {
    PlayerCharacter character = Characters.getPlayerCharacter(p);

    int points = character.getExperience().getLevel();

    List<Entry<Skill, Integer>> skills = character.getSkillSet().getSkills();
    for (Entry<Skill, Integer> skill : skills) {
      points -= skill.getKey().getTotalLevelCost(skill.getValue());
    }

    return points;
  }

  public static ItemStack getStackForSkillCooldown(PlayerCharacter pc, Entry<Skill, Integer> skill) {
    if (skill == null) {
      return Model.EMPTY_SLOT;
    }

    ItemBuilder builder = getItemRepresentation(skill.getKey());

    int stackSize = 1;
    if (pc.getCooldowns().hasCooldown("skill-" + skill.getKey().getId())) {
      stackSize = (int) Math.ceil(pc.getCooldowns().getCooldown("skill-" + skill.getKey().getId()).getRemainingDurationMillis() / 1000);
      if (stackSize >= 60) {
        stackSize = 60;
      } else {
        stackSize += 1;
      }
      builder.setEnchanted(false);
    } else {
      builder.setEnchanted(true);
    }

    builder.setAmount(stackSize);

    builder.setName(ChatColor.BOLD + skill.getKey().getUserFriendlyName(skill.getValue()) + " " + RomanNumeral.convertToRoman(skill.getValue()));
    builder.addLore(skill.getKey().getSkillDescription(pc, skill.getValue(), false));
    builder.addLore("", ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Right Click" + ChatColor.DARK_GRAY + " to Remove]");
    builder.setTag("skill", skill.getKey().getId());

    return builder.create();
  }

  public static ItemBuilder getItemRepresentation(Skill skill) {
    return Model.stack("skill-" + skill.getId());
  }
}