package com.legendsofvaleros.modules.auction;

import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

/**
 * Respresent a chat intstance
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
        //TODO MessageUtil.sendInfo ?
        playerCharacter.getPlayer().sendMessage(divider);
        playerCharacter.getPlayer().sendMessage("You started " + prompt.getChatString() + " " + auction.getItem().toInstance().gear.getName());
    }

    public void finish() {
        AuctionController.getInstance().removePrompt(playerCharacter.getUniqueCharacterId());
        getPlayerCharacter().getPlayer().sendMessage(divider);
    }

    private void cancelPrompt() {
        isCancelled = true;
        AuctionController.getInstance().removePrompt(playerCharacter.getUniqueCharacterId());

        playerCharacter.getPlayer().sendMessage("You Stopped " + prompt.getChatString() + " " + auction.getItem().toInstance().gear.getName());
        getPlayerCharacter().getPlayer().sendMessage(divider);
    }

    private void handleDecision() {
        this.handleDecision("");
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
            handleDecision();
        }
    }

    private boolean handleBidSteps(String decision) {
        if (currentStep == 1) {
            playerCharacter.getPlayer().sendMessage(
                    "The current bid price is " + Money.Format.format(auction.getPrice()) + ". How much do you want to bid?"
            );
        } else if (currentStep == 2) {
            try {
                int price = Integer.parseInt(decision);
                auction.bid(playerCharacter.getUniqueCharacterId(), price);
                return true;
            } catch (NumberFormatException e) {
                playerCharacter.getPlayer().sendMessage("Please enter a valid number");
                cancelPrompt();
            }
        }
        return false;
    }

    private boolean handleBuySteps(String decision) {
        if (currentStep == 1) {
            if (auction.getItem().amount > 0) {
                playerCharacter.getPlayer().sendMessage("How many do you want to buy?");
            } else {
                playerCharacter.getPlayer().sendMessage("Do you really want to buy " + auction.getItem().toInstance().gear.getName() + " ?");
            }
        } else if (currentStep == 2) {
            if (decision.equalsIgnoreCase("yes") || decision.equalsIgnoreCase("y")) {
                AuctionController.getInstance().confirmBuyPrompt(playerCharacter.getUniqueCharacterId());
                return true;
            }
        }
        return false;
    }

    private boolean handleSellSteps(String decision) {
        switch (currentStep) {
            case 1:
                playerCharacter.getPlayer().sendMessage("Do you want to sell or auction the item?");
                break;
            case 2:
                auction.setBidOffer(decision.equalsIgnoreCase("auction"));
                break;
            case 3:
                if (auction.isBidOffer()) {
                    playerCharacter.getPlayer().sendMessage("Please enter a minimum bid?");
                } else {
                    playerCharacter.getPlayer().sendMessage(
                            "How much should 1x " + auction.getItem().toInstance().gear.getName() + " cost?"
                    );
                }
                break;
            case 4:
                try {
                    int price = Integer.parseInt(decision);
                    auction.setPrice(price);
                    return true;
                } catch (NumberFormatException e) {
                    playerCharacter.getPlayer().sendMessage("Please enter a valid number");
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
        BUY("buying", 3),
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
