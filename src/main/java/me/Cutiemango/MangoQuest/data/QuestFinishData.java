package me.Cutiemango.MangoQuest.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;

public class QuestFinishData
{
	protected final Quest q;
	protected int times;
	protected long lastFinish;

	protected boolean rewardTaken;
	protected Player p;

	public Player getP() {
		return p;
	}
	public QuestFinishData() {
		this.q = null;
		
	}

	public QuestFinishData(Player p,Quest quest, int time, long lastFinishTime, boolean reward) {
		q = quest;
		times = time;
		lastFinish = lastFinishTime;
		rewardTaken = reward;
		this.p = p;
	}

	public Quest getQuest() {
		return q;
	}

	public int getFinishedTimes() {
		return times;
	}

	public long getLastFinish() {
		return lastFinish;
	}

	public void finish() {
		lastFinish = System.currentTimeMillis();
		times++;
	}

	public void setLastFinish(long l) {
		lastFinish = l;
	}

	public boolean isRewardTaken() {
		return rewardTaken;
	}

	public void setRewardTaken(boolean b) {
		rewardTaken = b;
		if(b && !(this instanceof IncompatibleQuestFinishData)) {
			//update npc effects
			if(!q.isRedoable()) {
				QuestPlayerData qpd = QuestUtil.getData(p);
				if(q.getQuestNPC()!=null&&qpd.getNpcEffects().containsKey(q.getQuestNPC())) {
					List<Quest> quests = new ArrayList<>(qpd.getNpcEffects().get(q.getQuestNPC()));
					for(Quest q1:qpd.getNpcEffects().get(q.getQuestNPC())) {
						if(QuestValidater.weakValidate(q, q1)) {
							quests.removeIf((quest)->{
								return quest.getInternalID().equals(q1.getInternalID());
							});
						}
					}
					qpd.putNPCEffectsQuestList(q.getQuestNPC(), quests);
					
				}
			}else {
				QuestPlayerData pd = QuestUtil.getData(p);
				List<Quest> questList = pd.getRedoableQuests();
				questList.add(q);
				pd.setRedoableQuests(questList);
			}
		}
	}

}
