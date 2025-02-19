package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.util.StringUtil;
import com.legendsofvaleros.features.gui.item.Book;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.action.QuestActionPlay;
import com.legendsofvaleros.modules.quests.action.QuestActions;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestAction;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestObjectivesCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestObjectivesStartedEvent;
import com.legendsofvaleros.modules.quests.event.QuestStartedEvent;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractQuest implements IQuest {
    /**
     * Due to quests being instantiated once, we need to make sure the players progress is saved.
     */
    private transient HashMap<CharacterId, QuestProgressPack> progress = new HashMap<>();

    @Override public Set<Map.Entry<CharacterId, QuestProgressPack>> getProgressions() {
        return progress.entrySet();
    }

    @Override public QuestProgressPack getProgress(PlayerCharacter pc) {
        return progress.get(pc.getUniqueCharacterId());
    }

    @Override public void loadProgress(PlayerCharacter pc, QuestProgressPack pack) {
        progress.put(pc.getUniqueCharacterId(), pack);
    }

    @Override public boolean hasProgress(PlayerCharacter pc) {
        return progress.containsKey(pc.getUniqueCharacterId());
    }

    @Override public void clearProgress(PlayerCharacter pc) {
        progress.remove(pc.getUniqueCharacterId());
    }

    private String id;

    @Override public String getId() {
        return id;
    }

    private String type;

    @Override public String getType() {
        return type;
    }

    private final List<IQuestPrerequisite> prerequisites = new ArrayList<>();

    @Override public List<IQuestPrerequisite> getPrerequisites() {
        return prerequisites;
    }

    private QuestActions actions;

    @Override public QuestActions getActions() {
        return actions;
    }

    @Override
    public void testResumeActions(PlayerCharacter pc) {
        QuestProgressPack progress = getProgress(pc);
        if (progress == null) return;
        if (progress.actionI != null)
            startActions(pc, progress.group);
    }

    private boolean forced = false;

    @Override public boolean isForced() {
        return forced;
    }

    private boolean repeatable = false;

    @Override public boolean isRepeatable() {
        return repeatable;
    }

    private String name;

    @Override public String getName() {
        return name;
    }

    private String description;

    @Override public String getDescription() {
        return description;
    }

    private QuestObjectives objectives;

    public QuestObjectives getObjectives() {
        return objectives;
    }

    public AbstractQuest() {
    }

    @Override
    public void onAccept(PlayerCharacter pc) {
        QuestController.getInstance().removeQuestProgress(this.id, pc);

        QuestController.getInstance().addPlayerQuest(pc, this);

        startGroup(pc, null);
    }

    @Override
    public void onDecline(PlayerCharacter pc) {
        QuestActionPlay.start(pc.getPlayer(), new QuestProgressPack(0, 0), getActions().decline);
    }

    @Override
    public Integer getActionGroupI(PlayerCharacter pc) {
        QuestProgressPack pack = progress.get(pc.getUniqueCharacterId());
        if (pack == null) return null;
        if (pack.group == null) return -1;
        return pack.actionI;
    }

    @Override
    public IQuestAction[] getActionGroup(PlayerCharacter pc) {
        Integer i = getActionGroupI(pc);
        if (i == null) return null;
        if (i == -1) return actions.accept;
        return actions.groups[i];
    }

    @Override
    public Integer getObjectiveGroupI(PlayerCharacter pc) {
        QuestProgressPack pack = progress.get(pc.getUniqueCharacterId());
        if (pack == null) return null;
        return pack.group;
    }

    @Override
    public IQuestObjective<?>[] getObjectiveGroup(PlayerCharacter pc) {
        Integer i = getObjectiveGroupI(pc);
        if (i == null) return null;
        return objectives.groups[i];
    }

    public void startGroup(PlayerCharacter pc, Integer group) {
        // Run the task later. This is done to be sure objective events finish firing before starting the next objective group.
        QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
            Integer currentGroup = getObjectiveGroupI(pc);

            if (currentGroup == null && group != null)
                throw new IllegalStateException(pc.getPlayer().getName() + "(" + pc.getUniqueCharacterId() + ") attempted to go to group " + group + " from " + currentGroup + " in quest '" + getId() + "'! This should never happen!");

            // If the player is just now starting the quest
            if (group == null) {
                loadProgress(pc, new QuestProgressPack(group, 0));

                Bukkit.getPluginManager().callEvent(new QuestStartedEvent(pc, this));

                startActions(pc, group);
            } else {
                if (currentGroup == group) return;

                Bukkit.getPluginManager().callEvent(new QuestObjectivesCompletedEvent(pc, this));

                if (objectives.groups.length > 0) {
                    // Clean up the objectives
                    for (int i = 0; i < objectives.groups[currentGroup].length; i++)
                        objectives.groups[currentGroup][i].onEnd(pc);
                }

                startActions(pc, currentGroup);
            }
        });
    }

    private void startActions(PlayerCharacter pc, Integer currentGroup) {
        IQuestAction[] acts = null;
        if (currentGroup == null)
            acts = actions.accept;
        else if (currentGroup < actions.groups.length)
            acts = actions.groups[currentGroup];

        ListenableFuture<Boolean> future = QuestActionPlay.start(pc, getProgress(pc), acts);

        future.addListener(() -> continueToNextGroup(pc, (currentGroup == null ? 0 : currentGroup + 1)),
                QuestController.getInstance().getScheduler()::async);
    }

    private void continueToNextGroup(PlayerCharacter pc, int nextGroup) {
        // If there are no more objective groups, end the quest
        if (nextGroup >= objectives.groups.length) {
            QuestController.getInstance().finishQuest(this, pc);
            clearProgress(pc);
            onCompleted(pc);

            QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                Bukkit.getPluginManager().callEvent(new QuestCompletedEvent(pc, this));
            });
            return;
        }

        if (objectives.groups.length > 0) {
            // Set up the new objective group progress information
            QuestProgressPack pack;
            IQuestObjective<?>[] objectiveGroup = objectives.groups[nextGroup];
            loadProgress(pc, (pack = new QuestProgressPack(nextGroup, objectiveGroup.length)));

            // Build the objective object and initialize it
            for (int i = 0; i < objectiveGroup.length; i++) {
                try {
                    objectiveGroup[i].onBegin(pc);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
            Bukkit.getPluginManager().callEvent(new QuestObjectivesStartedEvent(pc, this, nextGroup == 0));
        });

        // Check if the new objective is already completed
        checkCompleted(pc);
    }

    protected void displayBook(String title, List<String> pages, Player player) {
        if (forced && (pages.size() == 0 || pages.get(0).length() == 0)) return;

        Book book = new Book(title, "Acolyte");

        TextBuilder tb = new TextBuilder(StringUtil.center(Book.WIDTH, name)).color(ChatColor.DARK_AQUA).underlined(true)
                .append("\n\n").color(ChatColor.BLACK);

        for (int i = 0; i < pages.size(); i++) {
            String[] splitPages = pages.get(i).split("\\n\\n");
            for (int j = 0; j < splitPages.length; j++) {
                tb.append(ChatColor.translateAlternateColorCodes('&', splitPages[j]) + "\n\n").color(ChatColor.BLACK);
                if (j != splitPages.length - 1)
                    book.addPage(tb.create());
            }

            if (i == pages.size() - 1) {
                if (forced)
                    tb.append(StringUtil.center(Book.WIDTH, "[Ok]")).color(ChatColor.DARK_GREEN)
                            .command("/quests close");
                else {
                    tb.append(" [Accept]  ").color(ChatColor.DARK_GREEN)
                            .command("/quests accept " + id)
                            .append(" ")
                            .append("  [Decline] ").color(ChatColor.DARK_RED)
                            .command("/quests decline " + id);
                }
            }

            book.addPage(tb.create());

            tb = new TextBuilder("\n");
        }

        book.open(player, false);
    }
}