package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class ActionParticle extends AbstractQuestAction {
	String particleId;
	double x, y, z;
	double xOffset, yOffset, zOffset;
	int count;
	
	@Override
	public void play(Player player, Next next) {
		player.getWorld().spawnParticle(Particle.valueOf(particleId),
											x, y, z, count,
											xOffset, yOffset, zOffset);
		
		next.go();
	}
}