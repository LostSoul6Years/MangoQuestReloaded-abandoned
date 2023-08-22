package me.Cutiemango.MangoQuest.manager.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bukkit.entity.Player;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import me.Cutiemango.MangoQuest.data.IncompatibleQuestFinishData;
import me.Cutiemango.MangoQuest.data.IncompatibleQuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;

public class MongodbSaver {
	public static void savePlayerData(QuestPlayerData pd) {
		MongoCollection<Document> collection = MongodbManager.getCollection();

		Player p = pd.getPlayer();
		Document playerData = new Document();
		playerData.append("UUID", p.getUniqueId().toString()).append("LastKnownID", p.getName())
				.append("FinishedConv", pd.getFinishedConversations())
				.append("FinishedQuests", getFinishedQuestsBSON(pd.getFinishQuests()))
				.append("FriendPoints",
						pd.getFriendPointStorage().entrySet().stream()
								.collect(Collectors.toMap(kv -> kv.getKey().toString(), Map.Entry::getValue)))
				.append("QuestProgress", getQuestProgressBSON(pd.getProgresses()));

		Document res = collection.findOneAndReplace(new Document("UUID", p.getUniqueId().toString()), playerData);

		// no entry to replace
		if (res == null)
			collection.insertOne(playerData);
	}

	private static Document getQuestProgressBSON(Set<QuestProgress> quests) {
		Document data = new Document();
		for (QuestProgress q : quests) {
			if(!q.isIncompatibleMode()) {
				data.put(q.getQuest().getInternalID(), getQuestDataBSON(q));
			}else {
				data.put(q.getIncompatQuest(), getQuestDataBSON(q));
			}
		}
		return data;
	}

	private static Document getFinishedQuestsBSON(Set<QuestFinishData> quests) {

		Document data = new Document();

		for (QuestFinishData q : quests) {
			if (q instanceof IncompatibleQuestFinishData) {
				IncompatibleQuestFinishData iqfd = (IncompatibleQuestFinishData) q;
				data.put(iqfd.getIncompatQuest(), getIncompatQuestDataBSON(iqfd));
			} else {
	            data.put(q.getQuest().getInternalID(), getQuestDataBSON(q));
			}
		}
		return data;
	}

	private static Document getQuestDataBSON(QuestFinishData quest) {
		Document data = new Document();
		data.put("FinishedTimes", quest.getFinishedTimes());
		data.put("LastFinishTime", quest.getLastFinish());
		data.put("RewardTaken", quest.isRewardTaken());
		return data;
	}
	private static Document getIncompatQuestDataBSON(IncompatibleQuestFinishData quest) {
		Document data = new Document();
		data.put("FinishedTimes", quest.getFinishedTimes());
		data.put("LastFinishTime", quest.getLastFinish());
		data.put("RewardTaken", quest.isRewardTaken());
		return data;
	}

	private static Document getQuestDataBSON(QuestProgress quest) {
		Document data = new Document();
		if (!quest.isIncompatibleMode()) {

			data.put("Stage", quest.getCurrentStage());
			data.put("TakeStamp", quest.getTakeTime());
			data.put("Version", quest.getQuest().getVersion().getTimeStamp());

			// deprecated save supporting only int type
			// data.put("ObjectProgress",
			// quest.getCurrentObjects().stream().map(QuestObjectProgress::getProgress).collect(Collectors.toList()));

			// new format saves all stuffs as string to support double+int
			List<String> universalData = new ArrayList<String>();
			Map<Integer,Long> lastinvokedlogin = new HashMap<>();
			int count = 0;
			for (QuestObjectProgress qop : quest.getCurrentObjects()) {
				if (qop.getProgressD() > 0) {
					universalData.add(qop.getProgressD() + "");
				} else {
					universalData.add(qop.getProgress() + "");
				}
				lastinvokedlogin.put(count,qop.getLastInvokedMilli());
				count++;
			}
			
			
			data.put("ObjectProgress", universalData);
			data.put("LastInvokedLogin", new BasicDBObject(lastinvokedlogin));
			
			return data;
		} else {

			data.put("Stage", quest.getCurrentStage());
			data.put("TakeStamp", quest.getTakeTime());
			data.put("Version", quest.getIncompatQuestVersion());

			// deprecated save supporting only int type
			// data.put("ObjectProgress",
			// quest.getCurrentObjects().stream().map(QuestObjectProgress::getProgress).collect(Collectors.toList()));

			// new format saves all stuffs as string to support double+int
			List<String> universalData = new ArrayList<String>();
			Map<Integer,Long> lastinvokedlogin = new HashMap<>();
			int count = 0;
			for (IncompatibleQuestObjectProgress qop : quest.getIncompatObjList()) {
				if (qop.getProgressD() > 0) {
					universalData.add(qop.getProgressD() + "");
				} else {
					universalData.add(qop.getProgress() + "");
				}
				lastinvokedlogin.put(count,qop.getLastInvokedMilli());
				count++;
			}
			data.put("ObjectProgress", universalData);
			//data.put("LastInvokedLogin", lastinvokedlogin);
			data.put("LastInvokedLogin", new BasicDBObject(lastinvokedlogin));
			return data;
		}
	}

}
