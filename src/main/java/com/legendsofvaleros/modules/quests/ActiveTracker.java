package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.modules.quests.core.QuestStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    public static Promise<IQuest> getActiveQuest(PlayerCharacter pc) {
        String activeId = getActive(pc);
        if(activeId == null) {
            return Promise.make(() -> {
                Collection<IQuest> quests = QuestController.getInstance().getPlayerQuests(pc);
                if(quests == null || quests.size() == 0)
                    return null;

                IQuest active = quests.iterator().next();

                setActive(pc, active.getId());

                return active;
            });
        }else{
            return QuestController.getInstance().getQuest(getActive(pc)).then(val -> {
                if(!val.isPresent()) return null;

                IQuest quest = val.get();
                if(QuestController.getInstance().getStatus(pc, quest) == QuestStatus.ACCEPTED)
                    return quest;

                setActive(pc, null);

                return null;
            }).next(v -> {
                if(v.isPresent()) return Promise.make(v.orElse(null));
                return getActiveQuest(pc);
            });
        }
    }

    private static void onTick(long time) {
        int block = (int)(time % allUpdateInterval);
        int blockSize = (int)Math.ceil((double)Bukkit.getOnlinePlayers().size() / allUpdateInterval);

        Bukkit.getOnlinePlayers().stream()
                .skip(block * blockSize).limit(blockSize)
                .forEach(p -> {
                    if(!Characters.isPlayerCharacterLoaded(p)) return;

                    PlayerCharacter pc = Characters.getPlayerCharacter(p);

                    getActiveQuest(pc).on((err, val) -> {
                        boolean invalid = true;

                        if(err != null && val.isPresent()) {
                            IQuest active = val.get();
                            if (active != null) {
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