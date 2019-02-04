package com.legendsofvaleros.modules.bank.core;

import com.codingforcookies.doris.orm.ORM;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.bank.event.PlayerCurrencyChangeEvent;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Bank extends ORM {
    private final PlayerCharacter pc;

    private Map<String, Currency> currencies = new HashMap<>();

    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }

    public Map<Integer, Bank.Entry> content = new HashMap<>();

    public Bank(PlayerCharacter pc) {
        this.pc = pc;
    }

    public long getCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, new Currency(pc.getUniqueCharacterId(), currencyId));
        return currencies.get(currencyId).amount;
    }

    public void setCurrency(String currencyId, long amount) {
        PlayerCurrencyChangeEvent bcce = new PlayerCurrencyChangeEvent(pc, currencyId, amount);
        Bukkit.getPluginManager().callEvent(bcce);

        if(bcce.isCancelled()) return;

        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, new Currency(pc.getUniqueCharacterId(), currencyId));
        currencies.get(currencyId).amount = amount;
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
        content.put(index, new Entry(pc.getUniqueCharacterId(), index, item));
    }

    public void removeItem(int i) {
        content.remove(i);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("{");
        currencies.forEach((key, value) -> str.append(key).append("=").append(value.amount).append(","));
        str.setLength(str.length() - 1);
        str.append("}");
        return "Bank{character_id=" + pc.getUniqueCharacterId() + ", currencies=" + str.toString() + "}";
    }

    @Table(name = "player_bank")
    public static class Currency {
        // @ForeignKey(table = PlayerCharacterData.class, name = "character_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
        @Column(name = "character_id", length = 39)
        private final CharacterId characterId;

        @Column(name = "currency_id", length = 16)
        private final String currencyId;

        public String getCurrencyId() {
            return currencyId;
        }

        @Column(name = "amount")
        public long amount = 0;

        public Currency(CharacterId characterId, String currencyId) {
            this.characterId = characterId;
            this.currencyId = currencyId;
        }
    }

    @Table(name = "player_bank_content")
    public static class Entry {
        // @ForeignKey(table = PlayerCharacterData.class, name = "character_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
        @Column(primary = true, index = true, name = "character_id", length = 39)
        private final String characterId;

        @Column(name = "bank_index")
        public final int index;

        @Column(name = "bank_item")
        public final Gear.Data item;

        protected Entry(CharacterId characterId, int index, Gear.Data item) {
            this.characterId = characterId.toString();
            this.index = index;
            this.item = item;
        }
    }
}
