package com.legendsofvaleros.modules.mobs.trait;

import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.model.Model;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MobTrait extends Trait {
	public static final String TRAIT_NAME = "LOVMobTrait";

	public final Mob.Instance instance;

	private TextLine nameplate;
	private ItemLine rarityPlate;
	
	public MobTrait(Mob.Instance instance) {
		super(TRAIT_NAME);
		this.instance = instance;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		getNPC().getNavigator().getLocalParameters().useNewPathfinder();

		LivingEntity entity = (LivingEntity)npc.getEntity();

		nameplate = Nameplates.get(entity).get(Nameplates.BASE).appendTextLine(instance.entity.getRarity().newNameplate(instance));
		
		{
			Model model = Model.get("instance-rarity-" + instance.entity.getRarity().name().toLowerCase());
			if(model != Model.NONE) {
				rarityPlate = nameplate.getParent().insertItemLine(0, model.toStack().create());
			}
		}

		if(instance.entity.isGhost()) {
			entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
			entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		}
	}
	
	@Override
	public void onDespawn() {
		super.onDespawn();

		instance.destroy();
	}
}