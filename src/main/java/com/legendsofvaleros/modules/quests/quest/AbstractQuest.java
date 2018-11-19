package com.legendsofvaleros.modules.quests.quest;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.quests.action.stf.QuestActionPlay;
import com.legendsofvaleros.modules.quests.action.stf.QuestActions;
import com.legendsofvaleros.modules.quests.event.ObjectivesCompletedEvent;
import com.legendsofvaleros.modules.quests.event.ObjectivesStartedEvent;
import com.legendsofvaleros.modules.quests.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestStartedEvent;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.progress.stf.IObjectiveProgress;
import com.legendsofvaleros.modules.quests.progress.stf.ObjectiveProgressPack;
import com.legendsofvaleros.modules.quests.progress.stf.QuestProgressPack;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestObjectives;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractQuest implements IQuest {
    /**
     * Due to quests being instantiated once, we need to make sure the players progress is saved.
     */
    private final HashMap<CharacterId, QuestProgressPack> progress = new HashMap<>();

    @Override public QuestProgressPack getProgress(PlayerCharacter pc) {
        return progress.get(pc.getUniqueCharacterId());
    }

    @Override public void loadProgress(PlayerCharacter pc, QuestProgressPack pack) {
        progress.put(pc.getUniqueCharacterId(), pack);
    }

    @Override public boolean hasProgress(PlayerCharacter pc) {
        return progress.containsKey(pc.getUniqueCharacterId()) && getProgress(pc) != null;
    }

    @Override public void clearProgress(PlayerCharacter pc) {
        progress.remove(pc.getUniqueCharacterId());
    }

    @Override public void saveProgress(PlayerCharacter pc) {
        QuestManager.saveQuestProgress(pc, this);
    }

    private final String id;

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

    @Override public void addPrerequisite(IQuestPrerequisite prereq) {
        prerequisites.add(prereq);
    }

    private QuestActions actions;

    @Override public QuestActions getActions() {
        return actions;
    }

    @Override public void setActions(QuestActions actions) {
        this.actions = actions;
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

    @Override public void setForced(boolean forced) {
        this.forced = forced;
    }

    private boolean repeatable = false;

    @Override public boolean isRepeatable() {
        return repeatable;
    }

    @Override public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    private String name;

    @Override public String getName() {
        return name;
    }

    @Override public void setName(String name) {
        this.name = name;
    }

    private String description;

    @Override public String getDescription() {
        return description;
    }

    @Override public void setDescription(String description) {
        this.description = description;
    }

    private QuestObjectives objectives;

    public QuestObjectives getObjectives() {
        return objectives;
    }

    @Override public void setObjectives(QuestObjectives objectives) {
        this.objectives = objectives;

        WeakReference<IQuest> ref = new WeakReference<>(this);

        for (int group = 0; group < objectives.groups.length; group++)
            for (int i = 0; i < objectives.groups[group].length; i++)
                objectives.groups[group][i].init(ref, group, i);
    }

    public AbstractQuest(String id) {
        this.id = id;
    }

    @Override
    public void onAccept(PlayerCharacter pc) {
        startGroup(pc, -1);
    }

    @Override
    public void onDecline(PlayerCharacter pc) {
        QuestActionPlay.start(pc.getPlayer(), new QuestProgressPack(0, 0), getActions().decline);
    }

    @Override
    public int getCurrentGroupI(PlayerCharacter pc) {
        QuestProgressPack pack = progress.get(pc.getUniqueCharacterId());
        if (pack == null) return -1;
        return pack.group;
    }

    @Override
    public IObjective<?>[] getCurrentGroup(PlayerCharacter pc) {
        return objectives.getGroup(getCurrentGroupI(pc));
    }

    public void startGroup(PlayerCharacter pc, int group) {
        // Run the task later. This is done to be sure objective events finish firing before starting the next objective group.
        Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> {
            int currentGroup = getCurrentGroupI(pc);

            // If the player is just now starting the quest
            if (group == -1) {
                loadProgress(pc, new QuestProgressPack(group, 0));

                Bukkit.getPluginManager().callEvent(new QuestStartedEvent(pc, this));

                startActions(pc, group);
            } else {
                if (currentGroup == group) return;

                Bukkit.getPluginManager().callEvent(new ObjectivesCompletedEvent(pc, this));

                if (objectives.groups.length > 0) {
                    // Clean up the objectives
                    for (int i = 0; i < objectives.groups[currentGroup].length; i++)
                        objectives.groups[currentGroup][i].onEnd(pc);
                }

                startActions(pc, currentGroup);
            }
        });
    }

    private void startActions(PlayerCharacter pc, int currentGroup) {
        AbstractAction[] acts = null;
        if (currentGroup == -1)
            acts = actions.accept;
        else if (currentGroup < actions.groups.length)
            acts = actions.groups[currentGroup];

        ListenableFuture<Boolean> future = QuestActionPlay.start(pc, getProgress(pc), acts);

        future.addListener(() -> continueToNextGroup(pc, currentGroup + 1), Utilities.asyncExecutor());
    }

    private void continueToNextGroup(PlayerCharacter pc, int nextGroup) {
        // If there are no more objective groups, end the quest
        if (nextGroup >= objectives.groups.length) {
            QuestManager.finishQuest(this, pc);
            clearProgress(pc);
            onCompleted(pc);

            Bukkit.getPluginManager().callEvent(new QuestCompletedEvent(pc, this));

            return;
        }

        if (objectives.groups.length > 0) {
            saveProgress(pc);

            // Set up the new objective group progress information
            QuestProgressPack pack;
            IObjective<?>[] objectiveGroup = objectives.groups[nextGroup];
            loadProgress(pc, (pack = new QuestProgressPack(nextGroup, objectiveGroup.length)));

            // Build the objective object and initialize it
            for (int i = 0; i < objectiveGroup.length; i++) {
                try {
                    IObjective<?> obj = objectiveGroup[i];
                    ParameterizedType superClass = (ParameterizedType) obj.getClass().getGenericSuperclass();
                    @SuppressWarnings("unchecked")
                    Class<? extends IObjectiveProgress> type = (Class<? extends IObjectiveProgress>) superClass.getActualTypeArguments()[0];
                    IObjectiveProgress prog = type.newInstance();
                    pack.data[i] = new ObjectiveProgressPack(prog);

                    obj.onBegin(pc);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Bukkit.getPluginManager().callEvent(new ObjectivesStartedEvent(pc, this, nextGroup == 0));

        // Check if the new objective is already completed
        checkCompleted(pc);
    }

    protected void displayBook(String title, List<String> pages, Player player, boolean ok) {
        if (ok && (pages.size() == 0 || pages.get(0).length() == 0)) return;

        Book book = new Book(title, "Acolyte");

        FancyMessage fm = new FancyMessage(StringUtil.center(Book.WIDTH, name)).color(ChatColor.DARK_AQUA).style(ChatColor.UNDERLINE)
                .then("\n\n").color(ChatColor.BLACK);

        for (int i = 0; i < pages.size(); i++) {
            String[] splitPages = pages.get(i).split("\\n\\n");
            for (int j = 0; j < splitPages.length; j++) {
                fm.then(ChatColor.translateAlternateColorCodes('&', splitPages[j]) + "\n\n").color(ChatColor.BLACK);
                if (j != splitPages.length - 1)
                    book.addPage(fm);
            }

            if (i == pages.size() - 1) {
                if (ok)
                    fm.then(StringUtil.center(Book.WIDTH, "[Ok]")).color(ChatColor.DARK_GREEN)
                            .command("/quests close");
                else {
                    fm.then(" [Accept]  ").color(ChatColor.DARK_GREEN)
                            .command("/quests accept " + id)
                            .then(" ")
                            .then("  [Decline] ").color(ChatColor.DARK_RED)
                            .command("/quests decline " + id);
                }
            }

            book.addPage(fm);

            fm = new FancyMessage("\n");
        }

        book.open(player, false);
    }
}