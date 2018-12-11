package com.legendsofvaleros.modules.quests;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.objective.stf.IQuestObjective;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ActiveTracker {
    private static int allUpdateInterval;

    private static final Map<CharacterId, String> active = new HashMap<>();

    public static void onEnable() {
        allUpdateInterval = Quests.getInstance().getConfig().getInt("compass-tracker-update-smear", 20 * 10);

        Quests.getInstance().getLogger().info("Smearing compass tracker updates across " + allUpdateInterval + " ticks.");

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
            if(quests.size() == 0)
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
                }, Quests.getInstance().getScheduler()::async);
            }, Quests.getInstance().getScheduler()::async);
        }

        return ret;
    }

    private static void onTick(long time) {
        int block = (int)(time % allUpdateInterval);
        int blockSize = (int)Math.ceil((double)Bukkit.getOnlinePlayers().size() / allUpdateInterval);

        Bukkit.getOnlinePlayers().stream()
                    .limit(blockSize).skip(block * blockSize)
                .forEach(p -> {
                    if(!Characters.isPlayerCharacterLoaded(p)) return;

                    PlayerCharacter pc = Characters.getPlayerCharacter(p);

                    ListenableFuture<IQuest> future = getActiveQuest(pc);
                    future.addListener(() -> {
                        try {
                            IQuest active = future.get();
                            if(active == null) return;

                            IQuestObjective<?>[] group = active.getCurrentGroup(pc);
                            if(group != null)
                                for(IQuestObjective<?> obj : group) {
                                    if(!obj.isCompleted(pc)) {
                                        Location loc = obj.getLocation(pc);
                                        if (loc != null) {
                                            pc.getPlayer().setCompassTarget(loc);
                                            return;
                                        }
                                    }
                                }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, Quests.getInstance().getScheduler()::async);
                });
    }
}