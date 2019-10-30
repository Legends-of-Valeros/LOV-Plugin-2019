package com.legendsofvaleros.modules.quests.core;

import java.util.HashMap;

public class QuestLogMap extends HashMap<Integer, QuestLogEntry> {
    transient int highestEntry = 0;

    @Override
    public QuestLogEntry put(Integer k, QuestLogEntry v) {
        if(k > highestEntry)
            highestEntry = k;

        return super.put(k, v);
    }

    @Override
    public void clear() {
        this.highestEntry = 0;

        super.clear();
    }

    public int add(QuestLogEntry entry) {
        int id = highestEntry + 1;
        this.put(id, entry);
        return id;
    }
}