package com.legendsofvaleros.modules.quests;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import com.legendsofvaleros.util.cmd.CommandManager;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class QuestCommands {
    @CommandManager.Cmd(cmd = "quests refresh", help = "Refresh quests currently in the cache.", permission = "quest.refresh")
    public static CommandManager.CommandFinished cmdRefresh(CommandSender sender, Object[] args) {
        QuestManager.reloadQuests();

        MessageUtil.sendUpdate(sender, "Quests refreshed.");
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests uncomplete", args = "<name>", help = "Remove a completed quest. Using * will target all quests.", permission = "quest.uncomplete", only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdUncomplete(CommandSender sender, Object[] args) {
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);

        QuestManager.removeQuestProgress(String.valueOf(args[0]), pc);
        MessageUtil.sendUpdate(sender, "Quest uncompleted.");
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests talk", args = "<name>", showInHelp = false, only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdQuestTalk(CommandSender sender, Object[] args) {
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);

        ((Player) sender).closeInventory();
        Quests.attemptGiveQuest(pc, (String) args[0]);
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests accept", args = "<name>", showInHelp = false, only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdQuestAccept(CommandSender sender, Object[] args) {
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);

        ((Player) sender).closeInventory();
        ListenableFuture<IQuest> future = QuestManager.getQuest(String.valueOf(args[0]));
        future.addListener(() -> {
            try {
                IQuest quest = future.get();

                QuestStatus status = QuestManager.getStatus(pc, quest);

                QuestManager.removeQuestProgress(quest.getId(), pc);

                QuestManager.addPlayerQuest(pc, quest);

                quest.onAccept(pc);
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), sender, e, true);
            }
        }, Utilities.asyncExecutor());
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests decline", args = "<name>", showInHelp = false, only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdQuestDecline(CommandSender sender, Object[] args) {
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);

        ((Player) sender).closeInventory();
        ListenableFuture<IQuest> future = QuestManager.getQuest(String.valueOf(args[0]));
        future.addListener(() -> {
            try {
                future.get().onDecline(pc);
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), sender, e, true);
            }
        }, Utilities.asyncExecutor());
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests close", only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdQuestClose(CommandSender sender, Object[] args) {
        ((Player) sender).closeInventory();
        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests active", args = "<name>", showInHelp = false, only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdQuestActive(CommandSender sender, Object[] args) {
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);
        ActiveTracker.setActive(pc, String.valueOf(args[0]));

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

        return CommandManager.CommandFinished.DONE;
    }

    @CommandManager.Cmd(cmd = "quests gui", help = "Show quest stuff.", showInHelp = false, only = CommandManager.CommandOnly.PLAYER)
    public static CommandManager.CommandFinished cmdListQuestGUI(CommandSender sender, Object[] args) {
        if (!Characters.isPlayerCharacterLoaded((Player) sender)) return CommandManager.CommandFinished.DONE;
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) sender);

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
                    .command("/lov quests active " + quest.getId());

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
                                MessageUtil.sendException(LegendsOfValeros.getInstance(), sender, e, true);
                                fm.then("*Plugin error\n").color(ChatColor.DARK_RED);
                            }
                        }
                    if (i != 0 && currentI != 0)
                        fm.then(StringUtil.center(Book.WIDTH, "------") + "\n").color(ChatColor.DARK_GRAY);
                }

            book.addPage(fm);
        }

        book.open((Player) sender, false);
        return CommandManager.CommandFinished.DONE;
    }
}