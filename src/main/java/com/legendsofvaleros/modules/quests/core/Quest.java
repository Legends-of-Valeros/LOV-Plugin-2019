package com.legendsofvaleros.modules.quests.core;

import com.google.common.collect.HashBasedTable;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.*;
import org.bukkit.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Quest implements IQuest {
    private final Map<CharacterId, IQuestInstance> instances = new HashMap<>();
    private int instancesActive = 0;

    @SerializedName("_id")
    private final String id;
    private final String slug;

    private final String name;
    private final String description;
    private final boolean forced;
    private final IQuestPrerequisite[] prerequisites;
    private final Object repeat;

    private final QuestNodeMap nodes;

    private final HashBasedTable<Class<? extends Event>, IQuestNode, Method> events = HashBasedTable.create();

    public Quest(String id,
                 String slug,
                 String name,
                 String description,
                 boolean forced,
                 IQuestPrerequisite[] prerequisites,
                 Object repeat,
                 QuestNodeMap nodes) {
        this.id = id;
        this.slug = slug;

        this.name = name;
        this.description = description;

        this.forced = forced;
        this.prerequisites = prerequisites;
        this.repeat = repeat;
        this.nodes = nodes;

        // Build the node event list
        for (IQuestNode node : nodes.values()) {
            // Loop through all methods and find ones annotated with @QuestEvent
            for (Method method : node.getClass().getMethods()) {
                if (method.getAnnotation(QuestEvent.class) == null) continue;

                Class<?>[] params = method.getParameterTypes();
                if (params.length != 3) {
                    throw new IllegalArgumentException("@QuestEvent methods must have 3 arguments. IQuestInstance, generic T, and a supported Event.");
                }

                if (params[0] != IQuestInstance.class) {
                    throw new IllegalArgumentException("@QuestEvent method's first parameter must be IQuestInstance.");
                }

                if (Event.class.isAssignableFrom(params[2])
                        || !QuestController.getInstance().getEventRegistry().hasHandler((Class<? extends Event>) params[2])) {
                    throw new IllegalArgumentException("@QuestEvent method's third parameter must be a supported Event.");
                }

                Class<? extends Event> event = (Class<? extends Event>) params[2];

                events.put(event, node, method);
            }
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getSlug() {
        return this.slug;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean isForced() {
        return this.forced;
    }

    @Override
    public IQuestPrerequisite[] getPrerequisites() {
        return this.prerequisites;
    }

    @Override
    public Object getRepeatOptions() {
        return this.repeat;
    }

    @Override
    public Optional<IQuestNode> getNode(String id) {
        return Optional.ofNullable(this.nodes.get(id));
    }

    @Override
    public Collection<IQuestNode> getNodes() {
        return nodes.values();
    }

    @Override
    public Map<IQuestNode, Method> getListeners(Class<? extends Event> caught) {
        return events.row(caught);
    }

    /**
     * This will always return NONE if the player is offline.
     */
    @Override
    public QuestStatus getStatus(PlayerCharacter pc) {
        IQuestInstance instance = instances.get(pc.getUniqueCharacterId());

        if (instance != null) {
            // If the player is currently running the quest
            if (instance.getState().isActive())
                return QuestStatus.ACTIVE;

            if(instance.getState().wasCompleted()) {
                // If the quest is not repeatable, then just return NONE
                if (repeat == null) {
                    return QuestStatus.ENDED;
                } else {
                    // Repeat options exist, so check them
                    // TODO
                    return QuestStatus.NOT_READY;
                }
            }
        }

        // Check prerequisites
        for (IQuestPrerequisite prerequisite : prerequisites) {
            if (!prerequisite.canAccept(this, pc)) {
                return QuestStatus.PREREQUISITE_FAIL;
            }
        }

        // Quest can be accepted! Yay!
        return QuestStatus.READY;
    }

    @Override
    public IQuestInstance getInstance(PlayerCharacter pc) {
        if(!instances.containsKey(pc.getUniqueCharacterId()))
            return new QuestInstance(pc, this);
        return instances.get(pc.getUniqueCharacterId());
    }

    @Override
    public void setInstance(CharacterId characterId, IQuestInstance instance) {
        instances.put(characterId, instance);

        // If the instance is currently active, fire the activate function.
        if(instance.getState() == QuestState.ACTIVE)
            this.onActivated(instance);
    }

    @Override
    public Optional<IQuestInstance> removeInstance(CharacterId characterId) {
        if(!instances.containsKey(characterId)) return Optional.empty();

        IQuestInstance instance = instances.get(characterId);

        // If the instance is currently active, fire the deactivate function.
        if(instance.getState() == QuestState.ACTIVE)
            this.onDeactivated(instance);

        instances.remove(characterId);

        return Optional.of(instance);
    }

    @Override
    public void onActivated(IQuestInstance instance) {
        if(instancesActive == 0) {
            nodes.values().stream().forEach(IQuestNode::onWake);
        }

        nodes.values().stream().forEach(node -> node.onActivated(instance, instance.getNodeInstance(node)));

        instancesActive++;
    }

    @Override
    public void onDeactivated(IQuestInstance instance) {
        nodes.values().stream().forEach(node -> node.onDeactivated(instance, instance.getNodeInstance(node)));

        instancesActive--;

        if(instancesActive == 0) {
            nodes.values().stream().forEach(IQuestNode::onSleep);
        }
    }

    @Override
    public void callEvent(IQuestInstance instance, Class<? extends Event> caught, Event event) {
        if (instance.getState() != QuestState.ACTIVE) {
            return;
        }

        Map<IQuestNode, Method> listeners = getListeners(caught);

        // Fire the event in every node that's listening
        listeners.forEach((node, method) -> {
            try {
                method.invoke(node, this, instance.getNodeInstance(node), event);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}