package com.legendsofvaleros.modules.bank;

import com.codingforcookies.doris.orm.ORM;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;

import java.util.*;

public class PlayerBank extends ORM {
    private final CharacterId characterId;

    private Map<String, Currency> currencies = new HashMap<>();

    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }

    public List<PlayerBank.Entry> content = new ArrayList<>();

    public PlayerBank(CharacterId characterId) {
        this.characterId = characterId;
    }

    public long getCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, new Currency(characterId, currencyId));
        return currencies.get(currencyId).amount;
    }

    public void setCurrency(String currencyId, long amount) {
        if (!currencies.containsKey(currencyId))
            currencies.put(currencyId, new Currency(characterId, currencyId));
        currencies.get(currencyId).amount = amount;

        Bank.updateInv(Characters.inst().getCharacter(characterId));
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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("{");
        currencies.forEach((key, value) -> str.append(key).append("=").append(value.amount).append(","));
        str.setLength(str.length() - 1);
        str.append("}");
        return "Bank{character_id=" + characterId + ", currencies=" + str.toString() + "}";
    }

    @Table(name = "player_bank")
    public static class Currency {
        // TODO: @ForeignKey(table = BankData.class, name = "character_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
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
        // TODO: @ForeignKey(table = BankData.class, name = "character_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
        @Column(primary = true, index = true, name = "character_id", length = 39)
        private String characterId;

        @Column(name = "bank_entry")
        public GearItem.Data entry;

        public Entry(CharacterId characterId, GearItem.Data entry) {
            this.characterId = characterId.toString();
            this.entry = entry;
        }
    }
}
