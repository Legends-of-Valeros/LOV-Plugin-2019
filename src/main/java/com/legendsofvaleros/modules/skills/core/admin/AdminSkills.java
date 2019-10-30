package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.modules.classes.skills.Skill;

public class AdminSkills {

  private AdminSkills() {
  }

  private static final Skill[] skills = new Skill[]{
      new SkillWarp(),
      new SkillInsight()
  };

  public static Skill[] getSkills() {
    return skills;
  }
}
