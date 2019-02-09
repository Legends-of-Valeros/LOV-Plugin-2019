package com.legendsofvaleros.modules.quests.core;

public class QuestProgressPack {
	public final Integer group;
	public final Object[] data;
	public Integer actionI;
	
	public QuestProgressPack(Integer group, int objectivesSize) {
		this.group = group;
		this.data = new Object[objectivesSize];
	}

	public Object getForObjective(int objectiveI) { return data[objectiveI]; }

	public <T> void setForObjective(int objectiveI, T obj) { data[objectiveI] = obj; }
}