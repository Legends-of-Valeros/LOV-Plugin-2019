package com.legendsofvaleros.modules.mobs.trait;

import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.npcs.nameplate.Nameplates;
import com.legendsofvaleros.util.item.Model;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MobTrait extends Trait {
	public static final String TRAIT_NAME = "fuck_you_citizens";

	public final Mob.Instance instance;

	TextLine nameplate;
	ItemLine rarityPlate;
	
	public MobTrait(Mob.Instance instance) {
		super(TRAIT_NAME);
		this.instance = instance;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		getNPC().getNavigator().getLocalParameters().useNewPathfinder();

		LivingEntity entity = (LivingEntity)npc.getEntity();

		nameplate = Nameplates.get(entity).get(Nameplates.BASE).appendTextLine(instance.mob.getRarity().newNameplate(instance));
		
		{
			try {
				Model model = Model.get("instance-rarity-" + instance.mob.getRarity().name().toLowerCase()).get(5L, TimeUnit.SECONDS);
				if(model != Model.NONE) {
					rarityPlate = nameplate.getParent().insertItemLine(0, model.toStack().create());
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}

		if(instance.mob.getOptions().ghost) {
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