package com.legendsofvaleros.modules.gear.component.impl;

import com.legendsofvaleros.modules.gear.GearRegistry;

import java.util.HashMap;

public class ComponentMap extends HashMap<String, GearComponent<?>> {
	private static final long serialVersionUID = 1L;

	public <T extends GearComponent<?>> T getComponent(Class<T> component) {
		if(!containsKey(GearRegistry.getComponentID(component))) return null;
		return component.cast(get(GearRegistry.getComponentID(component)));
	}

	@Override
	public String toString() {
		return "ComponentMap(length=" + size() + ")";
	}
}
