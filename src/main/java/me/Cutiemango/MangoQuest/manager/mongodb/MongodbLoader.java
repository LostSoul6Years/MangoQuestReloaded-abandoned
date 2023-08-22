package me.Cutiemango.MangoQuest.manager.mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bson.Document;
import org.bukkit.entity.Player;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.data.IncompatibleQuestFinishData;
import me.Cutiemango.MangoQuest.data.IncompatibleQuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;

public class MongodbLoader {

	public static void loadPlayer(QuestPlayerData pd) {
		MongoCollection<Document> collection = MongodbManager.getCollection();
		Player p = pd.getPlayer();
		Document data = collection.find(new Document("UUID", p.getUniqueId().toString())).first();

		if (data == null) {
			// player is not present in database, try loading from yml
			File f = new File(Main.getInstance().getDataFolder() + "/data/", p.getUniqueId() + ".yml");
			if (f.exists())
				pd.load(ConfigSettings.SaveType.YML);
			MongodbSaver.savePlayerData(pd);
			return;
		}

		pd.getFriendPointStorage().putAll(getFriendPoints(data));
		pd.getFinishedConversations().addAll(data.getList("FinishedConv", String.class));
		pd.getProgresses().addAll(getProgressSet(pd.getPlayer(), data));
		pd.getFinishQuests().addAll(getFinishedQuests(p, data));

		DebugHandler.log(5, "%s's data loaded", data.get("LastKnownID"));
	}

	private static Set<QuestFinishData> getFinishedQuests(Player p, Document data) {
		Set<QuestFinishData> quests = new HashSet<>();
		data.get("FinishedQuests", Document.class)
				.forEach((questID, questData) -> quests.add(getQuestFinishData(p, questID, (Document) questData)));
		return quests;
	}

	private static QuestFinishData getQuestFinishData(Player p, String questID, Document questData) {
		Quest quest = QuestUtil.getQuest(questID);
		boolean incompatibleQuest = quest==null;
		if(incompatibleQuest && !ConfigSettings.differentialquestsserver) {
			DebugHandler.log(0,
					"[Mongodb] Found an invalid quest: %s, enable differential quest system or accept the error...",
					questID);
		}
		if(!incompatibleQuest) {
			int finishedTimes = questData.getInteger("FinishedTimes");
			long lastFinishTime = questData.getLong("LastFinishTime");
			boolean rewardTaken = questData.getBoolean("RewardTaken");
			return new QuestFinishData(p, quest, finishedTimes, lastFinishTime, rewardTaken);
		}else {
			
			int finishedTimes = questData.getInteger("FinishedTimes");
			long lastFinishTime = questData.getLong("LastFinishTime");
			boolean rewardTaken = questData.getBoolean("RewardTaken");
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "CommandInfo.IncompatQuest",questID,p.getDisplayName() ));			
			return new IncompatibleQuestFinishData(p,questID,finishedTimes,lastFinishTime,rewardTaken);
		}
	}

	private static Set<QuestProgress> getProgressSet(Player p, Document data) {
		Set<QuestProgress> quests = new HashSet<>();
		data.get("QuestProgress", Document.class).forEach((questID, questData) -> {
			QuestProgress q = toQuestProgress(p, questID, (Document) questData);
			if (q != null) {
				quests.add(q);
			}

		});
		return quests;
	}

	private static QuestProgress toQuestProgress(Player player, String questID, Document data) {
		Quest q = QuestUtil.getQuest(questID);
		boolean incompatQuest = q == null;
		if (incompatQuest && !ConfigSettings.differentialquestsserver) {
			DebugHandler.log(0,
					"[Mongodb] Found an invalid quest: %s, enable differential quest system or accept the error...",
					questID);
			return null;
		}
		if (!incompatQuest) {
			long version = data.getLong("Version");
			if (q.getVersion().getTimeStamp() != version) {
				QuestChatManager.error(player,
						I18n.locMsg(null, "CommandInfo.OutdatedQuestVersion", q.getInternalID()));
				return null;
			}

			int stage = data.getInteger("Stage");
			long takeStamp = data.getLong("TakeStamp");

			Iterator<SimpleQuestObject> objIter = q.getStage(stage).getObjects().iterator();
			List<QuestObjectProgress> objectives = new ArrayList<>();
			try {
				Iterator<Integer> progressIter = data.getList("ObjectProgress", Integer.class).iterator();

				while (objIter.hasNext() && progressIter.hasNext()) {
					objectives.add(new QuestObjectProgress(objIter.next(), progressIter.next()));
				}
			} catch (ClassCastException e) {
				// if the objective array uses new data storage format(string)
				Iterator<String> universalIter = data.getList("ObjectProgress", String.class).iterator();
				while (objIter.hasNext() && universalIter.hasNext()) {
					try {
						int progressInt = Integer.parseInt(universalIter.next());
						objectives.add(new QuestObjectProgress(objIter.next(), progressInt));
					} catch (NumberFormatException e0) {
						try {
							double progressD = Double.parseDouble(universalIter.next());
							objectives.add(new QuestObjectProgress(objIter.next(), progressD));
						} catch (NumberFormatException e1) {
							QuestChatManager.logCmd(Level.SEVERE,
									I18n.locMsg(null, "CommandInfo.MongoDBInvalidPorgress", questID));
							return null;
						}
					}
				}
			}

			objectives.forEach(QuestObjectProgress::checkIfFinished);
			if(data.containsKey("LastInvokedLogin")) {
				Map<Integer,Long> lastinvokedlogin = ((BasicDBObject)data.get("lastInvokedLogin")).toMap();
				int loginObjCount = 0;
				for(int i = 0;i < objectives.size();i++) {
					if(lastinvokedlogin.containsKey(loginObjCount)) {
						objectives.get(i).setLastInvokedMilli(lastinvokedlogin.get(loginObjCount));
					}
					loginObjCount++;
				}
			}
			return new QuestProgress(q, player, stage, objectives, takeStamp);
		} else {

			int stage = data.getInteger("Stage");
			long takeStamp = data.getLong("TakeStamp");

			
			List<IncompatibleQuestObjectProgress> objectives = new ArrayList<>();
			try {
				Iterator<Integer> progressIter = data.getList("ObjectProgress", Integer.class).iterator();

				while ( progressIter.hasNext()) {
					objectives.add(new IncompatibleQuestObjectProgress(stage, progressIter.next()));
				}
			} catch (ClassCastException e) {
				// if the objective array uses new data storage format(string)
				Iterator<String> universalIter = data.getList("ObjectProgress", String.class).iterator();
				while ( universalIter.hasNext()) {
					try {
						int progressInt = Integer.parseInt(universalIter.next());
						objectives.add(new IncompatibleQuestObjectProgress(stage, progressInt));
					} catch (NumberFormatException e0) {
						try {
							double progressD = Double.parseDouble(universalIter.next());
							objectives.add(new IncompatibleQuestObjectProgress(stage, progressD));
						} catch (NumberFormatException e1) {
							QuestChatManager.logCmd(Level.SEVERE,
									I18n.locMsg(null, "CommandInfo.MongoDBInvalidPorgress", questID));
							return null;
						}
					}
				}
			}

			if(data.containsKey("LastInvokedLogin")) {
				Map<Integer,Long> lastinvokedlogin = ((BasicDBObject)data.get("lastInvokedLogin")).toMap();
				int loginObjCount = 0;
				for(int i = 0;i < objectives.size();i++) {
					if(lastinvokedlogin.containsKey(loginObjCount)) {
						objectives.get(i).setLastInvokedMilli(lastinvokedlogin.get(loginObjCount));
					}
					loginObjCount++;
				}
			}
			QuestProgress qp = new QuestProgress(questID, player, stage, objectives, takeStamp);
			qp.setIncompatibleMode(true);
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "CommandInfo.IncompatQuest",questID,player.getDisplayName() ));
			qp.setIncompatQuestVersion(data.getLong("Version"));
			return qp;
		}
	}

	private static HashMap<Integer, Integer> getFriendPoints(Document data) {
		HashMap<Integer, Integer> friendPoints = new HashMap<>();
		data.get("FriendPoints", Document.class)
				.forEach((key, value) -> friendPoints.put(Integer.parseInt(key), (Integer) value));

		return friendPoints;
	}

}
