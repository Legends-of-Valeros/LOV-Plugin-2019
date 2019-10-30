package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class GiveCurrencyNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Currency")
    public IInportObject<Void, String> currency = IInportValue.of(this, String.class, null);
    
    @SerializedName("Count")
    public IInportObject<Void, Integer> count = IInportValue.of(this, Integer.class, 0);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.of(this, (instance, data) -> {
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