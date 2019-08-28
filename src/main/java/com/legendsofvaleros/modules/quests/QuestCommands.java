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
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestState;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
    @Description("Complete a quest. Using * will target all quests.")
    @CommandPermission("quests.complete")
    public void cmdComplete(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        QuestController.getInstance().getQuestBySlug(questId).onSuccess(val -> {
            if(!val.isPresent()) {
                MessageUtil.sendUpdate(player, "Unknown quest.");
                return;
            }

            QuestController.getInstance().completeQuest(val.get(), pc);
            MessageUtil.sendUpdate(player, "Quest completed.");
        });
    }

    @Subcommand("delete")
    @Description("Remove a quest instance from a player complete. Using * will target all quests.")
    @CommandPermission("quests.delete")
    public void cmdUncomplete(Player player, String questId) {
        if(!LegendsOfValeros.getMode().allowEditing()) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        QuestController.getInstance().getQuestBySlug(questId).onSuccess(val -> {
            QuestController.getInstance().removeQuestProgress(val.get(), pc);
            MessageUtil.sendUpdate(player, "Quest uncompleted.");
        });
    }

    @Subcommand("info")
    @Private
    public void cmdQuestInfo(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        QuestController.getInstance().getQuest(questId).onSuccess(val -> {
            QuestController.getInstance().getScheduler().sync(() -> {
                QuestController.getInstance().startQuest(val.get(), pc);
            });
        });
    }

    @Subcommand("start")
    @Private
    public void cmdQuestStart(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        player.closeInventory();

        QuestController.getInstance().getQuestBySlug(questId).onSuccess(val -> {
            QuestController.getInstance().getScheduler().sync(() -> {
                QuestController.getInstance().startQuest(val.get(), pc);
            });
        });
    }

    /*@Subcommand("active")
    public void cmdQuestActive(Player player, String questId) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        ActiveTracker.setActive(pc, questId);

        ActiveTracker.getActiveQuest(pc).onSuccess(val -> {
            if (!val.isPresent())
                MessageUtil.sendUpdate(pc.getPlayer(), "You are no longer tracking a gear.");
            else
                MessageUtil.sendUpdate(pc.getPlayer(), "You are now tracking '" + val.get().getName() + "'.");
        }, QuestController.getInstance().getScheduler()::async);
    }*/

    @Subcommand("gui")
    @Description("Show the quest book.")
    public void cmdListQuestGUI(Player player) {
        if (!Characters.isPlayerCharacterLoaded(player)) return;

        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        //String activeId = ActiveTracker.getActive(pc);
        int activeId = 0;

        Collection<IQuestInstance> instances = QuestController.getInstance().getPlayerQuests(pc);

        int active = instances.stream().mapToInt(v -> v.getState() == QuestState.ACTIVE ? 1 : 0).sum();
        int completed = instances.stream().mapToInt(v -> v.getState().wasCompleted() ? 1 : 0).sum();

        Book book = new Book("Quest Journal", "Acolyte");

        book.addPage(
                new TextBuilder("\n\n\n" + StringUtil.center(Book.WIDTH, "Quest Journal")).color(ChatColor.BLACK)
                        .append("\n\n\n" + StringUtil.center(Book.WIDTH, "Current Quests:") + "\n").color(ChatColor.DARK_AQUA)
                        .append(StringUtil.center(Book.WIDTH, active + "") + "\n").color(ChatColor.BLACK)
                        .append(StringUtil.center(Book.WIDTH, "Completed Quests:") + "\n").color(ChatColor.DARK_PURPLE)
                        .append(StringUtil.center(Book.WIDTH, completed + "")).color(ChatColor.BLACK)
                        .create()
        );

        TextBuilder tb;

        IQuest quest;
        for (IQuestInstance instance : instances) {
            quest = instance.getQuest();
            tb = new TextBuilder("");

            tb.append(StringUtil.center(Book.WIDTH, quest.getId().equals(activeId) ? "[ " + quest.getName() + " ]" : quest.getName()) + "\n").color(ChatColor.BLACK).underlined(true)
                    .command("/quests active " + quest.getId());

            if (quest.getDescription().length() > 0)
                tb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextBuilder(String.join("\n",
                                StringUtil.splitForStackLore(ChatColor.translateAlternateColorCodes('&', quest.getDescription()))
                            )).create()));

            tb.append("\nObjectives:\n").color(ChatColor.DARK_GRAY);

            List<Map.Entry<Integer, QuestLogEntry>> entries = new ArrayList<>();
            entries.addAll(instance.getLogEntries().entrySet());
            Collections.sort(entries, (o1, o2) -> o2.getKey() - o1.getKey());

            for(QuestLogEntry entry : entries.stream().map(v -> v.getValue()).toArray(QuestLogEntry[]::new)) {
                tb.append("[" + (entry.success ? "⦿" : "⦾") + "]")
                        .color(ChatColor.BLACK)
                    .append((entry.optional ? "(Optional)" : "") + entry.text + "\n")
                        .color(ChatColor.BLACK).strikethrough(entry.disabled);
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