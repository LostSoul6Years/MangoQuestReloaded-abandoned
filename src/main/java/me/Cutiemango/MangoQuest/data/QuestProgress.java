package me.Cutiemango.MangoQuest.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.event.QuestFinishEvent;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.reward.QuestReward;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlaceholderAPI;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectReachLocation;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTalkToNPC;

public class QuestProgress {
	private boolean incompatibleMode = false;
	public QuestProgress(Quest q, Player p) {
		this(q, p, 0, new ArrayList<>(), System.currentTimeMillis());

		for (SimpleQuestObject o : quest.getStage(currentStage).getObjects()) {
			QuestObjectProgress qop = new QuestObjectProgress(o, 0);
			qop.setLastInvokedMilli(System.currentTimeMillis());
			if(o instanceof DecimalObject) {
				qop.setProgressD(0);
			}
			objList.add(qop);
		}
	}
	public QuestProgress(String q, Player p,int s,List<IncompatibleQuestObjectProgress> o,long stamp){
		takeStamp = stamp;
		quest = null;
		//this.incompatibleMode = true;
		incompatQuest = q;
		owner = p;
		currentStage = s;
		incompatObjList = o;
	}

	public QuestProgress(Quest q, Player p, int s, List<QuestObjectProgress> o, long stamp) {
		quest = q;
		owner = p;
		currentStage = s;
		objList = o;
		takeStamp = stamp;
		
	}

	private final Quest quest;
	private String incompatQuest;
	private final Player owner;
	private int currentStage;
	private List<QuestObjectProgress> objList;
	private List<IncompatibleQuestObjectProgress> incompatObjList; 
	
	private final long takeStamp;
	public long getIncompatQuestVersion() {
		return incompatQuestVersion;
	}
	public void setIncompatQuestVersion(long incompatQuestVersion) {
		this.incompatQuestVersion = incompatQuestVersion;
	}

	private long incompatQuestVersion;

	public void finish() {
		if(incompatibleMode) {
			return;
		}
		quest.trigger(owner, TriggerType.TRIGGER_ON_FINISH, -1);
		QuestPlayerData pd = QuestUtil.getData(owner);
		QuestReward reward = quest.getQuestReward();
		
		boolean giveItem = reward.instantGiveReward() || !reward.hasMultipleChoices();

		pd.addFinishedQuest(quest, giveItem);

		if (giveItem)
			reward.executeItemReward(owner);
		else
			pd.getFinishData(quest).setRewardTaken(false);

		reward.executeReward(owner);
		QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.CompleteMessage", quest.getQuestName()));
		pd.getPapiQuests().removeIf((qp)->{
			return QuestValidater.weakValidate(qp.getQuest(), quest);
		});
		pd.removeProgress(quest);
		pd.save();
		if(QuestStorage.timedProgress.containsKey(owner.getUniqueId().toString())) {
			List<QuestProgress> obtained = QuestStorage.timedProgress.get(owner.getUniqueId().toString());
			obtained.removeIf(qp1->QuestValidater.weakValidate(qp1.getQuest(),quest));
			if(obtained.isEmpty()) {
				QuestStorage.timedProgress.remove(owner.getUniqueId().toString());
			}else {
				QuestStorage.timedProgress.put(owner.getUniqueId().toString(),obtained);
			}
		}
		//quest.isRedoable()&&
		if(quest.getQuestNPC()!=null&&(!pd.hasTakenReward(quest))) {
			List<Quest> questList = new ArrayList<>();
			if(pd.getNpcEffects().containsKey(quest.getQuestNPC())) {
				questList = pd.getNpcEffects().get(quest.getQuestNPC());
			}
			questList.add(quest);
			pd.getNpcEffects().put(quest.getQuestNPC(), questList);
		}else if(quest.getQuestNPC()!=null&&(quest.isRedoable()&&pd.hasTakenReward(quest))) {
			List<Quest> questList = pd.getRedoableQuests();
			questList.add(quest);
			pd.setRedoableQuests(questList);
		}
		Bukkit.getPluginManager().callEvent(new QuestFinishEvent(owner, quest));
	}
	
	public void save(YamlConfiguration io) {
		if(incompatibleMode) {
			return;
		}
		io.set("QuestProgress." + quest.getInternalID() + ".QuestStage", currentStage);
		io.set("QuestProgress." + quest.getInternalID() + ".Version", quest.getVersion().getTimeStamp());
		io.set("QuestProgress." + quest.getInternalID() + ".TakeStamp", takeStamp);
		int t = 0;
		int value = 0;
		double valueD = -99999;
		for (QuestObjectProgress qop : objList) {
			if (qop.isFinished()) {
				if (qop.getObject() instanceof QuestObjectTalkToNPC
						|| qop.getObject() instanceof QuestObjectReachLocation)
					value = 1;
				else if (qop.getObject() instanceof NumerableObject)
					value = ((NumerableObject) qop.getObject()).getAmount();
				else if (qop.getObject() instanceof DecimalObject)
					valueD = ((DecimalObject) qop.getObject()).getAmount();
			} else {
				//if(qop.getObject() instanceof NumerableObject) {
					value = qop.getProgress();
					valueD = qop.getProgressD();
				//}
			}
				
				
		
			if (qop.getObject() instanceof DecimalObject) {
				io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress." + t, valueD);
			}else {
				io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress." + t, value);
			}
			io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress._" + t + "_lastinvokedmilli",
					qop.getLastInvokedMilli());
			t++;
		}
	}

	public void save(QuestIO io) {
		if(incompatibleMode) {
			return;
		}
		io.set("QuestProgress." + quest.getInternalID() + ".QuestStage", currentStage);
		io.set("QuestProgress." + quest.getInternalID() + ".Version", quest.getVersion().getTimeStamp());
		io.set("QuestProgress." + quest.getInternalID() + ".TakeStamp", takeStamp);
		int t = 0;
		int value = 0;
		double valueD = -99999;
		for (QuestObjectProgress qop : objList) {
			if (qop.isFinished()) {
				if (qop.getObject() instanceof QuestObjectTalkToNPC
						|| qop.getObject() instanceof QuestObjectReachLocation)
					value = 1;
				else if (qop.getObject() instanceof NumerableObject)
					value = ((NumerableObject) qop.getObject()).getAmount();
				else if (qop.getObject() instanceof DecimalObject)
					valueD = ((DecimalObject) qop.getObject()).getAmount();
			} else {
				
				if(qop.getObject() instanceof DecimalObject) {
					valueD = qop.getProgressD();
				}else {
					value = qop.getProgress();
				}
			}	
			
			if (qop.getObject() instanceof DecimalObject) {
				io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress." + t, valueD);
			}else {
				io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress." + t, value);
			}
			io.set("QuestProgress." + quest.getInternalID() + ".QuestObjectProgress._" + t + "_lastinvokedmilli",
					qop.getLastInvokedMilli());
			t++;
		}
	}

	public void checkIfNextStage() {
		if(incompatibleMode) {
			return;
		}
		boolean notFinished = false;
		boolean hasPAPIObjects = false;
		for (QuestObjectProgress o : objList) {
			if (!o.isFinished()) {
				notFinished = true;
			}
			if(o.getObject() instanceof QuestObjectPlaceholderAPI&&!o.isFinished()) {
				hasPAPIObjects = true;
			}
		}
		if(!hasPAPIObjects) {
			QuestUtil.getData(owner).getPapiQuests().removeIf((qp)->{
				return QuestValidater.weakValidate(qp.getQuest(),quest);
			});			
		}
		if(notFinished) {
			return;
		}
		nextStage();
	}

	public void nextStage() {
		if(incompatibleMode) {
			return;
		}
		quest.trigger(owner, TriggerType.TRIGGER_STAGE_FINISH, currentStage + 1);
		if (currentStage + 1 < quest.getStages().size()) {
			currentStage++;
			objList.clear();
			for (SimpleQuestObject o : quest.getStage(currentStage).getObjects()) {
				QuestObjectProgress qop = new QuestObjectProgress(o, 0);
				if(o instanceof DecimalObject) {
					qop.setProgressD(0);
				}
				objList.add(qop);
				
			}
			quest.trigger(owner, TriggerType.TRIGGER_STAGE_START, currentStage + 1);
			if(objList.stream().anyMatch((qop)->{
				return qop.getObject() instanceof QuestObjectPlaceholderAPI;
			})) {
				QuestUtil.getData(owner).getPapiQuests().add(this);
			}
			QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.ProgressMessage", quest.getQuestName(),
					Integer.toString(currentStage), Integer.toString(quest.getStages().size())));
			return;
		}
		finish();
	}

	public List<QuestObjectProgress> getCurrentObjects() {
		return objList;
	}

	public void setCurrentObjects(List<QuestObjectProgress> objList) {
		this.objList = objList;
	}

	public int getCurrentStage() {
		return currentStage;
	}

	public Quest getQuest() {
		return this.quest;
	}

	public Player getOwner() {
		return this.owner;
	}

	public long getTakeTime() {
		return takeStamp;
	}

	public boolean isIncompatibleMode() {
		return incompatibleMode;
	}

	public void setIncompatibleMode(boolean incompatibleMode) {
		this.incompatibleMode = incompatibleMode;
	}

	public String getIncompatQuest() {
		return incompatQuest;
	}

	public void setIncompatQuest(String incompatQuest) {
		this.incompatQuest = incompatQuest;
	}

	public List<IncompatibleQuestObjectProgress> getIncompatObjList() {
		return incompatObjList;
	}

	public void setIncompatObjList(List<IncompatibleQuestObjectProgress> incompatObjList) {
		this.incompatObjList = incompatObjList;
	}
}
