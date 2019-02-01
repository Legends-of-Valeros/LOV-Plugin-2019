package com.legendsofvaleros.modules.quests.action.stf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestActions {
	public IQuestAction[] accept;
	public IQuestAction[] decline;
	public IQuestAction[][] groups;


	public IQuestAction[][] getAll() {
		List<IQuestAction[]> all = new ArrayList();

		all.add(accept);
		all.add(decline);
		all.addAll(Arrays.asList(groups));

		return all.toArray(new IQuestAction[0][0]);
	}
}