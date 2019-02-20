package com.legendsofvaleros.modules.cooldowns.cooldown;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Tracks a map of string:cooldown pairs for a player-character.
 * <p>
 * Uses a default-passive approach for cleanup and expiration. Although this does support
 * semi-precise, scheduled expirations, unless there is a client asking for one, cooldowns may hang
 * around until this object is accessed. This reduces the workload that the server has to do in
 * order to fulfill the cooldowns api.
 * <p>
 * Prioritizes cooldowns on the basis of their remaining lifetime when checking for expired
 * cooldowns. This makes it unnecessary to constantly iterate over every cooldown on every access.
 */
public class CharacterCooldowns implements Cooldowns {

    // Heuristics to balance between CPU load and memory usage
    // Short cooldowns will be checked for expiration more often than medium and long cooldowns
    private static final long SHORT_COOLDOWN_CUTOFF = 60000;
    private static final long LONG_COOLDOWN_CUTOFF = 1800000;

    private static final long MIN_SHORT_CHECK_INTERVAL = 5000;
    private static final long MIN_MEDIUM_CHECK_INTERVAL = 30000;
    private static final long MIN_LONG_CHECK_INTERVAL = 600000;

    private final PlayerCharacter playerCharacter;
    private final RemainingDurationCalculator calc;

    private final Map<String, CharacterCooldown> cooldownMap;
    public Collection<CharacterCooldown> getCooldowns() { return cooldownMap.values(); }

    private final Set<CharacterCooldown> shortCooldowns;
    private final Set<CharacterCooldown> mediumCooldowns;
    private final Set<CharacterCooldown> longCooldowns;

    private long lastShortCheck;
    private long lastMediumCheck;
    private long lastLongCheck;

    private final Set<String> expiredKeys;

    public CharacterCooldowns(PlayerCharacter parent) {
        this.playerCharacter = parent;
        this.calc = new RemainingDurationCalculator();
        this.cooldownMap = new HashMap<>();

        this.shortCooldowns = new HashSet<>();
        this.mediumCooldowns = new HashSet<>();
        this.longCooldowns = new HashSet<>();

        this.expiredKeys = new HashSet<>();
    }

    @Override
    public PlayerCharacter getPlayerCharacter() {
        return playerCharacter;
    }

    @Override
    public boolean hasCooldown(String key) {
        check();

        CharacterCooldown cooldown = cooldownMap.get(key);
        if (cooldown == null) {
            return false;
        }
        return cooldown.getRemainingDurationMillis() > 0;
    }

    @Override
    public CharacterCooldown getCooldown(String key) {
        check();

        CharacterCooldown cooldown = cooldownMap.get(key);

        // doesn't return an expired cooldown
        if (cooldown != null && cooldown.getRemainingDurationMillis() <= 0) {
            return null;
        }

        return cooldown;
    }

    @Override
    public CharacterCooldown offerCooldown(String key, CooldownType type, long durationMillis) {
        CharacterCooldown previous = getCooldown(key);
        if (previous != null) {
            return null;
        }

        return createCooldown(key, type, durationMillis);
    }

    @Override
    public CharacterCooldown overwriteCooldown(String key, CooldownType type, long durationMillis) {
        CharacterCooldown previous = getCooldown(key);

        CharacterCooldown cd = createCooldown(key, type, durationMillis);

        // adopts any previous cooldown's listeners
        if (previous != null) {
            for (CooldownExpirationListener listener : previous.listeners) {
                cd.registerListener(listener);
            }
        }

        return cd;
    }

    public void onLogin() {
        calc.onLogin();

        // schedules expiration tasks for any listened-to character-play-time cooldowns
        for (CharacterCooldown cd : cooldownMap.values()) {
            if (cd.type == CooldownType.CHARACTER_PLAY_TIME) {
                cd.scheduleTask();
            }
        }
    }

    public void onLogout() {
        calc.onLogout();

        // halts expiration tasks for any listened-to character-play-time cooldowns
        for (CharacterCooldown cd : cooldownMap.values()) {
            if (cd.type == CooldownType.CHARACTER_PLAY_TIME) {
                cd.unscheduleTask();
            }
        }
    }

    private CharacterCooldown createCooldown(String key, CooldownType type, long durationMillis) {
        check();

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        } else if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        } else if (durationMillis <= 0) {
            throw new IllegalArgumentException("duration must be positive");
        }

        // removes any previous cooldown associated with the given key
        CharacterCooldown previous = cooldownMap.get(key);
        if (previous != null) {
            remove(previous);
        }

        CharacterCooldown cd = new CharacterCooldown(this, key, type, durationMillis);
        cooldownMap.put(key, cd);

        if (durationMillis > LONG_COOLDOWN_CUTOFF) {
            longCooldowns.add(cd);
        } else if (durationMillis > SHORT_COOLDOWN_CUTOFF) {
            mediumCooldowns.add(cd);
        } else {
            shortCooldowns.add(cd);
        }
        expiredKeys.remove(key);

        return cd;
    }

    private void check() {
        long now = System.currentTimeMillis();
        Iterator<CharacterCooldown> iter;
        List<CharacterCooldown> remove = new LinkedList<>();

        if (now - lastLongCheck >= MIN_LONG_CHECK_INTERVAL) {
            lastLongCheck = now;

            iter = longCooldowns.iterator();
            while (iter.hasNext()) {
                CharacterCooldown cd = iter.next();
                long remaining = cd.getRemainingDurationMillis();

                if (remaining <= 0) {
                    iter.remove();
                    remove.add(cd);
                } else if (remaining <= SHORT_COOLDOWN_CUTOFF) {
                    iter.remove();
                    shortCooldowns.add(cd);
                } else if (remaining <= LONG_COOLDOWN_CUTOFF) {
                    iter.remove();
                    mediumCooldowns.add(cd);
                }
            }
        }

        if (now - lastMediumCheck >= MIN_MEDIUM_CHECK_INTERVAL) {
            lastMediumCheck = now;

            iter = mediumCooldowns.iterator();
            while (iter.hasNext()) {
                CharacterCooldown cd = iter.next();
                long remaining = cd.getRemainingDurationMillis();

                if (remaining <= 0) {
                    iter.remove();
                    remove.add(cd);
                } else if (remaining <= SHORT_COOLDOWN_CUTOFF) {
                    iter.remove();
                    shortCooldowns.add(cd);
                }
            }
        }

        if (now - lastShortCheck >= MIN_SHORT_CHECK_INTERVAL) {
            lastShortCheck = now;

            iter = shortCooldowns.iterator();
            while (iter.hasNext()) {
                CharacterCooldown cd = iter.next();
                long remaining = cd.getRemainingDurationMillis();

                if (remaining <= 0) {
                    iter.remove();
                    remove.add(cd);
                }
            }
        }

        // removes expired cooldowns from the map, if they are the most current one
        for (CharacterCooldown cd : remove) {
            Cooldown fromMap = cooldownMap.get(cd.getKey());
            if (cd.equals(fromMap)) {
                remove(cd);
            }
        }
    }

    private void remove(CharacterCooldown cooldown) {
        if (cooldown == null) {
            return;
        }
        cooldownMap.remove(cooldown.key);
        shortCooldowns.remove(cooldown);
        mediumCooldowns.remove(cooldown);
        longCooldowns.remove(cooldown);

        expiredKeys.add(cooldown.key);

        cooldown.unscheduleTask();
    }

    /**
     * An instance of a cooldown for a player-character.
     */
    public class CharacterCooldown implements Cooldown {

        private final Cooldowns parent;
        private final String key;
        private final CooldownType type;

        private BukkitRunnable scheduledTask;
        private final Set<CooldownExpirationListener> listeners;

        private final long timeWhenStartedLocally;
        private final long durationMillis;

        private CharacterCooldown(Cooldowns parent, String key, CooldownType type, long durationMillis) {
            this.parent = parent;
            this.key = key;
            this.type = type;
            this.listeners = new HashSet<>();

            this.timeWhenStartedLocally = System.currentTimeMillis();
            this.durationMillis = durationMillis;
        }

        @Override
        public Cooldowns getCooldowns() {
            return parent;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public CooldownType getCooldownType() {
            return type;
        }

        @Override
        public long getRemainingDurationMillis() {
            return calc.getRemainingMillis(type, timeWhenStartedLocally, durationMillis);
        }

        @Override
        public void expire() {
            remove(this);
            for (CooldownExpirationListener listener : listeners) {
                listener.onExpiration(this);
            }
        }

        @Override
        public void registerListener(CooldownExpirationListener listener) {
            listeners.add(listener);

            if (scheduledTask == null) {
                scheduleTask();
            }
        }

        public void unscheduleTask() {
            if (scheduledTask != null) {
                scheduledTask.cancel();
                scheduledTask = null;
            }
        }

        private void scheduleTask() {
            if (!listeners.isEmpty() && getRemainingDurationMillis() > 0) {
                unscheduleTask();

                scheduledTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        expire();
                    }
                };

                // converts remaining duration to ticks
                scheduledTask.runTaskLater(LegendsOfValeros.getInstance(), getRemainingDurationMillis() / 50);
            }
        }
    }

}
