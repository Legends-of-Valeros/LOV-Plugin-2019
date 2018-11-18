package com.legendsofvaleros.modules.parties;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.CharacterId;

import java.util.List;
import java.util.UUID;

public interface IParty {
	/**
	 * @return A UUID representing the party.
	 */
	UUID getUniqueId();
	
	List<CharacterId> getMembers();
	
	void onDisbanded();

	void onMemberJoin(CharacterId uuid);
	void onMemberLeave(CharacterId uuid);
	
	void onMemberEnter(CharacterId uuid);
	void onMemberExit(CharacterId uuid);

	/**
	 * Called every couple seconds to do visual updates for parties.
	 */
	void updateUI();
}