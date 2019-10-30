package com.legendsofvaleros.modules.skills.core.priest;

import com.legendsofvaleros.modules.skills.core.SkillTree;

public class TreePriest extends SkillTree {

  @Override
  public void initSkills() {

  }

  @Override
  public String getName() {
    return "The Priest";
  }

  @Override
  public String[] getDescription() {
    return new String[]{
        "Adept Spellcaster. Using your knowledge",
        "of the arcane you hold the ability to",
        "instantly turn the tide of battle,",
        "whether it be for or against yourself."
    };
  }

  @Override
  public String[] getCoreSkills() {
    return new String[]{
        //TODO add core skills
    };
  }

  @Override
  public SpecializedTree[] getSpecializedTrees() {
    return new SpecializedTree[]{
        //TODO add both specialized tree
    };
  }
}