package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeRegistry {
    private Map<String, Class<? extends IQuestNode>> types = new HashMap<>();
    private static final HashMap<Object, Type> instanceType = new HashMap<>();

    public Optional<Class<? extends IQuestNode>> getType(String id) {
        return Optional.ofNullable(types.get(id));
    }

    public boolean hasType(String id) {
        return getType(id).isPresent();
    }

    public void addType(String id, Class<? extends IQuestNode> type) {
        try {
            for (Method method : type.getClass().getMethods()) {
                if (method.getAnnotation(QuestEvent.class) == null) continue;

                Class<?>[] params = method.getParameterTypes();
                if (params.length != 3) {
                    throw new IllegalArgumentException("@QuestEvent methods must have 3 arguments. IQuestInstance, generic T, and a supported Event.");
                }

                if (params[0] != IQuestInstance.class) {
                    throw new IllegalArgumentException("@QuestEvent method's first parameter must be IQuestInstance.");
                }

                if (!Event.class.isAssignableFrom(params[2])
                        || !QuestController.getInstance().getEventRegistry().hasHandler((Class<? extends Event>) params[2])) {
                    throw new IllegalArgumentException("@QuestEvent method's third parameter must be a supported Event.");
                }
            }

            types.put(id, type);

            instanceType.put(id, ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0]);
            instanceType.put(type, ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0]);
        } catch(Exception e) {
            MessageUtil.sendSevereException(QuestController.getInstance(), e);
        }
    }

    public static Type getInstanceType(Object key) {
        return instanceType.get(key);
    }
}