package me.Cutiemango.MangoQuest.manager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.data.IncompatibleQuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;


public class DatabaseSaver
{
	
	public static void savePlayerData(QuestPlayerData pd)
	{
		int PDID = pd.getPDID();
		saveLoginData(pd.getPlayer());
		
		pd.getFinishQuests().forEach(questData -> saveFinishedQuest(questData, PDID));
		pd.getProgresses().forEach(questProgress -> saveQuestProgress(questProgress, PDID));
		pd.getFriendPointStorage().forEach((id, value) -> saveFriendPoint(id, value, PDID));
		pd.getFinishedConversations().forEach(convID -> saveFinishedConversation(convID, PDID));
		
		removeFinishedQuests(pd.getProgresses(), PDID);
	}

	public static void saveLoginData(Player p)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_playerdata WHERE UUID = ?");
			select.setNString(1, p.getUniqueId().toString());

			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"update player data for %s",p.getName());
				PreparedStatement update = conn.prepareStatement("UPDATE mq_playerdata set LastKnownID = ? WHERE UUID = ?");
				update.setNString(1, p.getName());
				update.setNString(2, p.getUniqueId().toString());
				update.execute();
				update.close();
			}
			else
			{
				DebugHandler.log(5,"insert player data for %s",p.getName());
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_playerdata (UUID, LastKnownID) values (?, ?)");
				insert.setNString(1, p.getUniqueId().toString());
				insert.setNString(2, p.getName());
				insert.execute();
				insert.close();
			}
			select.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void saveFriendPoint(int npc, int friendPoint, int PDID)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_friendpoint WHERE PDID = ? AND NPC = ?");
			select.setInt(1, PDID);
			select.setInt(2, npc);
			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"update friendpoint");
				PreparedStatement update = conn.prepareStatement("UPDATE mq_friendpoint set FriendPoint = ? WHERE PDID = ? AND NPC = ?");
				update.setInt(1, friendPoint);
				update.setInt(2, PDID);
				update.setInt(3, npc);
				update.execute();
				update.close();
			}
			else
			{
				DebugHandler.log(5,"insert friendpoint");
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_friendpoint (PDID, NPC, FriendPoint) values (?, ?, ?)");
				insert.setInt(1, PDID);
				insert.setInt(2, npc);
				insert.setInt(3, friendPoint);
				insert.execute();
				insert.close();
			}
			select.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void saveFinishedConversation(String convID, int PDID)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedconv WHERE PDID = ? AND ConvID = ?");
			select.setInt(1, PDID);
			select.setNString(2, convID);

			if (!select.executeQuery().next())
			{
				DebugHandler.log(5,"insert finished convs");
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_finishedconv (PDID, ConvID) values (?, ?)");
				insert.setInt(1, PDID);
				insert.setNString(2, convID);
				insert.execute();
				insert.close();
			}
			select.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void saveQuestProgress(QuestProgress questProgress, int PDID)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_questprogress WHERE PDID = ? AND QuestID = ?");
			select.setInt(1, PDID);
			select.setNString(2, questProgress.getQuest().getInternalID());

			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"update questprogress");
				PreparedStatement update = conn.prepareStatement(
						"UPDATE mq_questprogress set QuestObjectProgress = ?, QuestStage = ?, Version = ? WHERE PDID = ? AND QuestID = ?");
				if(!questProgress.isIncompatibleMode()) {
				update.setNString(1, JSONSerializer.jsonSerialize(questProgress.getCurrentObjects()));
				update.setInt(2, questProgress.getCurrentStage());
				update.setLong(3, questProgress.getQuest().getVersion().getTimeStamp());
				update.setInt(4, PDID);
				update.setNString(5, questProgress.getQuest().getInternalID());
				update.execute();
				update.close();
				}else {
					update.setNString(1, JSONSerializer.jsonSerializeIncompatible(questProgress.getIncompatObjList()));
					update.setInt(2, questProgress.getCurrentStage());
					update.setLong(3, questProgress.getIncompatQuestVersion());
					update.setInt(4, PDID);
					update.setNString(5, questProgress.getIncompatQuest());
					update.execute();
					update.close();
				}
			}
			else
			{
				DebugHandler.log(5,"insert quest progress");
				PreparedStatement insert = conn.prepareStatement(
						"INSERT INTO mq_questprogress (PDID, QuestID, QuestObjectProgress, QuestStage, TakeStamp, Version) values (?, ?, ?, ?, ?, ?)");
				if(!questProgress.isIncompatibleMode()) {

					insert.setInt(1, PDID);
					insert.setNString(2, questProgress.getQuest().getInternalID());
					insert.setNString(3, JSONSerializer.jsonSerialize(questProgress.getCurrentObjects()));
					insert.setInt(4, questProgress.getCurrentStage());
					insert.setLong(5, questProgress.getTakeTime());
					insert.setLong(6, questProgress.getQuest().getVersion().getTimeStamp());
					insert.execute();
					insert.close();
				}else {
					insert.setInt(1, PDID);
					insert.setNString(2, questProgress.getIncompatQuest());
					insert.setNString(3, JSONSerializer.jsonSerializeIncompatible(questProgress.getIncompatObjList()));
					insert.setInt(4, questProgress.getCurrentStage());
					insert.setLong(5, questProgress.getTakeTime());
					insert.setLong(6, questProgress.getIncompatQuestVersion());
					insert.execute();
					insert.close();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void removeFinishedQuests(Set<QuestProgress> progresses, int PDID)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_questprogress WHERE PDID = ?");
			select.setInt(1, PDID);
			ResultSet results = select.executeQuery();
			while (results.next())
			{
				String questID = results.getString("QuestID");
				if (progresses.stream().noneMatch(questProgress -> questProgress.getQuest().getInternalID().equals(questID)))
				{
					DebugHandler.log(5,"removed finished quest");
					PreparedStatement delete = conn.prepareStatement("DELETE FROM mq_questprogress WHERE PDID = ? AND QuestID = ?");
					delete.setInt(1, PDID);
					delete.setNString(2, questID);
					delete.execute();
					delete.close();
				}
			}
			select.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void saveFinishedQuest(QuestFinishData questData, int PDID)
	{
		Connection conn = DatabaseManager.getConnection();
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedquest WHERE PDID = ? AND QuestID = ?");
			select.setInt(1, PDID);
			select.setNString(2, questData.getQuest().getInternalID());
			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"save finishedquest");
				PreparedStatement update = conn.prepareStatement(
						"UPDATE mq_finishedquest set FinishedTimes = ?, RewardTaken = ?, LastFinishTime = ? WHERE PDID = ? AND QuestID = ?");
				if(!(questData instanceof IncompatibleQuestFinishData)) {
					update.setInt(1, questData.getFinishedTimes());
					update.setInt(2, questData.isRewardTaken() ? 1 : 0);
					update.setLong(3, questData.getLastFinish());
					update.setInt(4, PDID);
					update.setNString(5, questData.getQuest().getInternalID());
					update.execute();
					update.close();
				}else {
					IncompatibleQuestFinishData iqfd = (IncompatibleQuestFinishData) questData;
					update.setInt(1, iqfd.getFinishedTimes());
					update.setInt(2, iqfd.isRewardTaken() ? 1 : 0);
					update.setLong(3, iqfd.getLastFinish());
					update.setInt(4, PDID);
					update.setNString(5, iqfd.getIncompatQuest());
					update.execute();
					update.close();
				}
			}
			else
			{
				DebugHandler.log(5,"insert friendpoint");
				PreparedStatement insert = conn.prepareStatement(
						"INSERT INTO mq_finishedquest (PDID, QuestID, LastFinishTime, FinishedTimes, RewardTaken) values (?, ?, ?, ?, ?)");
				if(!(questData instanceof IncompatibleQuestFinishData)) {
					insert.setInt(1, PDID);
					insert.setNString(2, questData.getQuest().getInternalID());
					insert.setLong(3, questData.getLastFinish());
					insert.setInt(4, questData.getFinishedTimes());
					insert.setBoolean(5, questData.isRewardTaken());
					insert.execute();
					insert.close();
				}else {
					IncompatibleQuestFinishData iqfd = (IncompatibleQuestFinishData) questData;
					insert.setInt(1, PDID);
					insert.setNString(2, iqfd.getIncompatQuest());
					insert.setLong(3, iqfd.getLastFinish());
					insert.setInt(4, iqfd.getFinishedTimes());
					insert.setBoolean(5, iqfd.isRewardTaken());
					insert.execute();
					insert.close();
				}

			}
			select.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}