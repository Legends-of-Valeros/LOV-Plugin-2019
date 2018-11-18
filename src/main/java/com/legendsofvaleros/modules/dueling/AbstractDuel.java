package com.legendsofvaleros.modules.dueling;

public abstract class AbstractDuel {
	public abstract boolean shouldEnd();
	public abstract void onStart();
	public abstract void onEnd();
}