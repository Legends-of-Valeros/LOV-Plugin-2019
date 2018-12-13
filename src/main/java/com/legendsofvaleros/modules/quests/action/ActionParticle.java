package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ActionParticle extends AbstractQuestAction {
	String particleId;
	boolean relative;
	double x, y, z;
	double xOffset, yOffset, zOffset;
	int count;
	
	@Override
	public void play(Player player, Next next) {
		double x, y, z;

		if(!relative) {
			x = this.x; y = this.y; z = this.z;
		}else{
			x = player.getLocation().getX() + this.x;
			y = player.getLocation().getY() + this.y;
			z = player.getLocation().getZ() + this.z;
		}

		player.getWorld().spawnParticle(Particle.valueOf(particleId),
											x, y, z, count,
											xOffset, yOffset, zOffset);
		
		next.go();
	}
}