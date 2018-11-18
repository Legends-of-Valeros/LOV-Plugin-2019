package com.legendsofvaleros.modules.mobs.behavior;

public enum NodeStatus {
	/**
	 * Success will cause the behavior to continue on to the next action in the set.
	 */
	SUCCESS,
	
	/**
	 * Fail will cause the behavior to stop, rather than continue on to the next action
	 * in the set.
	 */
	FAIL,
	
	/**
	 * Will cause the behavior to execute on the next step.
	 */
	ONGOING
}