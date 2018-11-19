package com.legendsofvaleros.modules.bank;

import com.codingforcookies.robert.core.RobertStack;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.pouch.CreatePouchGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.event.InventoryFullEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

public class Money {
    public static final String ID = "copper";

    public static long get(PlayerCharacter pc) {
        return Bank.getBank(pc).getCurrency(ID);
    }

    public static void add(PlayerCharacter pc, long c) {
        pc.getPlayer().playSound(pc.getLocation(), "ui.coins.accquire", 1F, 1F);

        Bank.getBank(pc).addCurrency(ID, c);
    }

    public static boolean sub(PlayerCharacter pc, long c) {
        pc.getPlayer().playSound(pc.getLocation(), "ui.coins.accquire", 1F, 1F);

        return Bank.getBank(pc).subCurrency(ID, c);
    }

    public static void onEnable() {
        Bank.registerCurrency(ID, new Currency() {
            @Override
            public String getName() {
                return "Money";
            }

            @Override
            public String getDisplay(long amount) {
                return Money.Format.format(amount);
            }
        });

        InventoryManager.addFixedItem(17, new InventoryManager.InventoryItem(new ItemBuilder(Material.GOLD_INGOT).setName(null).create(), (p, event) -> {
            if(!Characters.isPlayerCharacterLoaded(p)) return;
            if(RobertStack.top(p) instanceof CreatePouchGUI) return;

            PlayerCharacter pc = Characters.getPlayerCharacter(p);

            if(event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                GearItem.Instance instance = GearItem.Instance.fromStack(event.getView().getCursor());
                if(instance == null) return;

//                Long worth = instance.getPersist(WorthComponent.class);
//                if(worth == null) return;
//
//                Money.add(pc, worth);
//                event.getView().setCursor(null);

                return;
            }

            new CreatePouchGUI().open(p);
        }));
    }

    @EventHandler
    public void onInventoryFull(InventoryFullEvent event) {
        // event.setCancelled(true);

		/*ListenableFuture<BankData> future = getBank(event.getPlayerCharacter());
		future.addListener(() -> {
			try {
				BankData data = future.get();

				GearItem.Instance instance = event.getItem();

				data.content.add(instance.getData());

				MessageUtil.sendUpdate(event.getPlayer(), instance.amount + "x[" + instance.gear.getName() + ChatColor.AQUA + "] has been sent to your bank.");
			} catch (Exception e) {
				MessageUtil.sendException(Bank.this, event.getPlayer(), e, true);
			}
		}, Utilities.asyncExecutor());*/
    }

    public enum Format {
        GOLD('g', 10000, ChatColor.GOLD),
        SILVER('s', 100, ChatColor.GRAY),
        COPPER('c', 1, ChatColor.DARK_RED);

        char c;
        long copper;
        ChatColor color;

        Format(char c, long copper, ChatColor color) {
            this.c = c;
            this.copper = copper;
            this.color = color;
        }

        public static String format(long copper) {
            StringBuilder sb = new StringBuilder();

            if(copper == 0) {
                sb.append(ChatColor.BOLD);
                sb.append(COPPER.color);
                sb.append("❂");
                sb.append(ChatColor.WHITE);
                sb.append(" ");
                sb.append(0);
            }else
                for(Format c : values()) {
                    long amt = copper / c.copper;
                    if(amt > 0) {
                        copper -= amt * c.copper;

                        sb.append(" ");
                        sb.append(ChatColor.BOLD);
                        sb.append(c.color);
                        sb.append("❂");
                        sb.append(ChatColor.WHITE);
                        sb.append(" ");
                        sb.append(amt);
                    }
                }

            return sb.toString().trim();
        }
    }

    /*public static class Arg extends CommandArgument<Long> {
        @Override
        public String getDescription() {
            return "An amount of money. Ex: 5g2c";
        }

        @Override
        public Long doParse(String arg) {
            if(arg.length() == 1) {
                return Long.parseLong(arg);
            }

            int total = 0;

            String[] datas = arg.split("..");
            Long amt = null;

            base:
            for(String data : datas) {
                if(amt == null) {
                    amt = Long.parseLong(data);
                }else{
                    char c = data.charAt(0);
                    for(Format f : Format.values())
                        if(f.c == c) {
                            total += amt * f.copper;
                            continue base;
                        }

                    throw new IllegalArgumentException("Invalid currency type '" + c + "'.");
                }
            }

            return null;
        }
    }*/
}