package com.legendsofvaleros.modules.gear.component;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;

public abstract class GearComponent<T> {
	public final double doGetValue(Gear.Instance item, Object persist) {
		return getValue(item, (T)persist);
	}

	public final void doGenerate(Gear.Instance item, Object persist, ItemBuilder builder) {
		onGenerateItem(item, (T)persist, builder);
	}

	public final Boolean doTest(Gear.Instance item, Object persist, GearTrigger trigger) {
		return test(item, (T)persist, trigger);
	}

	@SuppressWarnings("unchecked")
	public final T doFire(Gear.Instance item, Object persist, GearTrigger trigger) {
		return fire(item, (T)persist, trigger);
	}

	public abstract GearComponentOrder getOrder();
	public abstract T onInit();

	/**
	 * The "power" of this component. These are added up to determine total item power.
	 */
	public abstract double getValue(Gear.Instance item, T persist);

	protected abstract void onGenerateItem(Gear.Instance item, T persist, ItemBuilder builder);
	
	/**
	 * This should return true if, for the defined trigger, it should be allowed to fire().
	 * 
	 * You MUST make sure to return null on any trigger that is not affected by this component.
	 */
	protected Boolean test(Gear.Instance item, T persist, GearTrigger trigger) { return null; }
	
	/**
	 * Fire the component. This is called after all components have returned true in their test()
	 * methods. Return the persistent variable if it changed; If it didn't change, return null.
	 */
	protected T fire(Gear.Instance item, T persist, GearTrigger trigger) { return null; }
}