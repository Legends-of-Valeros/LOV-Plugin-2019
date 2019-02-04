package com.legendsofvaleros.modules.gear;

import com.legendsofvaleros.modules.gear.component.GearComponent;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

public class GearRegistry {
	protected static final HashMap<String, Class<? extends GearComponent<?>>> components = new HashMap<>();
	private static final HashMap<Class<? extends GearComponent<?>>, String> componentID = new HashMap<>();
	private static final HashMap<Object, Type> componentPersist = new HashMap<>();

	public static Class<? extends GearComponent<?>> getComponent(String id) {
		return components.get(id);
	}

	public static String getComponentID(Class<? extends GearComponent<?>> component) {
		return componentID.get(component);
	}

	public static <T extends GearComponent<?>> T getComponent(Class<T> component) {
		return component.cast(components.get(getComponentID(component)));
	}

	public static Type getPersist(Object key) {
		return componentPersist.get(key);
	}
	
	public static void registerComponent(String id, Class<? extends GearComponent<?>> component) {
		if(components.containsKey(id))
			throw new RuntimeException("An attempt was made to register a component with an existing name.");
		
		// Both the name and the component are registered so that either can be used to fetch the data
		components.put(id, component);
		componentID.put(component, id);

		componentPersist.put(id, ((ParameterizedType)component.getGenericSuperclass()).getActualTypeArguments()[0]);
		componentPersist.put(component, ((ParameterizedType)component.getGenericSuperclass()).getActualTypeArguments()[0]);
	}
}