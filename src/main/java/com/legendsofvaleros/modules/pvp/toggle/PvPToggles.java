package com.legendsofvaleros.modules.pvp.toggle;

import com.google.common.collect.TreeMultimap;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

public class PvPToggles {

    private TreeMultimap<UUID, PvPToggle> toggles;

    public PvPToggles() {
        toggles = TreeMultimap.create(UUID::compareTo,
                Comparator.comparingInt(PvPToggle::getPriority));
    }

    /**
     * Checks whether or not PvP is enabled for this player, taking priority into account.
     * @param uuid The {@link UUID} of the player to check.
     * @return If PvP is enabled or not for this player. If there is no toggle set it will default to false.
     */
    public boolean isPvPEnabledFor(UUID uuid) {
        PvPToggle toggle = getRulingToggleFor(uuid);

        return toggle != null && toggle.isEnabled();
    }

    /**
     * Set a toggle for the specified player. If the toggle already exists it will be removed and the new toggle will be added in its place.
     * @param uuid The {@link UUID} of the player to set the {@link PvPToggle} for.
     * @param priority The priority of the {@link PvPToggle}
     * @param pvp If PvP is enabled or not.
     * @param honorPoints The amount of honor points to be awarded for this type of PvP, 0 if none.
     */
    public void setToggleFor(UUID uuid, byte priority, boolean pvp, int honorPoints) {
        Collection<PvPToggle> togglesFor = toggles.get(uuid);
        PvPToggle toggle = togglesFor.stream().filter(t -> t.getPriority() == priority).findFirst().orElse(new PvPToggle(priority, pvp, honorPoints));

        togglesFor.removeIf(t -> t.getPriority() == priority);
        togglesFor.add(toggle);
    }

    /**
     * Set a toggle for the specified player. If the toggle already exists it will be removed and the new toggle will be added in its place.
     * @param uuid The {@link UUID} of the player to set the {@link PvPToggle} for.
     * @param toggle The toggle object to apply to the player.
     */
    public void setToggleFor(UUID uuid, PvPToggle toggle) {
        Collection<PvPToggle> togglesFor = toggles.get(uuid);
        PvPToggle old = togglesFor.stream().filter(t -> t.getPriority() == toggle.getPriority()).findFirst().orElse(toggle);

        togglesFor.removeIf(t -> t.getPriority() == toggle.getPriority());
        togglesFor.add(toggle);
    }

    /**
     * Removes a toggle for the specified player, based on the toggle priority.
     * @param uuid The player ID to remove the toggle for.
     * @param priority The priority of the toggle to remove.
     * @return Whether the toggle was removed.
     */
    public boolean removeToggleFor(UUID uuid, byte priority) {
        return toggles.get(uuid).removeIf(toggle -> toggle.getPriority() == priority);
    }

    /**
     * Gets the {@link PvPToggle} with the highest priority.
     * @param uuid The {@link UUID} of the player to check.
     * @return The ruling {@link PvPToggle}, or null if there is none.
     */
    public PvPToggle getRulingToggleFor(UUID uuid) {
        Iterator<PvPToggle> it = toggles.get(uuid).iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * Clears all the {@link PvPToggle}s for the player with the given UUID.
     * @param uuid
     */
    public void clearTogglesFor(UUID uuid) {
        toggles.removeAll(uuid);
    }

    /**
     * Gets all the toggles for the given UUID.
     * @param uniqueId The UUID.
     * @return The toggles.
     */
    public Collection<PvPToggle> getTogglesFor(UUID uniqueId) {
        return toggles.get(uniqueId);
    }
}
