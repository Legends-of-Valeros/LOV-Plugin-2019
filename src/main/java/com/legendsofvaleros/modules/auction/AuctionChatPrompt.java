package com.legendsofvaleros.modules.auction;

import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Respresents an auction chat prompt instance
 * Created by Crystall on 12/11/2018
 */
public class AuctionChatPrompt {
    private PlayerCharacter playerCharacter;
    private Auction auction;
    private int currentStep = 1;
    private AuctionPromptType prompt;
    private boolean isCancelled = false;

    private static final String divider = "------------------------------------";

    AuctionChatPrompt(PlayerCharacter player, Auction auction, AuctionPromptType prompt) {
        this.playerCharacter = player;
        this.auction = auction;
        this.prompt = prompt;
        //TODO MessageUtil.sendInfo
        playerCharacter.getPlayer().sendMessage(ChatColor.BOLD + divider);
        MessageUtil.sendInfo(playerCharacter.getPlayer(), "You started " + prompt.getChatString() + " " + auction.getItem().toInstance().gear.getName());
        this.handleDecision("");
    }

    public void finish() {
        AuctionController.getInstance().removePrompt(playerCharacter.getUniqueCharacterId());
        getPlayerCharacter().getPlayer().sendMessage(ChatColor.BOLD + divider);
    }

    public void cancelPrompt() {
        isCancelled = true;
        AuctionController.getInstance().removePrompt(playerCharacter.getUniqueCharacterId());

        //readd the item to the player inventory if he is trying to sell stuff
        if (prompt == AuctionPromptType.SELL) {
            playerCharacter.getPlayer().getInventory().addItem(getAuction().getItem().toStack());
        }

        MessageUtil.sendInfo(playerCharacter.getPlayer(), "You Stopped " + prompt.getChatString() + " " + auction.getItem().toInstance().gear.getName());
        getPlayerCharacter().getPlayer().sendMessage(ChatColor.BOLD + divider);
    }

    public void handleDecision(String decision) {
        if (decision.equalsIgnoreCase("cancel")) {
            cancelPrompt();
            return;
        }
        boolean rerun = false;
        switch (prompt) {
            case BID:
                rerun = handleBidSteps(decision);
                break;
            case BUY:
                rerun = handleBuySteps(decision);
                break;
            case SELL:
                rerun = handleSellSteps(decision);
                break;
        }

        currentStep++;
        if (!isCancelled && currentStep > getPrompt().maxSteps) {
            this.finish();
        }
        if (rerun) {
            this.handleDecision("");
        }
    }

    private boolean handleBidSteps(String decision) {
        if (currentStep == 1) {
            MessageUtil.sendInfo(playerCharacter.getPlayer(),
                    "The current bid price is " + auction.getPriceFormatted() + ". How much do you want to bid?"
            );
        } else if (currentStep == 2) {
            try {
                int price = Integer.parseInt(decision);
                if (!auction.bid(playerCharacter.getUniqueCharacterId(), price)) {
                    cancelPrompt();
                    return false;
                }

                AuctionController.getInstance().confirmBidPrompt(playerCharacter.getUniqueCharacterId());
                MessageUtil.sendInfo(playerCharacter.getPlayer(),
                        "You successfully bid " + Money.Format.format(price) + " on " + auction.getItem().toInstance().gear.getName()
                );
            } catch (NumberFormatException e) {
                MessageUtil.sendError(playerCharacter.getPlayer(), "Please enter a valid number");
                cancelPrompt();
            }
        }
        return false;
    }

    private boolean handleBuySteps(String decision) {
        if (currentStep == 1) {
            if (auction.getItem().amount > 1) {
                MessageUtil.sendInfo(playerCharacter.getPlayer(), "How many " + auction.getItem().toInstance().gear.getName() + " do you want to buy?");
            } else {
                MessageUtil.sendInfo(playerCharacter.getPlayer(),
                        "Do you really want to buy " + auction.getItem().toInstance().gear.getName() + " for " + auction.getPriceFormatted() + " ?"
                );
            }
        } else if (currentStep == 2) {
            if (auction.getItem().amount > 1) {
                try {
                    int amount = Integer.parseInt(decision);
                    if (amount > auction.getItem().amount) {
                        MessageUtil.sendError(playerCharacter.getPlayer(),
                                "You are trying to buy more items than the auction has. The maximum amount is " + auction.getItem().amount
                        );
                        cancelPrompt();
                        return false;
                    }
                    //buying the items failed
                    if (!auction.buy(playerCharacter.getUniqueCharacterId(), amount)) {
                        cancelPrompt();
                        return false;
                    }
                    AuctionController.getInstance().confirmBuyPrompt(playerCharacter.getUniqueCharacterId(), amount);
                    MessageUtil.sendInfo(playerCharacter.getPlayer(), "You successfully bought " + amount + "x of " + auction.getItem().toInstance().gear.getName());
                } catch (NumberFormatException e) {
                    MessageUtil.sendError(playerCharacter.getPlayer(), "Please enter a valid number");
                    cancelPrompt();
                    return false;
                }
            } else {
                if (decision.equalsIgnoreCase("yes") || decision.equalsIgnoreCase("y")) {
                    if (!auction.buy(playerCharacter.getUniqueCharacterId(), 1)) {
                        cancelPrompt();
                        return false;
                    }
                    AuctionController.getInstance().confirmBuyPrompt(playerCharacter.getUniqueCharacterId(), 1);
                    MessageUtil.sendInfo(playerCharacter.getPlayer(), "You successfuly bought " + auction.getItem().toInstance().gear.getName());
                } else {
                    cancelPrompt();
                }
            }
        }
        return false;
    }

    private boolean handleSellSteps(String decision) {
        switch (currentStep) {
            case 1:
                MessageUtil.sendInfo(playerCharacter.getPlayer(), "Do you want to sell or auction the item?");
                break;
            case 2:
                if (decision.equalsIgnoreCase("auction")) {
                    auction.setBidOffer(true);
                    //set the auction to run out in 48h
                    Date d = new Date(System.currentTimeMillis());
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    c.add(Calendar.DATE, 2);
                    auction.setValidUntil(TimeUnit.MILLISECONDS.toSeconds(c.getTimeInMillis()));
                }
                auction.setBidOffer(decision.equalsIgnoreCase("auction"));
                return true;
            case 3:
                if (auction.isBidOffer()) {
                    MessageUtil.sendInfo(playerCharacter.getPlayer(), "Please enter a minimum bid");
                } else {
                    MessageUtil.sendInfo(playerCharacter.getPlayer(),
                            "How much should 1x " + auction.getItem().toInstance().gear.getName() + " cost?"
                    );
                }
                break;
            case 4:
                try {
                    int price = Integer.parseInt(decision);
                    auction.setPrice(price);
                    MessageUtil.sendInfo(playerCharacter.getPlayer(),
                            "You successfully started selling " + auction.getItem().toInstance().gear.getName() + " for " + auction.getPriceFormatted()
                    );
                    return true;
                } catch (NumberFormatException e) {
                    MessageUtil.sendError(playerCharacter.getPlayer(), "Please enter a valid number");
                    cancelPrompt();
                }
                break;
            case 5:
                AuctionController.getInstance().confirmSellPrompt(playerCharacter.getUniqueCharacterId());
                break;
        }
        return false;
    }

    public Auction getAuction() {
        return auction;
    }

    public PlayerCharacter getPlayerCharacter() {
        return playerCharacter;
    }

    public AuctionPromptType getPrompt() {
        return prompt;
    }

    public enum AuctionPromptType {
        BUY("buying", 2),
        SELL("selling", 5),
        BID("bidding on", 2);

        private String chatString;
        private int maxSteps;

        AuctionPromptType(String chatString, int maxSteps) {
            this.chatString = chatString;
            this.maxSteps = maxSteps;
        }

        public String getChatString() {
            return chatString;
        }
    }
}
