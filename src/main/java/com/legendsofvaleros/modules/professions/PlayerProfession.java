package com.legendsofvaleros.modules.professions;

import com.legendsofvaleros.modules.characters.api.CharacterId;

/**
 * Created by Crystall on 04/10/2019
 */
public class PlayerProfession {
    private CharacterId characterId;

    private int primaryProfessionId;
    private int primaryProfessionLevel;

    private int secondaryProfessionsId;
    private int secondaryProfessionLevel;

    public PlayerProfession(CharacterId characterId, int primaryProfessionId, int primaryProfessionLevel, int secondaryProfessionsId, int secondaryProfessionLevel) {
        this.characterId = characterId;
        this.primaryProfessionId = primaryProfessionId;
        this.primaryProfessionLevel = primaryProfessionLevel;
        this.secondaryProfessionsId = secondaryProfessionsId;
        this.secondaryProfessionLevel = secondaryProfessionLevel;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public int getPrimaryProfessionId() {
        return primaryProfessionId;
    }

    public int getPrimaryProfessionLevel() {
        return primaryProfessionLevel;
    }

    public int getSecondaryProfessionLevel() {
        return secondaryProfessionLevel;
    }

    public int getSecondaryProfessionsId() {
        return secondaryProfessionsId;
    }

    public void setCharacterId(CharacterId characterId) {
        this.characterId = characterId;
    }

    public void setPrimaryProfessionId(int primaryProfessionId) {
        this.primaryProfessionId = primaryProfessionId;
    }

    public void setPrimaryProfessionLevel(int primaryProfessionLevel) {
        this.primaryProfessionLevel = primaryProfessionLevel;
    }

    public void setSecondaryProfessionLevel(int secondaryProfessionLevel) {
        this.secondaryProfessionLevel = secondaryProfessionLevel;
    }

    public void setSecondaryProfessionsId(int secondaryProfessionsId) {
        this.secondaryProfessionsId = secondaryProfessionsId;
    }
}
