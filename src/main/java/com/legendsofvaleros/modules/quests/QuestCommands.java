package com.legendsofvaleros.modules.quests;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

@CommandAlias("quests|lov quests")
public class QuestCommands extends BaseCommand {
    @Subcommand("reload")
    @Description("Reload quests currently in the cache.")
    @CommandPermission("quests.reload")
    public void cmdReload(CommandSender sender) throws Throwable {
        QuestController.getInstance().reloadQuests();

        MessageUtil.sendUpdate(sender, "Quests reloaded.");
    }

    @Subcommand("complete")
    @Description("Complete a gear. Using * will target all quests.")
    @CommandPermission("quests.complete")
    public void cmdComplete(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        QuestController.getInstance().getQuest(questId).onSuccess(val -> {
            if(!val.isPresent()) {
                MessageUtil.sendUpdate(player, "Unknown quest.");
                return;
            }

            QuestController.getInstance().finishQuest(val.get(), pc);
            MessageUtil.sendUpdate(player, "Quest completed.");
        });
    }

    @Subcommand("uncomplete")
    @Description("Remove a completed gear. Using * will target all quests.")
    @CommandPermission("quests.uncomplete")
    public void cmdUncomplete(Player player, String questId) {
        if(!LegendsOfValeros.getMode().allowEditing()) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        QuestController.getInstance().removeQuestProgress(questId, pc);
        MessageUtil.sendUpdate(player, "Quest uncompleted.");
    }

    @Subcommand("talk")
    @Private
    public void cmdQuestTalk(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();
        QuestController.getInstance().attemptGiveQuest(pc, questId);
    }

    @Subcommand("accept")
    @Private
    public void cmdQuestAccept(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        QuestController.getInstance().getQuest(questId).onSuccess(val -> {
            if(!val.isPresent()) {
                MessageUtil.sendUpdate(player, "Unknown quest.");
                return;
            }

            IQuest quest = val.get();
            quest.onAccept(pc);

            MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' accepted!");
        });
    }

    @Subcommand("decline")
    @Private
    public void cmdQuestDecline(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        QuestController.getInstance().getQuest(questId).onSuccess(val -> {
            if(!val.isPresent()) {
                MessageUtil.sendUpdate(player, "Unknown quest.");
                return;
            }

            val.get().onDecline(pc);

            MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' declined!");
        });
    }

    @Subcommand("close")
    @Private
    public void cmdQuestClose(Player player) {
        player.closeInventory();
    }

    @Subcommand("active")
    public void cmdQuestActive(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        ActiveTracker.setActive(pc, questId);

        ActiveTracker.getActiveQuest(pc).onSuccess(val -> {
            if (!val.isPresent())
                MessageUtil.sendUpdate(pc.getPlayer(), "You are no longer tracking a gear.");
            else
                MessageUtil.sendUpdate(pc.getPlayer(), "You are now tracking '" + val.get().getName() + "'.");
        }, QuestController.getInstance().getScheduler()::async);
    }

    @Subcommand("gui")
    @Description("Show the quest book.")
    public void cmdListQuestGUI(Player player) {
        if (!Characters.isPlayerCharacterLoaded(player)) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        String activeId = ActiveTracker.getActive(pc);

        Collection<IQuest> quests = QuestController.getInstance().getPlayerQuests(pc);

        Book book = new Book("Quest Journal", "Acolyte");

        book.addPage(
                new TextBuilder("\n\n\n" + StringUtil.center(Book.WIDTH, "Quest Journal")).color(ChatColor.BLACK)
                        .append("\n\n\n" + StringUtil.center(Book.WIDTH, "Current Quests:") + "\n").color(ChatColor.DARK_AQUA)
                        .append(StringUtil.center(Book.WIDTH, quests.size() + "") + "\n").color(ChatColor.BLACK)
                        .append(StringUtil.center(Book.WIDTH, "Completed Quests:") + "\n").color(ChatColor.DARK_PURPLE)
                        .append(StringUtil.center(Book.WIDTH, QuestController.getInstance().completedQuests.row(pc.getUniqueCharacterId()).size() + "")).color(ChatColor.BLACK)
                        .create()
        );

        TextBuilder tb;

        for (IQuest quest : quests) {
            tb = new TextBuilder("");

            tb.append(StringUtil.center(Book.WIDTH, quest.getId().equals(activeId) ? "[ " + quest.getName() + " ]" : quest.getName()) + "\n").color(ChatColor.BLACK).underlined(true)
                    .command("/quests active " + quest.getId());

            if (quest.getDescription().length() > 0)
                tb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextBuilder(String.join("\n",
                                StringUtil.splitForStackLore(ChatColor.translateAlternateColorCodes('&', quest.getDescription()))
                            )).create()));

            tb.append("\nObjectives:\n").color(ChatColor.DARK_GRAY);

            Integer currentI = quest.getObjectiveGroupI(pc);
            if (currentI == null) {
                tb.append("*No objectives, yet\n").color(ChatColor.DARK_RED);
            } else
                for (int i = currentI; i >= 0; i--) {
                    IQuestObjective<?>[] objs = quest.getObjectives().groups[i];
                    if (objs.length == 0)
                        tb.append("*An error occurred\n").color(ChatColor.DARK_RED);
                    else
                        for (IQuestObjective<?> obj : objs) {
                            try {
                                boolean completed = currentI != i || obj.isCompleted(pc);
                                if (obj.isVisible()) {
                                    tb.append("*" + (completed ? obj.getCompletedText(pc) : obj.getProgressText(pc)) + "\n").color(ChatColor.BLACK);
                                    if (completed) tb.strikethrough(true);
                                }
                            } catch (Exception e) {
                                MessageUtil.sendException(QuestController.getInstance(), player, e);
                                tb.append("*Plugin error\n").color(ChatColor.DARK_RED);
                            }
                        }
                    if (i != 0 && currentI != 0)
                        tb.append(StringUtil.center(Book.WIDTH, "------") + "\n").color(ChatColor.DARK_GRAY);
                }

            book.addPage(tb.create());
        }

        book.open(player, false);
    }

    @Default
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}