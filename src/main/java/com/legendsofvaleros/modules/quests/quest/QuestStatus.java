package com.legendsofvaleros.modules.quests.quest;

public enum QuestStatus {
	/**
	 * If the gear has been accepted, but not completed.
	 */
	ACCEPTED(false),
	
	/**
	 * If the gear has been completed.
	 */
	COMPLETED(false),
	
	/**
	 * If the gear has been completed, but can be done again.
	 */
	REPEATABLE_READY(true),
	
	/**
	 * If the gear has been completed, but can be done again.
	 */
	REPEATABLE_NOT_READY(false),
	
	/**
	 * If the gear has not been accepted, nor completed.
	 */
	NEITHER(true),
	
	/**
	 * The gear has a prerequisite that has not been met.
	 */
	PREREQ(false),
	
	NONE(false);
	
	boolean canAccept;
	
	QuestStatus(boolean canAccept) {
		this.canAccept = canAccept;
	}

	public boolean canAccept() {
		return this.canAccept;
	}
}