package com.legendsofvaleros.modules.quests.trait;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestStartedEvent;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.item.Model;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitQuestGiver extends LOVTrait {
    private static final String MARKER = "quest";

    public static class Marker implements Listener {
        @EventHandler
        public void onQuestStarted(QuestStartedEvent event) {
            update(event.getPlayerCharacter());
        }

        @EventHandler
        public void onQuestComplete(QuestCompletedEvent event) {
            update(event.getPlayerCharacter());
        }

		/*@EventHandler
		public void onObjectivesComplete(QuestObjectivesCompletedEvent event) {
			update(event.getPlayerCharacter());
		}*/

        @EventHandler
        public void onCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
            update(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            for (TraitQuestGiver trait : all)
                trait.available.getVisibilityManager().hideTo(event.getPlayer());
        }

        public static void update(TraitQuestGiver trait) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!Characters.isPlayerCharacterLoaded(p)) continue;

                update(trait, Characters.getPlayerCharacter(p));
            }
        }

        public static void update(PlayerCharacter pc) {
            if (pc == null || !pc.isCurrent()) return;

            for (TraitQuestGiver trait : all)
                update(trait, pc);
        }

        private static Set<TraitQuestGiver> working = new HashSet<>();

        private static void update(TraitQuestGiver trait, PlayerCharacter pc) {
            if (working.contains(trait)) return;
            working.add(trait);

            List<ListenableFuture<IQuest>> futures = new ArrayList<>();
            AtomicInteger left = new AtomicInteger(trait.questIDs.length);

            for (String id : trait.questIDs) {
                ListenableFuture<IQuest> future = QuestManager.getQuest(id);
                futures.add(future);

                future.addListener(() -> {
                    try {
                        try {
                            IQuest quest = future.get();
                            QuestStatus status = QuestManager.getStatus(pc, quest);

                            if (status.canAccept()) {
                                trait.available.getVisibilityManager().showTo(pc.getPlayer());
                                futures.forEach(f -> f.cancel(true));
                                return;
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        if (left.decrementAndGet() == 0) {
                            trait.available.getVisibilityManager().hideTo(pc.getPlayer());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, Quests.getInstance().getScheduler()::sync);
            }
        }
    }

    public static List<TraitQuestGiver> all = new ArrayList<>();

    @Override
    public void onSpawn() {
        all.add(this);

        available = trait.nameplates.getOrAdd(MARKER);
        available.appendItemLine(Model.stack("marker-quest-available").create());
        available.getVisibilityManager().setVisibleByDefault(false);

        Marker.update(this);
    }

    @Override
    public void onDespawn() {
        all.remove(this);

        available.delete();
    }

    public String introText;
    public String[] questIDs;
    public Hologram available;

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        SettableFuture<List<IQuest>> future = SettableFuture.create();

        future.addListener(() -> {
            LinkedHashMap<IQuest, QuestStatus> playerQuests = new LinkedHashMap<>();

            try {
                PlayerCharacter pc = Characters.getPlayerCharacter(player);
                for (IQuest quest : future.get()) {
                    QuestStatus status = QuestManager.getStatus(pc, quest);
                    if (status.canAccept())
                        playerQuests.put(quest, status);
                }
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), player, e, true);
                return;
            }

            if (playerQuests.size() == 0) {
                slot.set(null);
                return;
            }

            if (playerQuests.size() == 1) {
                Entry<IQuest, QuestStatus> quest = playerQuests.entrySet().iterator().next();
                player.performCommand("quests talk " + quest.getKey().getId());
            } else if (playerQuests.size() != 0) {
                slot.set(new Slot(new ItemBuilder(Material.BOOK_AND_QUILL).setName("Quests").create(), (gui, p, event) -> {
                    gui.close(p);

                    openGUI(player, playerQuests);
                }));
            }
        }, Quests.getInstance().getScheduler()::async);

        List<IQuest> quests = new ArrayList<>();
        AtomicInteger left = new AtomicInteger(questIDs.length);
        for (String questId : questIDs) {
            ListenableFuture<IQuest> futureQuest = QuestManager.getQuest(questId);

            futureQuest.addListener(() -> {
                try {
                    IQuest quest = futureQuest.get();
                    if (quest != null)
                        quests.add(quest);
                    else
                        throw new Exception("Failed to load quest on NPC! Offender: " + questId + " on " + trait.npcId);
                } catch (Exception e) {
                    MessageUtil.sendException(Quests.getInstance(), e, false);
                }

                if (left.decrementAndGet() == 0)
                    future.set(quests);
            }, Quests.getInstance().getScheduler()::async);
        }
    }

    private void openGUI(Player player, LinkedHashMap<IQuest, QuestStatus> playerQuests) {
        Book book = new Book("Possible Quests", "Acolyte");

        TextBuilder tb = new TextBuilder(StringUtil.center(Book.WIDTH, npc.getName())).color(ChatColor.DARK_AQUA).underlined(true);

        if (introText != null && introText.length() > 0)
            tb.append("\n\n" + introText).color(ChatColor.BLACK);

        tb.append("\n\n");

        for (final Entry<IQuest, QuestStatus> quest : playerQuests.entrySet()) {
            if (StringUtil.getStringWidth(quest.getKey().getName()) > Book.WIDTH)
                tb.append("[" + quest.getKey().getName() + "]" + "\n\n");
            else
                tb.append(StringUtil.center(Book.WIDTH, "[" + quest.getKey().getName() + "]") + "\n\n");

            // TODO: Switch to using "temporary" commands, as anyone who knows the secret command can accept any quest.
            tb.color(quest.getValue() == QuestStatus.NEITHER ? ChatColor.DARK_PURPLE : ChatColor.DARK_RED)
                    .command("/quests talk " + quest.getKey().getId());
        }

        book.addPage(tb.create());

        book.open(player, false);
    }
}