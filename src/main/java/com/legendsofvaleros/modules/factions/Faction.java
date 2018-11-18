package com.legendsofvaleros.modules.factions;

import com.codingforcookies.doris.orm.ORM;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.ForeignKey;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;

@Table(name = "factions")
public class Faction {
	@Column(primary = true, name = "faction_id", length = 32)
	private String id;
	public String getId() { return id; }

	@Column(name = "faction_name", length = 32)
	protected String name;
	public String getName() { return name; }

	@Column(name = "faction_description")
	protected String description;
	public String getDescription() { return description; }

	@Column(name = "faction_rep_max")
	protected int maxReputation;
	public int getMaxReputation() { return maxReputation; }

	@Table(name = "player_faction_rep")
	public static class Reputation extends ORM {
		@Column(primary = true, name = "character_id", length = 39)
		private CharacterId characterId;
		public CharacterId getCharacterId() { return characterId; }
		
		@ForeignKey(table = Faction.class, name = "faction_id", onUpdate = ForeignKey.Trigger.CASCADE, onDelete = ForeignKey.Trigger.CASCADE)
		@Column(primary = true, name = "faction_id", length = 32)
		private String factionId;
		public String getFactionId() { return factionId; }

		@Column(name = "faction_rep")
		public int reputation = 0;

		public Reputation(CharacterId characterId, String factionId) {
			this.characterId = characterId;
			this.factionId = factionId;
		}
	}
}