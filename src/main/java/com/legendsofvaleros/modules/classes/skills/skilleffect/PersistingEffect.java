package com.legendsofvaleros.modules.classes.skills.skilleffect;

import com.legendsofvaleros.modules.characters.api.CharacterId;

/**
 * An effect that can be stored in a database when a player-character logs out and reapplied when
 * they log back in.
 */
public class PersistingEffect {

  /**
   * Creates a new instance for a builder of a persisting effect.
   * <p>
   * Takes mandatory fields as arguments, so they are not left uninitialized. The returned object is
   * ready to build immediately, though its methods can be used to attach additional, optional
   * information to the persisting effect's record.
   * 
   * @param effectId The unique string name of the effect that will be stored across logins.
   * @param affectedCharacter The character who is affected by the effect that will be stored across
   *        logins.
   * @param remainingDurationMillis The remaining duration of the effect, in milliseconds.
   * @return A new builder to finish creating a record for the effect that can persist across
   *         logins.
   * @throws IllegalArgumentException On a <code>null</code> or empty parameter or on a remaining
   *         duration that is not positive.
   */
  public static PersistingEffectBuilder newBuilder(String effectId, CharacterId affectedCharacter,
      long remainingDurationMillis) throws IllegalArgumentException {
    return new PersistingEffectBuilder(effectId, affectedCharacter, remainingDurationMillis);
  }

  private String effectId;
  private CharacterId affected;

  private long elapsedMillis = -1;
  private long remainingDurationMillis;

  // arbitrary meta data
  private int level = 1;
  private String stringMeta;
  private byte[] byteMeta;

  private PersistingEffect() {}

  /**
   * Gets the name of the effect.
   * <p>
   * The name must be the same on the server that this effect is created on and where it is loaded
   * from its persistent record, or it will not be able to be reapplied.
   * 
   * @return The effect's unique string name.
   */
  public String getEffectId() {
    return effectId;
  }

  /**
   * Gets the unique name of the affected character.
   * 
   * @return The affected character's unique name.
   */
  public CharacterId getAffected() {
    return affected;
  }

  /**
   * Gets how much of this effect's total duration has elapsed, in milliseconds.
   * 
   * @return The amount of time that has elapsed in milliseconds. <code>-1</code> if undefined.
   */
  public long getElapsedMillis() {
    return elapsedMillis;
  }

  /**
   * Gets the remaining duration of this effect, in milliseconds.
   * 
   * @return The remaining amount of time this effect should be in place unless interrupte for some
   *         reason, in milliseconds.
   */
  public long getRemainingDurationMillis() {
    return remainingDurationMillis;
  }

  /**
   * Gets the level of this effect.
   * 
   * @return This effect's level.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Gets an arbitrary string metadata associated with this effect, if there is one.
   * 
   * @return An arbitrary string to stored with this effect, if one was defined. Else
   *         <code>null</code>.
   */
  public String getStringMeta() throws IllegalArgumentException {
    return stringMeta;
  }

  /**
   * Gets an arbitrary set of bytes associated with this effect, if there is one.
   * 
   * @return An arbitrary byte array stored with this effect, if one was defined. Else
   *         <code>null</code>.
   */
  public byte[] getByteMeta() {
    return byteMeta;
  }

  /**
   * A builder for persisting effects.
   */
  public static class PersistingEffectBuilder {

    private PersistingEffect building;
    private boolean saved;

    public PersistingEffectBuilder(String effectId, CharacterId affectedCharacter,
        long remainingDurationMillis) {
      if (effectId == null || effectId.isEmpty()) {
        throw new IllegalArgumentException(
            "effectId is a mandatory field and cannot be null or empty");
      } else if (affectedCharacter == null) {
        throw new IllegalArgumentException(
            "affectedCharacter is a mandatory field and cannot be null");
      } else if (remainingDurationMillis < 1) {
        throw new IllegalArgumentException("remaining duration must be positive");
      }

      this.building = new PersistingEffect();
      building.effectId = effectId;
      building.affected = affectedCharacter;
      building.remainingDurationMillis = remainingDurationMillis;
    }

    /**
     * Builds the persisting effect object and makes an asychronous attempt to save it.
     * <p>
     * If this effect has already been saved or is in the process of saving, this does nothing.
     */
    public void buildAndSave() {
      if (!saved) {
        PersistingEffects.saveEffect(building);
        saved = true;
      }
    }

    PersistingEffect build() {
      return building;
    }

    /**
     * Sets the amount of this effect's duration has already elapsed, in milliseconds.
     * <p>
     * This is not a mandatory field, but may be useful in many scenarios in which a
     * player-character's progress (ie 50% or 75% done) in this effect may be significant.
     * 
     * @param elapsedMillis The amount of milliseconds that have already passed in this effect's
     *        duration.
     * @return This builder.
     * @throws IllegalArgumentException On a negative number of milliseconds.
     */
    public PersistingEffectBuilder setElapsedDurationMillis(long elapsedMillis)
        throws IllegalArgumentException {
      if (elapsedMillis < 0) {
        throw new IllegalArgumentException("elapsed time cannot be negative");
      }
      building.elapsedMillis = elapsedMillis;
      return this;
    }

    /**
     * Sets the level if this effect.
     * 
     * @param level This effect's level.
     * @return This builder.
     */
    public PersistingEffectBuilder setLevel(int level) {
      building.level = level;
      return this;
    }

    /**
     * Sets arbitrary string metadata to be stored with this effect.
     * <p>
     * Max length: 255 characters.
     * 
     * @return An arbitrary string (up to 255 characters) to be stored with this effect. Can be
     *         <code>null</code> to clear any previous metadata.
     * @return This builder.
     * @throws IllegalArgumentException On a string longer than 255 characters.
     */
    public PersistingEffectBuilder setStringMeta(String stringMeta) throws IllegalArgumentException {
      if (stringMeta != null && stringMeta.length() > 255) {
        throw new IllegalArgumentException("string meta cannot be more than 255 characters");
      }
      building.stringMeta = stringMeta;
      return this;
    }

    /**
     * Sets arbitrary byte metadata to be stored with this effect.
     * <p>
     * Max length: 512 bytes.
     * 
     * @param byteMeta An arbitrary byte array (up to 512 bytes) to be stored with this effect. Can
     *        be <code>null</code> to clear any previous metadata.
     * @return This builder.
     * @throws IllegalArgumentException On a an array longer than 512 bytes.
     */
    public PersistingEffectBuilder setByteMeta(byte[] byteMeta) throws IllegalArgumentException {
      if (byteMeta != null && byteMeta.length > 512) {
        throw new IllegalArgumentException("byte array meta cannot be longer than 512");
      }
      if (byteMeta != null) {
        // defensive copy
        byteMeta = byteMeta.clone();
      }
      building.byteMeta = byteMeta;
      return this;
    }

  }

}
