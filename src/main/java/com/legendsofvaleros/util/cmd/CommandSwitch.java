package com.legendsofvaleros.util.cmd;

import java.util.List;

public abstract class CommandSwitch<T> {
    public abstract String getDescription();

    public abstract T getDefault();

    public String getArgumentLabel() { return null; }

    public T doParse(String arg) { return null; }

    public List<String> onTabComplete() { return null; }
}