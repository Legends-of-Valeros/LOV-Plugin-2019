package com.legendsofvaleros.modules.quests;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

@CommandAlias("quests|quest")
public class QuestCommands extends BaseCommand {
    @Subcommand("refresh")
    @Description("Refresh quests currently in the cache.")
    @CommandPermission("quests.refresh")
    public void cmdRefresh(CommandSender sender) {
        QuestManager.reloadQuests();

        MessageUtil.sendUpdate(sender, "Quests refreshed.");
    }

    @Subcommand("complete")
    @Description("Complete a quest. Using * will target all quests.")
    @CommandPermission("quests.complete")
    @Syntax("<quest id>")
    public void cmdComplete(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        ListenableFuture<IQuest> future = QuestManager.getQuest(id);
        future.addListener(() -> {
            try {
                IQuest quest = future.get();
                if(quest == null) {
                    MessageUtil.sendUpdate(player, "Unknown quest.");
                    return;
                }

                QuestManager.finishQuest(quest, pc);
                MessageUtil.sendUpdate(player, "Quest completed.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }, Utilities.syncExecutor());
    }

    @Subcommand("uncomplete")
    @Description("Remove a completed quest. Using * will target all quests.")
    @CommandPermission("quests.uncomplete")
    @Syntax("<quest id>")
    public void cmdUncomplete(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        QuestManager.removeQuestProgress(id, pc);
        MessageUtil.sendUpdate(player, "Quest uncompleted.");
    }

    @Subcommand("talk")
    @Syntax("<quest id>")
    private void cmdQuestTalk(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();
        Quests.attemptGiveQuest(pc, id);
    }

    @Subcommand("accept")
    @Syntax("<quest id>")
    private void cmdQuestAccept(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        ListenableFuture<IQuest> future = QuestManager.getQuest(id);
        future.addListener(() -> {
            try {
                IQuest quest = future.get();

                QuestManager.removeQuestProgress(quest.getId(), pc);

                QuestManager.addPlayerQuest(pc, quest);

                quest.onAccept(pc);
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), player, e, true);
            }
        }, Utilities.asyncExecutor());
    }

    @Subcommand("defline")
    @Syntax("<quest id>")
    private void cmdQuestDecline(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        ListenableFuture<IQuest> future = QuestManager.getQuest(id);
        future.addListener(() -> {
            try {
                future.get().onDecline(pc);
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), player, e, true);
            }
        }, Utilities.asyncExecutor());
    }

    @Subcommand("close")
    private void cmdQuestClose(Player player) {
        player.closeInventory();
    }

    @Subcommand("active")
    @Syntax("<quest id>")
    public void cmdQuestActive(Player player, String id) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        ActiveTracker.setActive(pc, id);

        ListenableFuture<IQuest> future = ActiveTracker.getActiveQuest(pc);
        future.addListener(() -> {
            try {
                IQuest active = future.get();
                if (active == null)
                    MessageUtil.sendUpdate(pc.getPlayer(), "You are no longer tracking a quest.");
                else
                    MessageUtil.sendUpdate(pc.getPlayer(), "You are now tracking '" + active.getName() + "'.");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, Utilities.asyncExecutor());
    }

    @Default
    @Subcommand("gui")
    @Description("Show the quest book.")
    public void cmdListQuestGUI(Player player) {
        if (!Characters.isPlayerCharacterLoaded(player)) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        String activeId = ActiveTracker.getActive(pc);

        Collection<IQuest> quests = QuestManager.getQuestsForEntity(pc);

        Book book = new Book("Quest Journal", "Acolyte");

        book.addPage(
                new FancyMessage("\n\n\n" + StringUtil.center(Book.WIDTH, "Quest Journal")).color(ChatColor.BLACK)
                        .then("\n\n\n" + StringUtil.center(Book.WIDTH, "Current Quests:") + "\n").color(ChatColor.DARK_AQUA)
                        .then(StringUtil.center(Book.WIDTH, quests.size() + "") + "\n").color(ChatColor.BLACK)
                        .then(StringUtil.center(Book.WIDTH, "Completed Quests:") + "\n").color(ChatColor.DARK_PURPLE)
                        .then(StringUtil.center(Book.WIDTH, QuestManager.completedQuests.row(pc.getUniqueCharacterId()).size() + "")).color(ChatColor.BLACK)
        );

        FancyMessage fm;

        for (IQuest quest : quests) {
            fm = new FancyMessage("");

            fm.then(StringUtil.center(Book.WIDTH, quest.getId().equals(activeId) ? "[ " + quest.getName() + " ]" : quest.getName()) + "\n").color(ChatColor.BLACK).style(ChatColor.UNDERLINE)
                    .command("/quests active " + quest.getId());

            if (quest.getDescription().length() > 0)
                fm.tooltip(StringUtil.splitForStackLore(ChatColor.translateAlternateColorCodes('&', quest.getDescription())));

            fm.then("\nObjectives:\n").color(ChatColor.DARK_GRAY);

            int currentI = quest.getCurrentGroupI(pc);
            if (currentI == -1) {
                fm.then("*No objectives, yet\n").color(ChatColor.DARK_RED);
            } else
                for (int i = currentI; i >= 0; i--) {
                    IObjective<?>[] objs = quest.getObjectives().getGroup(i);
                    if (objs.length == 0)
                        fm.then("*An error occurred\n").color(ChatColor.DARK_RED);
                    else
                        for (IObjective<?> obj : objs) {
                            try {
                                boolean completed = currentI != i || obj.isCompleted(pc);
                                if (obj.isVisible()) {
                                    fm.then("*" + (completed ? obj.getCompletedText(pc) : obj.getProgressText(pc)) + "\n").color(ChatColor.BLACK);
                                    if (completed) fm.style(ChatColor.STRIKETHROUGH);
                                }
                            } catch (Exception e) {
                                MessageUtil.sendException(Quests.getInstance(), player, e, true);
                                fm.then("*Plugin error\n").color(ChatColor.DARK_RED);
                            }
                        }
                    if (i != 0 && currentI != 0)
                        fm.then(StringUtil.center(Book.WIDTH, "------") + "\n").color(ChatColor.DARK_GRAY);
                }

            book.addPage(fm);
        }

        book.open(player, false);
    }

    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}