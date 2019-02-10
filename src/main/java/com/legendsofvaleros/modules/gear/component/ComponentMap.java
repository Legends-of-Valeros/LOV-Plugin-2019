package com.legendsofvaleros.modules.gear.component;

import com.legendsofvaleros.modules.gear.GearRegistry;

import java.util.HashMap;

public class ComponentMap extends HashMap<String, GearComponent<?>> {
	private static final long serialVersionUID = 1L;

	public <T extends GearComponent<?>> T getComponent(Class<T> component) {
		if(!containsKey(GearRegistry.getComponentId(component))) return null;
		return component.cast(get(GearRegistry.getComponentId(component)));
	}

	@Override
	public String toString() {
		return "ComponentMap(length=" + size() + ")";
	}
}
