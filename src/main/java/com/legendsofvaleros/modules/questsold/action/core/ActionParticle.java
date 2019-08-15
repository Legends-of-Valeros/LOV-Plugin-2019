package com.legendsofvaleros.modules.questsold.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Particle;

public class ActionParticle extends AbstractQuestAction {
	String particleId;
	boolean relative;
	double x, y, z;
	double xOffset, yOffset, zOffset;
	int count;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		double x, y, z;

		if(!relative) {
			x = this.x; y = this.y; z = this.z;
		}else{
			x = pc.getLocation().getX() + this.x;
			y = pc.getLocation().getY() + this.y;
			z = pc.getLocation().getZ() + this.z;
		}

		pc.getPlayer().getWorld().spawnParticle(Particle.valueOf(particleId),
											x, y, z, count,
											xOffset, yOffset, zOffset);
		
		next.go();
	}
}