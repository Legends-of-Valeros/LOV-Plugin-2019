package com.legendsofvaleros.modules.characters.skill;

import com.codingforcookies.robert.core.StringUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffect;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffects;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.DamageHistory;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * An ability of a player-class.
 */
public abstract class Skill {
    protected static final String NONE = "None";
    protected static final String INSTANT = "Instant";
    protected static final String TARGET = "Targeted Instant";
    protected static final String NEXT_ATTACK = "Next Attack";

    private static Multimap<EntityClass, Skill> classSkills = HashMultimap.create();
    private static HashMap<String, Skill> availableSkills = new HashMap<>();

    public static Set<String> getSkillIds() {
        return availableSkills.keySet();
    }

    public static Collection<Skill> getSkills() {
        return availableSkills.values();
    }

    public static Skill getSkillById(String id) {
        if (availableSkills.containsKey(id))
            return availableSkills.get(id);
        return null;
    }

    public static List<Skill> getSkillsForCharacter(List<String> skills) {
        List<Skill> characterSkills = new ArrayList<>();

        for (String skillId : skills) {
            Skill skill = getSkillById(skillId);
            if (skill != null)
                characterSkills.add(skill);
        }

        return characterSkills;
    }


    private final String id;
    private final EntityClass pclass;
    private final int[] levelCosts;

    public int getMaxLevel() {
        return this.levelCosts.length;
    }

    private final int[] powerCost;
    private final int[] cooldown;
    private final Object[] description;

    protected static Object getEarliest(Object[] arr, int level) {
        return arr[Math.max(0, Math.min(level, arr.length) - 1)];
    }

    protected static int getEarliest(int[] arr, int level) {
        return arr[Math.max(0, Math.min(level, arr.length) - 1)];
    }

    protected static double getEarliest(double[] arr, int level) {
        return arr[Math.max(0, Math.min(level, arr.length) - 1)];
    }

    public Skill(String id, EntityClass pclass, int[] levelCosts, int[] powerCost, double[] cooldown, Object[] description) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty!");
        } else if (getSkillById(id) != null) {
            throw new IllegalArgumentException("name must be unique from other skills' ids!");
        }

        this.id = id;
        this.pclass = pclass;
        this.levelCosts = levelCosts;
        this.powerCost = powerCost;

        this.cooldown = new int[cooldown.length];
        for (int i = 0; i < this.cooldown.length; i++)
            this.cooldown[i] = (int) (1000 * cooldown[i]);

        List<Object> desc = new ArrayList<>();
        desc.add("\n" + ChatColor.GOLD + ChatColor.BOLD + "Cooldown: ");
        desc.add(new TimePart().millis(this.cooldown));
        desc.add("\n" + ChatColor.GOLD + ChatColor.BOLD + getStatUsed().getUserFriendlyName() + " Cost: ");
        desc.add((IDescriptionPart) (level, showUpgrade) -> new String[]{String.valueOf(getSkillCost(level))});
        if (getActivationTime() != null)
            desc.add("\n" + ChatColor.GOLD + ChatColor.BOLD + "Activation: " + ChatColor.YELLOW + getActivationTime());
        desc.add("\n\n");
        desc.addAll(Arrays.asList(description));
        this.description = desc.toArray(new Object[0]);

        classSkills.put(pclass, this);
        availableSkills.put(this.id, this);
    }

    /**
     * Gets the string name of this skill.
     * @return This skill's string name.
     */
    public final String getId() {
        return id;
    }

    /**
     * Core skills use a small hack. Their level 1 cost is set to -1, which causes the code to think
     * there are no more upgrades available. But, since they auto upgrade, they bypass that level 1 cost
     * to get to the next cost to upgrade the skill, which should be > 0.
     * @return The upgrade cost of the skill. Should return -1 if no more upgrades are possible.
     */
    public int getNextLevelCost(int toLevel) {
        toLevel--; // Normalize the value to the array location
        if (toLevel >= this.levelCosts.length)
            return -1;
        return levelCosts[toLevel];
    }

    public final int getTotalLevelCost(int level) {
        int cost = 0;
        for (int i = 0; i < Math.min(level, this.levelCosts.length); i++) {
            int scost = getNextLevelCost(i + 1);
            if (scost > 0)
                cost += scost;
        }
        return cost;
    }

    /**
     * Gets the user friendly name of the skill.
     * @return This skill's user friendly name.
     */
    public abstract String getUserFriendlyName(int level);

    public abstract String getActivationTime();

    public String[] getSkillDescription(PlayerCharacter pc, int level, boolean showUpgrade) {
        StringBuilder descAfter = new StringBuilder();
        StringBuilder desc = new StringBuilder(ChatColor.GRAY.toString());
        for (Object obj : this.description) {
            if (obj instanceof Object[]) {
                Object[] o = (Object[]) obj;
                desc.append(ChatColor.YELLOW);
                desc.append(getEarliest(o, level));
                if (showUpgrade)
                    desc.append(uptokeStrong("", getEarliest(o, level), getEarliest(o, level + 1)));
            } else if (obj instanceof int[]) {
                int[] o = (int[]) obj;
                desc.append(ChatColor.YELLOW);
                desc.append(getEarliest(o, level));
                if (showUpgrade)
                    desc.append(uptokeStrong("", getEarliest(o, level), getEarliest(o, level + 1)));
            } else if (obj instanceof double[]) {
                double[] o = (double[]) obj;
                desc.append(ChatColor.YELLOW);
                desc.append(getEarliest(o, level));
                if (showUpgrade)
                    desc.append(uptokeStrong("", getEarliest(o, level), getEarliest(o, level + 1)));
            } else if (obj instanceof IDescriptionPart) {
                desc.append(ChatColor.YELLOW);
                String[] arg = ((IDescriptionPart) obj).get(level, showUpgrade);
                switch (arg.length) {
                    case 1:
                        desc.append(arg[0]);
                        break;
                    default:
                        desc.append(arg[0]);
                        descAfter.append(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE);
                        descAfter.append(arg[1]).append("\n");
                }
            } else {
                desc.append(obj.toString());
            }

            desc.append(ChatColor.GRAY);
        }

        if (doesRequireFocus())
            desc.append("\n\n" + ChatColor.LIGHT_PURPLE + "Requires a Focus");

        List<String> lore = new ArrayList<>(Arrays.asList(StringUtil.splitForStackLore(desc.toString().trim())));
        if (descAfter.length() > 4) {
            lore.add("");
            lore.addAll(Arrays.asList(StringUtil.splitForStackLore(descAfter.toString().trim())));
        }

        return lore.toArray(new String[0]);
    }

    private static String uptokeStrong(String prefix, Object current, Object next) {
        if (current == next)
            return "";

        ChatColor color = ChatColor.YELLOW;
        if (next instanceof Integer) {
            if ((int) next > (int) current)
                color = ChatColor.GREEN;
            else if ((int) next < (int) current)
                color = ChatColor.RED;
            else
                return "";
        }

        return " " + uptokeStrong(next, color);
    }

    private static String uptokeStrong(Object next, ChatColor color) {
        return String.valueOf(color) + "(-> " + next + ")";
    }

    public boolean doesRequireFocus() {
        return false;
    }

    public final RegeneratingStat getStatUsed() {
        return pclass.getSkillCostType();
    }

    public final double getSkillCost(int level) {
        return getEarliest(powerCost, level + 1);
    }

    public final long getSkillCooldownTime(PlayerCharacter pc, int level) {
        return getEarliest(cooldown, level);
    }

    /**
     * When the skill is actually used.
     * @param ce    The combat entity using the skill.
     * @param level The level of the skill
     * @return True if spell targeted an entity successfully.
     */
    public abstract boolean onSkillUse(World world, CombatEntity ce, int level);

    public Collection<LivingEntity> getNearbyEntities(CombatEntity ce, double x, double y, double z) {
        return getNearbyEntities(ce.getLivingEntity(), ce.getLivingEntity().getLocation(), x, y, z);
    }

    public Collection<LivingEntity> getNearbyEntities(Location l, double x, double y, double z) {
        return getNearbyEntities(null, l, x, y, z);
    }

    public Collection<LivingEntity> getNearbyEntities(LivingEntity ignore, Location l, double x, double y, double z) {
        List<LivingEntity> entities = new ArrayList<>();
        for (Entity e : l.getWorld().getNearbyEntities(l, x, y, z)) {
            if (e == ignore || !(e instanceof LivingEntity)) continue;
            if (CombatEngine.getEntity((LivingEntity) e) == null) continue;
            entities.add((LivingEntity) e);
        }
        return entities;
    }

    public Location getLookTargetGround(CombatEntity ce, int range) {
        Location loc = getLookTarget(ce, range);
        Block b = ce.getLivingEntity().getWorld().getBlockAt(loc);
        if (b != null) {
            if (b.getType() != Material.AIR) {
                loc = b.getLocation().add(0, 1, 0);
            } else {
                loc = b.getLocation();
                while (ce.getLivingEntity().getWorld().getBlockAt(loc.subtract(0, 1, 0)).getType() == Material.AIR) ;
                loc.add(0, 1, 0);
            }
        }
        return loc;
    }

    public Location getLookTarget(CombatEntity ce, int range) {
        Set<Material> s = new HashSet<>();
        s.add(Material.AIR);
        Block b = ce.getLivingEntity().getTargetBlock(s, range);
        if (b != null) return b.getLocation();
        return null;
    }

    /**
     * A helper function used to get the entity a player is targeting.<br /><br />
     * <p>
     * It begins by compiling a list of targets within x blocks around the player.
     * Once done, it checks for approximate line of sight, if an entity is within
     * it, it is returned. Otherwise <code>null</code>.
     */
    public LivingEntity getTarget(CombatEntity ce, double reach) {
        LivingEntity target = null;
        double targetDistance = 0;

        Vector l = ce.getLivingEntity().getEyeLocation().toVector(),
                playerLoc = ce.getLivingEntity().getLocation().getDirection().normalize();

        for (LivingEntity other : getTargets(ce, reach, LivingEntity.class)) {
            double dist = ce.getLivingEntity().getLocation().distance(other.getLocation());

            if (target == null || dist < targetDistance) {
                Vector entityLoc = other.getLocation().add(0, 1, 0).toVector().subtract(l);
                double radius = Math.max(.25, .9 - (dist / 3) * .55);
                if (playerLoc.clone().crossProduct(entityLoc).lengthSquared() < radius && entityLoc.normalize().dot(playerLoc) >= .97D) {
                    target = other;
                    targetDistance = dist;
                }
            }
        }

        return target;
    }

    /**
     * A helper function used to get the entity a player is targeting.<br /><br />
     * <p>
     * It begins by compiling a list of targets within 8 block around the player.
     * Once done, it checks for approximate line of sight, if an entity is within
     * it, it is returned. Otherwise, the recent history of the player is fetched,
     * and if the entity that did the most recent damage to the player is within
     * 10 blocks, that is returned. Otherwise <code>null</code>.
     */
    public LivingEntity getTargetOrLast(CombatEntity ce, double reach) {
        LivingEntity target = getTarget(ce, reach);
        if (target == null) {
            DamageHistory history = CombatEngine.getInstance().getDamageHistory(ce.getLivingEntity());
            if (history == null) return null;
            target = history.getLastDamager();
            if (target != null && ce.getLivingEntity().getLocation().distance(target.getLocation()) > 10)
                return null;
        }

        if (CombatEngine.getEntity(target) == null) return null;

        return target;
    }

    @SafeVarargs
    public final Collection<LivingEntity> getTargets(CombatEntity ce, double radius, Class<? extends LivingEntity>... types) {
        if (types != null && types.length > 0) {
            List<LivingEntity> entities = new ArrayList<>();

            Collection<LivingEntity> targets = getTargets(ce, radius);
            for (LivingEntity target : targets) {
                for (Class<? extends LivingEntity> type : types) {
                    if (type.isAssignableFrom(target.getClass())) {
                        entities.add(target);
                        break;
                    }
                }
            }

            return entities;
        }

        return getNearbyEntities(ce, radius, radius, radius);
    }

    public <T extends LivingEntity> Collection<T> getTargets(CombatEntity ce, double radius, Class<T> type) {
        List<T> entities = new ArrayList<>();

        Collection<LivingEntity> targets = getTargets(ce, radius);
        for (Entity target : targets)
            if (type.isAssignableFrom(target.getClass()))
                entities.add(type.cast(target));

        return entities;
    }

    public boolean hasSkillEffect(LivingEntity entity, String effect) {
        SkillEffects effects = Characters.getInstance().getSkillEffectManager();
        return effects.getActiveEffects(entity).contains(effects.getSkillEffect(effect));
    }

    @FunctionalInterface
    public interface IDescriptionPart {
        String[] get(int level, boolean showUpgrade);
    }

    private static class IntArrPart implements IDescriptionPart {
        private final String postfix;
        private final int[] arr;

        public IntArrPart(String postfix, int[] arr) {
            this.postfix = postfix;
            this.arr = arr;
        }

        @Override
        public String[] get(int level, boolean showUpgrade) {
            int curr = getEarliest(arr, level);
            int next = getEarliest(arr, level + 1);
            return new String[]{curr + (showUpgrade ? uptokeStrong(" ", curr, next) : "") + ChatColor.YELLOW + postfix};
        }
    }

    public static class RangePart extends IntArrPart {
        public RangePart(int[] range) {
            super(" blocks", range);
        }
    }

    public static class RadiusPart extends IntArrPart {
        public RadiusPart(int[] radius) {
            super(" block radius", radius);
        }
    }

    public static class WDPart extends IntArrPart {
        public WDPart(int[] damage) {
            super("% weapon damage", damage);
        }
    }

    public static class TimePart implements IDescriptionPart {
        private int[] millis = new int[0];

        public TimePart() {
        }

        public TimePart seconds(double[] seconds) {
            this.millis = new int[seconds.length];
            for (int i = 0; i < seconds.length; i++)
                this.millis[i] = (int) (seconds[i] * 1000);
            return this;
        }

        public TimePart seconds(int[] seconds) {
            this.millis = new int[seconds.length];
            for (int i = 0; i < seconds.length; i++)
                this.millis[i] = seconds[i] * 1000;
            return this;
        }

        public TimePart ticks(int[] ticks) {
            this.millis = new int[ticks.length];
            for (int i = 0; i < ticks.length; i++)
                this.millis[i] = ticks[i] * 50;
            return this;
        }

        public TimePart millis(int[] millis) {
            this.millis = millis;
            return this;
        }

        @Override
        public String[] get(int level, boolean showUpgrade) {
            return new String[]{StringUtil.getTimeFromMilliseconds(getEarliest(millis, level), 1, false)};
        }
    }

    public static abstract class EffectPart<T> implements IDescriptionPart {
        private MetaEffectInstance<T> meta;

        @SuppressWarnings("unchecked")
        public EffectPart(String effect) {
            this.meta = new MetaEffectInstance<>((SkillEffect<T>) Characters.getInstance().getSkillEffectManager().getSkillEffect(effect));
            this.meta.level = 1;
        }

        @Override
        public String[] get(int level, boolean showUpgrade) {
            if (meta != null)
                meta(level, meta);
            if (meta != null && meta.getEffect() != null) {
                return new String[]{meta.getEffect().generateUserFriendlyName(meta), meta.getEffect().generateUserFriendlyDetails(meta)};
            } else
                return new String[]{"Unknown Effect"};
        }

        public abstract void meta(int level, MetaEffectInstance<T> meta);
    }
}