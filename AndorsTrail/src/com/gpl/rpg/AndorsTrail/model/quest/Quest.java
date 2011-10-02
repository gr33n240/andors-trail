package com.gpl.rpg.AndorsTrail.model.quest;

import com.gpl.rpg.AndorsTrail.model.actor.Player;

public final class Quest implements Comparable<Quest> {
	public final String questID;
	public final String name;
	public final QuestLogEntry[] stages; //Must be sorted in ascending stage order
	public final boolean showInLog;
	public final int sortOrder;
	
	public Quest(String questID, String name, QuestLogEntry[] stages, boolean showInLog, int sortOrder) {
		this.questID = questID;
		this.name = name;
		this.stages = stages;
		this.showInLog = showInLog;
		this.sortOrder = sortOrder;
	}
	
	public boolean isCompleted(final Player player) {
		for (QuestLogEntry e : stages) {
			if (!e.finishesQuest) continue;
			if (player.hasExactQuestProgress(questID, e.progress)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Quest q) {
		return (new Integer(sortOrder)).compareTo(q.sortOrder);
	}
}
