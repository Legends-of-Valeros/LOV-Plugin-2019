package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class GiveCurrencyNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Currency")
    public IInportValue<Void, String> currency = new IInportValue<>(this, String.class, null);
    
    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        BankController.getBank(instance.getPlayerCharacter()).addCurrency(currency.get(instance), count.get(instance));

        onCompleted.run(instance);
    });
    
    public GiveCurrencyNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}