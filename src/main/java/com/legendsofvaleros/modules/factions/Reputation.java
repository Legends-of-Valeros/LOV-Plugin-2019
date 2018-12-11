package com.legendsofvaleros.modules.factions;

import com.codingforcookies.doris.orm.ORM;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;

/**
 * Created by Crystall on 11/24/2018
 */
@Table(name = "player_faction_rep")
public class Reputation extends ORM {
    @Column(primary = true, name = "character_id", length = 39)
    private CharacterId characterId;

    @ForeignKey(table = Faction.class, name = "faction_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
    @Column(primary = true, name = "faction_id", length = 32)
    private String factionId;

    @Column(name = "faction_rep")
    public int reputation = 0;

    public Reputation(CharacterId characterId, String factionId) {
        this.characterId = characterId;
        this.factionId = factionId;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public int getReputation() {
        return reputation;
    }

    public String getFactionId() {
        return factionId;
    }

}