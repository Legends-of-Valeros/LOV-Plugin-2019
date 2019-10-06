package com.legendsofvaleros.modules.npcs.trait.quests;

import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.api.Ref;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.QuestStatus;
import com.legendsofvaleros.modules.quests.events.QuestEndedEvent;
import com.legendsofvaleros.modules.quests.events.QuestStartedEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TraitQuestGiver extends LOVTrait {
    private static final String MARKER = "quest";

    public static List<TraitQuestGiver> all = new ArrayList<>();

    public transient Hologram available;

    public String text;
    public Ref<IQuest>[] quests;

    @Override
    public void onSpawn() {
        all.add(this);

        available = trait.nameplates.getOrAdd(MARKER);
        available.appendItemLine(Models.stack("marker-quest-available").create());
        available.getVisibilityManager().setVisibleByDefault(false);

        Marker.update(this);
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
            Collection<IQuest> acceptableQuests = new ArrayList<>();

            try {
                PlayerCharacter pc = Characters.getPlayerCharacter(player);
                for (IQuest quest : future.get()) {
                    QuestStatus status = quest.getStatus(pc);
                    if (status.canAccept()) {
                        acceptableQuests.add(quest);
                    }
                }
            } catch (Exception e) {
                MessageUtil.sendSevereException(QuestController.getInstance(), player, e);
                return;
            }

            if (acceptableQuests.isEmpty()) {
                slot.set(null);
                return;
            }

            if (acceptableQuests.size() == 1) {
                IQuest quest = acceptableQuests.iterator().next();
                QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                    player.performCommand("quests init " + quest.getId());
                });
            } else {
                slot.set(new Slot(new ItemBuilder(Material.WRITABLE_BOOK).setName("Quests").create(), (gui, p, event) -> {
                    gui.close(p);

                    openGUI(player, acceptableQuests);
                }));
            }
        }, QuestController.getInstance().getScheduler()::async);
    }

    private void openGUI(Player player, Collection<IQuest> acceptableQuests) {
        Book book = new Book("Possible Quests", "Acolyte");

        TextBuilder tb = new TextBuilder(StringUtil.center(Book.WIDTH, npc.getName())).color(ChatColor.DARK_AQUA).underlined(true);

        if (text != null && text.length() > 0)
            tb.append("\n\n" + text).color(ChatColor.BLACK);

        tb.append("\n\n");

        for (IQuest quest : acceptableQuests) {
            if (StringUtil.getStringWidth(quest.getName()) > Book.WIDTH) {
                tb.append("[" + quest.getName() + "]" + "\n\n");
            } else {
                tb.append(StringUtil.center(Book.WIDTH, "[" + quest.getName() + "]") + "\n\n");
            }

            // TODO: Switch to using the page event, as anyone who knows the secret command can accept any quest.
            tb.color(ChatColor.DARK_PURPLE).command("/quests info " + quest.getId());
        }

        book.addPage(tb.create());

        book.open(player, false);
    }

    // TODO: fix this bullshit. It's ass.
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

            for (Ref<IQuest> ref : trait.quests) {
                if(ref.get() == null) continue;

                // If the quest can be accepted, the marker should be active.
                if (ref.get().getStatus(pc).canAccept()) {
                    // trait.available.getVisibilityManager().showTo(pc.getPlayer());
                    return;
                }
            }

            trait.available.getVisibilityManager().hideTo(pc.getPlayer());
        }
    }
}