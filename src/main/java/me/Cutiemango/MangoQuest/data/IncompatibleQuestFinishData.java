package me.Cutiemango.MangoQuest.data;

import org.bukkit.entity.Player;

public class IncompatibleQuestFinishData extends QuestFinishData{

	private final String q;
	

	public IncompatibleQuestFinishData (Player p,String quest, int time, long lastFinishTime, boolean reward) {
		//super class is useless this way.
		super();
		q = quest;
		super.times = time;
		super.lastFinish = lastFinishTime;
		super.rewardTaken = reward;
		super.p = p;
	}

	public String getIncompatQuest() {
		return q;
	}

}
