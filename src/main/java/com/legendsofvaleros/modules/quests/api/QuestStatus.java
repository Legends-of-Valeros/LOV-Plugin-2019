package com.legendsofvaleros.modules.quests.api;

// TODO: Include the ID of the floating icon above an NPC's head?
public enum QuestStatus {
	/**
	 * The quest has a prerequisite that has not been met.
	 */
	PREREQUISITE_FAIL(false),

	/**
	 * If the quest has been accepted, but not completed.
	 */
	ACTIVE(false),
	
	/**
	 * If the quest has been completed, failed, or abandoned. Only returned if the quest is not repeatable at all.
	 */
	ENDED(false),

	/**
	 * If the quest can be accepted.
	 */
	READY(true),

	/**
	 * If the quest is not yet ready to be accepted. Typically used by repeatable quests.
	 */
	NOT_READY(false);
	
	boolean canAccept;
	
	QuestStatus(boolean canAccept) {
		this.canAccept = canAccept;
	}

	public boolean canAccept() {
		return this.canAccept;
	}
}