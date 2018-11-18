package com.legendsofvaleros.modules.quests.quest.stf;

public enum QuestStatus {
	/**
	 * If the quest has been accepted, but not completed.
	 */
	ACCEPTED(false),
	
	/**
	 * If the quest has been completed.
	 */
	COMPLETED(false),
	
	/**
	 * If the quest has been completed, but can be done again.
	 */
	REPEATABLE_READY(true),
	
	/**
	 * If the quest has been completed, but can be done again.
	 */
	REPEATABLE_NOT_READY(false),
	
	/**
	 * If the quest has not been accepted, nor completed.
	 */
	NEITHER(true),
	
	/**
	 * The quest has a prerequisite that has not been met.
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