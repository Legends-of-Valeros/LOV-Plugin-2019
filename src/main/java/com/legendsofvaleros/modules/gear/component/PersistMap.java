package com.legendsofvaleros.modules.gear.component;

import com.legendsofvaleros.modules.gear.GearRegistry;

import java.util.HashMap;

public class PersistMap extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public <T> void putPersist(Class<? extends GearComponent<T>> component, T obj) {
		put(GearRegistry.getComponentID(component), obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPersist(Class<? extends GearComponent<T>> component) {
		return (T)get(GearRegistry.getComponentID(component));
	}
}
