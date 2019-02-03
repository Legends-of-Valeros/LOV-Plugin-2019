package com.legendsofvaleros.modules.characters.api;

import java.io.Serializable;
import java.util.UUID;

/**
 * An identifier for a player character that is unique across all players and characters.
 * <p>
 * Hashing and equality are based on the contents of the name, rather than the object's default
 * identity.
 */
public final class CharacterId implements Serializable {
    public static final byte LENGTH = 39;

    // string format: <uuid>-<characterNumber>

    private static final long serialVersionUID = -680310384381099912L;

    /**
     * Gets a character name from a string version of it.
     * @param stringVersion The string version of the character name.
     * @return A unique character name object for the given string version.
     * @throws IllegalArgumentException if the string is not a retrievable form of a character name.
     */
    public static CharacterId fromString(String stringVersion) {
        try {
            //preventing errors on auction loading when there is no highest_bidder
            if (stringVersion == null || stringVersion.length() == 0) {
                return null;
            }

            String uidStr = stringVersion.substring(0, 36);
            UUID uid = UUID.fromString(uidStr);

            // skips over the '-' that separates the two components of the name.
            String numStr = stringVersion.substring(36 + 1);
            int characterNumber = Integer.valueOf(numStr);

            return new CharacterId(uid, characterNumber);
        } catch (Exception e) {
            throw new IllegalArgumentException("not a valid string version of a character name");
        }
    }

    private final UUID playerId;
    private final int number;
    private final String result;

    private Integer hashCode; // caches immutable hash code

    /**
     * Class constructor.
     * @param playerId        The name of the player.
     * @param characterNumber The number of the player character, where a player's first character is
     *                        <code>1</code>.
     * @throws IllegalArgumentException On a <code>null</code> uid.
     */
    public CharacterId(UUID playerId, int characterNumber) throws IllegalArgumentException {
        if (playerId == null) {
            throw new IllegalArgumentException("player UUID cannot be null");
        }
        this.playerId = playerId;
        this.number = characterNumber;
        this.result = playerId.toString() + "-" + characterNumber;

    }

    /**
     * Gets the player's unique name, a component of this unique character name.
     * @return The player-character's player's unique name.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the number of the character this name is for, unique only to its player (a component of this
     * unique character name).
     * @return This character's number where a player's first character is <code>1</code>.
     */
    public int getCharacterNumber() {
        return number;
    }

    @Override
    public String toString() {
        return result;
    }

    @Override
    public int hashCode() {
        // precomputes hashcode on first use, as this object is immutable
        if (hashCode == null) {
            final int prime = 31;
            int hash = 1;
            hashCode = prime * hash + ((this.result == null) ? 0 : this.result.hashCode());
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CharacterId other = (CharacterId) obj;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        return true;
    }

}
