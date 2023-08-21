package me.Cutiemango.MangoQuest.manager.database;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;


public class DatabaseSaverBackup
{
	private Connection conn;	
	public DatabaseSaverBackup(Connection conn) {
		this.conn = conn;
	}
	
	public  void savePlayerData(QuestPlayerData pd)
	{
		int PDID = pd.getPDID();
		saveLoginData(pd.getPlayer());
		
		pd.getFinishQuests().forEach(questData -> saveFinishedQuest(questData, PDID));
		pd.getProgresses().forEach(questProgress -> saveQuestProgress(questProgress, PDID));
		pd.getFriendPointStorage().forEach((id, value) -> saveFriendPoint(id, value, PDID));
		pd.getFinishedConversations().forEach(convID -> saveFinishedConversation(convID, PDID));
		
		removeFinishedQuests(pd.getProgresses(), PDID);
	}

	public  void saveLoginData(Player p)
	{
		
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_playerdata WHERE UUID = ?");
			select.setString(1, p.getUniqueId().toString());

			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"update player data for %s",p.getName());
				PreparedStatement update = conn.prepareStatement("UPDATE mq_playerdata set LastKnownID = ? WHERE UUID = ?");
				update.setString(1, p.getName());
				update.setString(2, p.getUniqueId().toString());
				update.execute();
			}
			else
			{
				DebugHandler.log(5,"insert player data for %s",p.getName());
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_playerdata (UUID, LastKnownID) values (?, ?)");
				insert.setString(1, p.getUniqueId().toString());
				insert.setString(2, p.getName());
				insert.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public  void saveFriendPoint(int npc, int friendPoint, int PDID)
	{
		
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
			}
			else
			{
				DebugHandler.log(5,"insert friendpoint");
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_friendpoint (PDID, NPC, FriendPoint) values (?, ?, ?)");
				insert.setInt(1, PDID);
				insert.setInt(2, npc);
				insert.setInt(3, friendPoint);
				insert.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public  void saveFinishedConversation(String convID, int PDID)
	{
		
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedconv WHERE PDID = ? AND ConvID = ?");
			select.setInt(1, PDID);
			select.setString(2, convID);

			if (!select.executeQuery().next())
			{
				DebugHandler.log(5,"insert finished convs");
				PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_finishedconv (PDID, ConvID) values (?, ?)");
				insert.setInt(1, PDID);
				insert.setString(2, convID);
				insert.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public  void saveQuestProgress(QuestProgress questProgress, int PDID)
	{
		
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_questprogress WHERE PDID = ? AND QuestID = ?");
			select.setInt(1, PDID);
			select.setString(2, questProgress.getQuest().getInternalID());

			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"update questprogress");
				PreparedStatement update = conn.prepareStatement(
						"UPDATE mq_questprogress set QuestObjectProgress = ?, QuestStage = ?, Version = ? WHERE PDID = ? AND QuestID = ?");
				update.setString(1, JSONSerializer.jsonSerialize(questProgress.getCurrentObjects()));
				update.setInt(2, questProgress.getCurrentStage());
				update.setLong(3, questProgress.getQuest().getVersion().getTimeStamp());
				update.setInt(4, PDID);
				update.setString(5, questProgress.getQuest().getInternalID());
				update.execute();
			}
			else
			{
				DebugHandler.log(5,"insert quest progress");
				PreparedStatement insert = conn.prepareStatement(
						"INSERT INTO mq_questprogress (PDID, QuestID, QuestObjectProgress, QuestStage, TakeStamp, Version) values (?, ?, ?, ?, ?, ?)");
				insert.setInt(1, PDID);
				insert.setString(2, questProgress.getQuest().getInternalID());
				insert.setString(3, JSONSerializer.jsonSerialize(questProgress.getCurrentObjects()));
				insert.setInt(4, questProgress.getCurrentStage());
				insert.setLong(5, questProgress.getTakeTime());
				insert.setLong(6, questProgress.getQuest().getVersion().getTimeStamp());
				insert.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public  void removeFinishedQuests(Set<QuestProgress> progresses, int PDID)
	{
		
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
					delete.setString(2, questID);
					delete.execute();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public  void saveFinishedQuest(QuestFinishData questData, int PDID)
	{
		
		try
		{
			PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedquest WHERE PDID = ? AND QuestID = ?");
			select.setInt(1, PDID);
			select.setString(2, questData.getQuest().getInternalID());
			if (select.executeQuery().next())
			{
				DebugHandler.log(5,"save finishedquest");
				PreparedStatement update = conn.prepareStatement(
						"UPDATE mq_finishedquest set FinishedTimes = ?, RewardTaken = ?, LastFinishTime = ? WHERE PDID = ? AND QuestID = ?");
				update.setInt(1, questData.getFinishedTimes());
				update.setInt(2, questData.isRewardTaken() ? 1 : 0);
				update.setLong(3, questData.getLastFinish());
				update.setInt(4, PDID);
				update.setString(5, questData.getQuest().getInternalID());
				update.execute();
			}
			else
			{
				DebugHandler.log(5,"insert friendpoint");
				PreparedStatement insert = conn.prepareStatement(
						"INSERT INTO mq_finishedquest (PDID, QuestID, LastFinishTime, FinishedTimes, RewardTaken) values (?, ?, ?, ?, ?)");
				insert.setInt(1, PDID);
				insert.setString(2, questData.getQuest().getInternalID());
				insert.setLong(3, questData.getLastFinish());
				insert.setInt(4, questData.getFinishedTimes());
				insert.setBoolean(5, questData.isRewardTaken());
				insert.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}