package com.legendsofvaleros.modules.bank.core;

import com.legendsofvaleros.modules.bank.event.PlayerCurrencyChangeEvent;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class Bank {
    public CharacterId characterId;

    private Map<String, Long> currencies = new HashMap<>();
    public Map<String, Long> getCurrencies() {
        return currencies;
    }

    private Map<Integer, Gear.Data> content = new HashMap<>();
    public Map<Integer, Gear.Data> getContent() {
        return content;
    }

    public long getCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, 0L);
        return currencies.get(currencyId);
    }

    public void setCurrency(String currencyId, long amount) {
        if(Characters.isPlayerCharacterLoaded(characterId)) {
            PlayerCurrencyChangeEvent bcce = new PlayerCurrencyChangeEvent(Characters.getPlayerCharacter(characterId), currencyId, amount);
            Bukkit.getPluginManager().callEvent(bcce);

            if(bcce.isCancelled()) return;
        }

        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, 0L);
        currencies.put(currencyId, amount);
    }

    public void addCurrency(String currencyId, long amount) {
        setCurrency(currencyId, getCurrency(currencyId) + amount);
    }

    public boolean subCurrency(String currencyId, long amount) {
        if (getCurrency(currencyId) < amount)
            return false;

        setCurrency(currencyId, getCurrency(currencyId) - amount);
        return true;
    }

    public void setItem(Gear.Data item) {
        int i = 0;
        for(Integer j : content.keySet())
            if(j > i) {
                setItem(i, item);
                return;
            }else
                i = j;
    }

    public void setItem(int index, Gear.Data item) {
        content.put(index, item);
    }

    public void removeItem(int i) {
        content.remove(i);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("{");
        currencies.forEach((key, value) -> str.append(key).append("=").append(value).append(","));
        str.setLength(str.length() - 1);
        str.append("}");
        return "Bank{character_id=" + characterId + ", currencies=" + str.toString() + "}";
    }
}
