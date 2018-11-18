package com.legendsofvaleros.modules.bank;

public abstract class Currency {
    public abstract String getDisplay(long amount);

    public abstract String getName();
}