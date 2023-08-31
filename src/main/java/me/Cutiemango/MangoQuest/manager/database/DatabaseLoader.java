package me.Cutiemango.MangoQuest.manager.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.data.IncompatibleQuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.model.Quest;

public class DatabaseLoader
{

	public static void loadPlayer(QuestPlayerData pd) {
		try {
			Connection conn = DatabaseManager.getConnection();
			PreparedStatement query = conn.prepareStatement("SELECT * FROM `mq_playerdata` WHERE `UUID` = ?");
			Player p = pd.getPlayer();
			query.setNString(1, p.getUniqueId().toString());

			ResultSet rsRet = query.executeQuery();
			// if player data found in database
			if (rsRet.next()) {
				int PDID = rsRet.getInt("PDID");
				Set<QuestProgress> prog = getQuestProgress(p, PDID);
				Set<QuestFinishData> data = getFinishedData(p,PDID);
				Set<String> convs = getFinishedConversations(PDID);
				HashMap<Integer, Integer> fp = getFriendPointMap(PDID);

				DebugHandler.log(2, "[Database] Player %s's data has been loaded from the database.", p.getName());
				pd.loadExistingData(prog, data, convs, fp, PDID);
			} else {
				// no previous player data found in database, try to migrate data from yml file
				// migration support will be moved to be command-based next update
				File f = new File(Main.getInstance().getDataFolder() + "/data/", p.getUniqueId() + ".yml");
				DatabaseSaver.saveLoginData(p);
				// get PDID
				pd.load(ConfigSettings.SaveType.SQL);

				// found yml data, start migrating
				//continue it ffs, cuz idc
				if (f.exists()) {
					DebugHandler.log(2, "[Database] Found player %s's yml data, start migrating...", p.getName());
					// load from yml
					pd.load(ConfigSettings.SaveType.YML);

					// save yml data to database
					DatabaseSaver.savePlayerData(pd);
					DebugHandler.log(2, "[Database] Migration completed.");
				}
				//
			}
			query.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Set<QuestProgress> getQuestProgress(Player p, int PDID) {
		Set<QuestProgress> prog = new HashSet<>();
		Connection conn = DatabaseManager.getConnection();

		try {
			PreparedStatement query = conn.prepareStatement("SELECT * FROM `mq_questprogress` WHERE `PDID` = ?");
			query.setInt(1, PDID);

			ResultSet rsRet = query.executeQuery();
			while (rsRet.next()) {
				DebugHandler.log(5, "[Database] Found quest progress  for player %s", p);
				boolean incompatibleQuest = QuestUtil.getQuest(rsRet.getString("QuestID")) == null;
				if (incompatibleQuest && !ConfigSettings.differentialquestsserver) {
					//DebugHandler.log(2, "[Database] Found an invalid quest (%s) in player's quest data, skipping...", rsRet.getString("QuestID"));
					DebugHandler.log(0, I18n.locMsg(null, "CmdLog.NoValidQuest",rsRet.getString("QuestID"),"{loading quests into cache from sql}"));
					continue;
				}
				if(!incompatibleQuest) {
					//DebugHandler.log(0, I18n.locMsg(null, "CmdLog.NoValidQuest",rsRet.getString("QuestID"),"{loading quests into cache from sql}"));
					Quest q = QuestUtil.getQuest(rsRet.getString("QuestID"));
					long version = rsRet.getLong("Version");

					if (q.getVersion().getTimeStamp() != version) {
						QuestChatManager.error(p, I18n.locMsg(null,"CommandInfo.OutdatedQuestVersion", q.getInternalID()));
						continue;
					}

					int stage = rsRet.getInt("QuestStage");
					long takeStamp = rsRet.getLong("TakeStamp");

					QuestProgress progress = new QuestProgress(q, p, stage,
							JSONSerializer.jsonDeserialize(q.getStage(stage).getObjects(), rsRet.getString("QuestObjectProgress")), takeStamp);
					prog.add(progress);
				}else {
					String quest = rsRet.getString("QuestID");
					int stage = rsRet.getInt("QuestStage");
					long takeStamp = rsRet.getLong("TakeStamp");
					QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "CommandInfo.IncompatQuest",quest,p.getDisplayName()));
					QuestProgress progress = new QuestProgress(quest,p,stage,JSONSerializer.jsonDeserializeIncompatible(rsRet.getString("QuestObjectProgress")),takeStamp);
					progress.setIncompatQuestVersion(rsRet.getLong("Version"));
					progress.setIncompatibleMode(true);
					prog.add(progress);
				}
			}
			query.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return prog;
	}

	private static Set<QuestFinishData> getFinishedData(Player p,int PDID) {
		Set<QuestFinishData> data = new HashSet<>();
		Connection conn = DatabaseManager.getConnection();

		try {
			PreparedStatement query = conn.prepareStatement("SELECT * FROM `mq_finishedquest` WHERE `PDID` = ?");
			query.setInt(1, PDID);

			ResultSet rsRet = query.executeQuery();
			while (rsRet.next()) {
				boolean incompatibleQuest = QuestUtil.getQuest(rsRet.getString("QuestID")) == null;
				DebugHandler.log(5, "[Database] Found quest finished data for player %s", p);
				if (incompatibleQuest && !ConfigSettings.differentialquestsserver) {
					DebugHandler.log(0, I18n.locMsg(null, "CmdLog.NoValidQuest",rsRet.getString("QuestID"),"{loading quest finish data into cache from sql}"));
					//DebugHandler.log(2, "[Database] Found an invalid quest (%s) in player's quest data, skipping...", rsRet.getString("QuestID"));
					continue;
				}
				if(!incompatibleQuest) {
				Quest q = QuestUtil.getQuest(rsRet.getString("QuestID"));
				data.add(new QuestFinishData(p,q, rsRet.getInt("FinishedTimes"), rsRet.getLong("LastFinishTime"), rsRet.getInt("RewardTaken") != 0));
				}else {
					String q = rsRet.getString("QuestID");
					data.add(new IncompatibleQuestFinishData(p,q,rsRet.getInt("FinishedTimes"), rsRet.getLong("LastFinishTime"), rsRet.getInt("RewardTaken") != 0));
				}
			}
			query.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	private static HashMap<Integer, Integer> getFriendPointMap(int PDID) {
		HashMap<Integer, Integer> map = new HashMap<>();
		Connection conn = DatabaseManager.getConnection();

		try {
			DebugHandler.log(5, "[Database] Found friendpoint data for player ");
			PreparedStatement query = conn.prepareStatement("SELECT * FROM `mq_friendpoint` WHERE `PDID` = ?");
			query.setInt(1, PDID);

			ResultSet rsRet = query.executeQuery();
			while (rsRet.next())
				map.put(rsRet.getInt("NPC"), rsRet.getInt("FriendPoint"));
			query.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	private static Set<String> getFinishedConversations(int PDID) {
		Set<String> set = new HashSet<>();
		Connection conn = DatabaseManager.getConnection();

		try {
			PreparedStatement query = conn.prepareStatement("SELECT * FROM `mq_finishedconv` WHERE `PDID` = ?");
			query.setInt(1, PDID);

			ResultSet rsRet = query.executeQuery();
			while (rsRet.next()) {
				DebugHandler.log(5, "[Database] Found finished convs");
				String convID = rsRet.getString("ConvID");
				if (ConversationManager.getConversation(convID) == null) {
					DebugHandler.log(2, "[Database] Found an invalid conversation (%s) in player's data, skipping...", convID);
					continue;
				}
				set.add(convID);
			}
			query.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return set;
	}
}
