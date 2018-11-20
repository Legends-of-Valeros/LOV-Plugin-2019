package com.legendsofvaleros.modules.characters.creation;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.ui.CharacterCreationListener;

import java.util.Random;

public class CreationInfo {
	final int age = new Random().nextInt(8) + 19;
	final int day = new Random().nextInt(20) + 1;
	final int month = new Random().nextInt(12) + 1;

	public int number;
	public EntityClass clazz;
	public EntityRace race;
	
	public final CharacterCreationListener listener;
	
	CreationInfo(int number, CharacterCreationListener listener) {
		this.number = number;
		this.listener = listener;
	}
}