package com.legendsofvaleros.modules.quests;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.core.QuestStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class ActiveTracker {
    private static Random RAND = new Random();

    private static int allUpdateInterval;

    private static final Map<CharacterId, String> active = new HashMap<>();

    public static void onEnable() {
        allUpdateInterval = QuestController.getInstance().getConfig().getInt("compass-tracker-update-smear", 20 * 5);

        QuestController.getInstance().getLogger().info("Smearing compass tracker updates across " + allUpdateInterval + " ticks.");

        new BukkitRunnable() {
            private long time = 0;

            @Override
            public void run() {
                onTick(time++);
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 1, 1);
    }

    public static String getActive(PlayerCharacter pc) {
        if(!active.containsKey(pc.getUniqueCharacterId()))
            return null;
        return active.get(pc.getUniqueCharacterId());
    }

    public static void setActive(PlayerCharacter pc, String s) {
        active.put(pc.getUniqueCharacterId(), s);
    }

    public static ListenableFuture<IQuest> getActiveQuest(PlayerCharacter pc) {
        SettableFuture<IQuest> ret = SettableFuture.create();

        String activeId = getActive(pc);
        if(activeId == null) {
            Collection<IQuest> quests = QuestManager.getQuestsForEntity(pc);
            if(quests == null || quests.size() == 0)
                ret.set(null);
            else{
                IQuest active = quests.iterator().next();

                setActive(pc, active.getId());

                ret.set(active);
            }
        }else{
            ListenableFuture<IQuest> future = QuestManager.getQuest(getActive(pc));
            future.addListener(() -> {
                try {
                    IQuest quest = future.get();
                    if(QuestManager.getStatus(pc, quest) == QuestStatus.ACCEPTED) {
                        ret.set(quest);
                        return;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                setActive(pc, null);

                ListenableFuture<IQuest> future2 = getActiveQuest(pc);
                future2.addListener(() -> {
                    try {
                        ret.set(future2.get());
                        return;
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    ret.set(null);
                }, QuestController.getInstance().getScheduler()::async);
            }, QuestController.getInstance().getScheduler()::async);
        }

        return ret;
    }

    private static void onTick(long time) {
        int block = (int)(time % allUpdateInterval);
        int blockSize = (int)Math.ceil((double)Bukkit.getOnlinePlayers().size() / allUpdateInterval);

        Bukkit.getOnlinePlayers().stream()
                .skip(block * blockSize).limit(blockSize)
                .forEach(p -> {
                    if(!Characters.isPlayerCharacterLoaded(p)) return;

                    PlayerCharacter pc = Characters.getPlayerCharacter(p);

                    ListenableFuture<IQuest> future = getActiveQuest(pc);
                    future.addListener(() -> {
                        boolean invalid = true;

                        try {
                            IQuest active = future.get();
                            if(active != null) {
                                IQuestObjective<?>[] group = active.getObjectiveGroup(pc);
                                if (group != null)
                                    for (IQuestObjective<?> obj : group) {
                                        if (!obj.isCompleted(pc)) {
                                            Location loc = obj.getLocation(pc);
                                            if (loc != null) {
                                                pc.getPlayer().setCompassTarget(loc);
                                                invalid = false;
                                                break;
                                            }
                                        }
                                    }
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(invalid) {
                            pc.getPlayer().setCompassTarget(new Location(pc.getPlayer().getWorld(),
                                    pc.getPlayer().getLocation().getX() + RAND.nextInt(100) - 50,
                                    0,
                                    pc.getPlayer().getLocation().getZ() + RAND.nextInt(100) - 50));
                        }
                    }, QuestController.getInstance().getScheduler()::async);
                });
    }
}