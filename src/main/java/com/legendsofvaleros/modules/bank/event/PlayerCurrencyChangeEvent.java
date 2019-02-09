package com.legendsofvaleros.modules.bank.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerCurrencyChangeEvent extends PlayerCharacterEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    private boolean cancelled = false;
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { cancelled = cancel; }

    private final String currencyId;
    public String getCurrencyId() { return currencyId; }

    private long change;
    public long getChange() { return change; }
    public void setChange(long change) { this.change = change; }

    public PlayerCurrencyChangeEvent(PlayerCharacter pc, String currencyId, long change) {
        super(pc);

        this.currencyId = currencyId;
        this.change = change;
    }
}
