package com.legendsofvaleros.modules.gear.component.trigger;

public abstract class GearTrigger {
	private boolean refresh = false;

	/**
	 * Call this if the item stack should be regenerated. This cannot be undone.
	 */
	public void requestStackRefresh() { refresh = true; }
	public boolean shouldRefreshStack() { return refresh; }
	
	public boolean equals(Class<? extends GearTrigger> c) {
		return c.isAssignableFrom(getClass());
	}
	
	@Deprecated
	public boolean equals(Object o) { return false; }
	
	public enum TriggerEvent {
		NOTHING(false),
		NBT_UPDATED(true),
		REFRESH_STACK(true);
		
		private final boolean changed;
		public boolean didChange() { return changed; }

		TriggerEvent(boolean changed) {
			this.changed = changed;
		}
	}
}