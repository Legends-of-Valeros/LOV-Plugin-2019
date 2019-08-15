package com.legendsofvaleros.modules.npcs.trait.quests;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.questsold.api.IQuest;
import com.legendsofvaleros.modules.quests.api.QuestStatus;
import com.legendsofvaleros.modules.quests.events.QuestEndedEvent;
import com.legendsofvaleros.modules.quests.events.QuestStartedEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitQuestGiver extends LOVTrait {
    private static final String MARKER = "quest";

    public static List<TraitQuestGiver> all = new ArrayList<>();

    public String introText;
    public String[] questIDs;
    public Hologram available;

    public transient List<IQuest> quests;

    @Override
    public void onSpawn() {
        all.add(this);

        available = trait.nameplates.getOrAdd(MARKER);
        available.appendItemLine(Model.stack("marker-quest-available").create());
        available.getVisibilityManager().setVisibleByDefault(false);

        List<Promise<IQuest>> promises = new ArrayList<>();

        for (String id : questIDs) {
            promises.add(QuestController.getInstance().getQuest(id));
        }

        Promise.collect(promises).onSuccess(val -> {
            quests = val.orElseGet(ArrayList::new);
            Marker.update(this);
        });
    }

    @Override
    public void onDespawn() {
        all.remove(this);

        available.delete();
    }

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
                    QuestStatus status = QuestController.getInstance().getStatus(pc, quest);
                    if (status.canAccept()) {
                        playerQuests.put(quest, status);
                    }
                }
            } catch (Exception e) {
                MessageUtil.sendSevereException(QuestController.getInstance(), player, e);
                return;
            }

            if (playerQuests.isEmpty()) {
                slot.set(null);
                return;
            }

            if (playerQuests.size() == 1) {
                Entry<IQuest, QuestStatus> quest = playerQuests.entrySet().iterator().next();
                QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                    player.performCommand("quests talk " + quest.getKey().getId());
                });
            } else {
                slot.set(new Slot(new ItemBuilder(Material.WRITABLE_BOOK).setName("Quests").create(), (gui, p, event) -> {
                    gui.close(p);

                    openGUI(player, playerQuests);
                }));
            }
        }, QuestController.getInstance().getScheduler()::async);

        List<IQuest> quests = new ArrayList<>();
        AtomicInteger left = new AtomicInteger(questIDs.length);
        for (String questId : questIDs) {
            QuestController.getInstance().getQuest(questId).on((err, val) -> {
                if (val.isPresent()) {
                    IQuest quest = val.get();
                    quests.add(quest);
                } else
                    throw new Exception("Failed to load quest on NPC! Offender: " + questId + " on " + trait.npcId);

                if (left.decrementAndGet() == 0)
                    future.set(quests);
            }, QuestController.getInstance().getScheduler()::async);
        }
    }

    private void openGUI(Player player, LinkedHashMap<IQuest, QuestStatus> playerQuests) {
        Book book = new Book("Possible Quests", "Acolyte");

        TextBuilder tb = new TextBuilder(StringUtil.center(Book.WIDTH, npc.getName())).color(ChatColor.DARK_AQUA).underlined(true);

        if (introText != null && introText.length() > 0)
            tb.append("\n\n" + introText).color(ChatColor.BLACK);

        tb.append("\n\n");

        for (final Entry<IQuest, QuestStatus> quest : playerQuests.entrySet()) {
            if (StringUtil.getStringWidth(quest.getKey().getName()) > Book.WIDTH) {
                tb.append("[" + quest.getKey().getName() + "]" + "\n\n");
            } else {
                tb.append(StringUtil.center(Book.WIDTH, "[" + quest.getKey().getName() + "]") + "\n\n");
            }

            // TODO: Switch to using "temporary" commands, as anyone who knows the secret command can accept any quest.
            tb.color(quest.getValue() == QuestStatus.NEITHER ? ChatColor.DARK_PURPLE : ChatColor.DARK_RED)
                    .command("/quests talk " + quest.getKey().getId());
        }

        book.addPage(tb.create());

        book.open(player, false);
    }

    public static class Marker implements Listener {
        @EventHandler
        public void onQuestStarted(QuestStartedEvent event) {
            update(event.getPlayerCharacter());
        }

        @EventHandler
        public void onQuestComplete(QuestEndedEvent event) {
            update(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
            update(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            for (TraitQuestGiver trait : all) {
                trait.available.getVisibilityManager().hideTo(event.getPlayer());
            }
        }

        public static void update(TraitQuestGiver trait) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!Characters.isPlayerCharacterLoaded(p)) {
                    continue;
                }

                update(trait, Characters.getPlayerCharacter(p));
            }
        }

        public static void update(PlayerCharacter pc) {
            if (pc == null || !pc.isCurrent()) {
                return;
            }

            for (TraitQuestGiver trait : all) {
                update(trait, pc);
            }
        }

        private static void update(TraitQuestGiver trait, PlayerCharacter pc) {
            if (trait.quests == null) {
                return;
            }

            for (IQuest quest : trait.quests) {
                QuestStatus status = QuestController.getInstance().getStatus(pc, quest);

                // If the quest can be accepted, the marker should be active.
                if (status.canAccept()) {
                    trait.available.getVisibilityManager().showTo(pc.getPlayer());
                    return;
                }
            }

            trait.available.getVisibilityManager().hideTo(pc.getPlayer());
        }
    }

}