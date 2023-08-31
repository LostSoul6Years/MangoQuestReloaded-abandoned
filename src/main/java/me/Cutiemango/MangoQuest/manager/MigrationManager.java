package me.Cutiemango.MangoQuest.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MigrationManager {

	// public MigrationManager() {

	// }
	@Nullable
	public static CompletableFuture<List<YamlConfiguration>> convertSQLToYml(Connection conn) {
		// if (directory.isDirectory()) {
		// return null;
		// }
		try {
			if (conn == null || conn.isClosed()) {
				return null;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
		return CompletableFuture.supplyAsync(() -> {
			List<YamlConfiguration> datas = new ArrayList<>();

			try {
				PreparedStatement pollPd = conn.prepareStatement("SELECT * FROM mq_playerdata");

				ResultSet pollPdr = pollPd.executeQuery();
				while (pollPdr.next()) {
					int PDID = pollPdr.getInt("PDID");
					YamlConfiguration pdata = new YamlConfiguration();
					String LastKnownID = pollPdr.getString("LastKnownID");
					;
					String UUID = pollPdr.getString("UUID");
					pdata.set("LastKnownID", LastKnownID);
					pdata.set("UUID", UUID);
					PreparedStatement pollQp = conn.prepareStatement("SELECT * FROM mq_questprogress WHERE PDID = ?");
					pollQp.setInt(1, PDID);
					ResultSet pollQpr = pollQp.executeQuery();
					while (pollQpr.next()) {
						String QuestID = pollQpr.getString("QuestID");
						long version = pollQpr.getLong("Version");
						int QuestStage = pollQpr.getInt("QuestStage");
						long TakeStamp = pollQpr.getLong("TakeStamp");
						pdata.set("QuestProgress." + QuestID + ".QuestStage", QuestStage);
						pdata.set("QuestProgress." + QuestID + ".Version", version);
						pdata.set("QuestProgress." + QuestID + ".TakeStamp", TakeStamp);
						String jsonfiedProgress = pollQpr.getString("QuestObjectProgress");
						JSONParser parser = new JSONParser();
						try {
							JSONObject json = (JSONObject) parser.parse(jsonfiedProgress);
							for (int i = 0; i < json.keySet().size() / 2; i++) {
								String rawProgress = json.get("" + i).toString();
								String rawLastInvokedMilli = json.get(i + "lastinvokedmilli").toString();
								try {
									pdata.set("QuestProgress." + QuestID + ".QuestObjectProgress." + i,
											Integer.parseInt(rawProgress));
								} catch (NumberFormatException e) {
									pdata.set("QuestProgress." + QuestID + ".QuestObjectProgress." + i,
											Double.parseDouble(rawProgress));
								}
								pdata.set(
										"QuestProgress." + QuestID + ".QuestObjectProgress._" + i + "_lastinvokedmilli",
										rawLastInvokedMilli);

							}

						} catch (ParseException e) {
							QuestChatManager.logCmd(Level.SEVERE,
									"Corrupted SQL JSON Objective while converting sql to yml,aborting migration....");
							pollQp.close();
							pollPd.close();
							return null;
						}
						pollQpr.close();
						pollQp.close();
					}
					PreparedStatement pollFq = conn.prepareStatement("SELECT * FROM mq_finishedquest WHERE PDID = ?");
					pollFq.setInt(1, PDID);
					ResultSet pollFqr = pollFq.executeQuery();
					while (pollFqr.next()) {
						String QuestID = pollFqr.getString("QuestID");
						int FinishedTimes = pollFqr.getInt("FinishedTimes");
						long LastFinishTime = pollFqr.getLong("LastFinishTime");
						int RewardTaken = pollFqr.getInt("RewardTaken");
						boolean RewardTakenBool = RewardTaken == 1?true:false;
						pdata.set("FinishedQuest."+ QuestID + ".FinishedTimes", FinishedTimes);
						pdata.set("FinishedQuest." + QuestID + ".LastFinishTime", LastFinishTime);
						pdata.set("FinishedQuest." + QuestID + ".RewardTaken", RewardTakenBool);
					}
					pollFqr.close();
					pollFq.close();
					PreparedStatement pollFp = conn.prepareStatement("SELECT * FROM mq_friendpoint WHERE PDID = ?");
					pollFp.setInt(1, PDID);
					ResultSet pollFpr = pollFp.executeQuery();
					while (pollFpr.next()) {
						int NPC = pollFpr.getInt("NPC");
						int FriendPoint = pollFpr.getInt("FriendPoint");
						pdata.set("FriendPoint." + NPC, FriendPoint);
					}
					pollFpr.close();
					pollFp.close();
					PreparedStatement pollFc = conn.prepareStatement("SELECT * FROM mq_finishedconv WHERE PDID = ?");
					pollFc.setInt(1, PDID);
					ResultSet pollFcr = pollFc.executeQuery();
					List<String> finishedConvs = new ArrayList<>();
					while (pollFcr.next()) {
						finishedConvs.add(pollFcr.getString("ConvID"));
					}
					pollFcr.close();
					pollFc.close();
					pdata.set("FinishedConversation", finishedConvs);
					datas.add(pdata);
					
				}
				pollPdr.close();
				pollPd.close();
			} catch (SQLException e) {
				e.printStackTrace();			
				return null;
			}

			return datas;
		});
	}

	public static CompletableFuture<Boolean> convertYmltoSQL(Map<String, YamlConfiguration> ymls, Connection conn) {
		return CompletableFuture.supplyAsync(() -> {
			if (conn == null) {
				return false;
			}
			try {

				PreparedStatement playerData = conn.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `mq_playerdata`(" +
							"    `PDID` INTEGER PRIMARY KEY AUTOINCREMENT ," +
							"    `LastKnownID` VARCHAR(16) NOT NULL DEFAULT '' ," +
							"    `UUID` VARCHAR(36) NOT NULL DEFAULT '' ," +
							//"    PRIMARY KEY (`PDID`)," +
							"    UNIQUE(`UUID`)" +
							") " +
							//"ENGINE=InnoDB " +
							//"DEFAULT CHARSET=utf8mb4 " +
							//"COLLATE=utf8mb4_unicode_ci " +
							//AUTO_INCREMENT=0
							"");
				playerData.execute();
				playerData.close();

				// Table questProgress
				PreparedStatement questProgress = conn.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `mq_questprogress`(" +
							"    `QPDID` INTEGER PRIMARY KEY AUTOINCREMENT ," +
							"    `PDID` INTEGER NOT NULL ," +
							"    `QuestID` VARCHAR(128) NOT NULL DEFAULT '' ," +
							"    `QuestStage` INTEGER NOT NULL ," +
							"    `Version` BIGINT NOT NULL DEFAULT 0 ," +
							"    `TakeStamp` BIGINT NOT NULL DEFAULT 0," +
							"    `QuestObjectProgress` VARCHAR(1023) NOT NULL DEFAULT '' ," +
							//"    PRIMARY KEY (`QPDID`)," +
							"    UNIQUE(`QPDID`)" +
							") " +
							//"ENGINE=InnoDB " +
							//"DEFAULT CHARSET=utf8mb4 " +
							//"COLLATE=utf8mb4_unicode_ci " +
							//AUTO_INCREMENT=0
							"");
				questProgress.execute();
				questProgress.close();

				// Table questProgress
				PreparedStatement finishedQuest = conn.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `mq_finishedquest`(" +
							"    `FQID` INTEGER PRIMARY KEY AUTOINCREMENT," +
							"    `PDID` INTEGER NOT NULL," +
							"    `QuestID` VARCHAR(128) NOT NULL DEFAULT ''," +
							"    `FinishedTimes` INTEGER NOT NULL ," +
							"    `LastFinishTime` BIGINT NOT NULL DEFAULT 0 ," +
							"    `RewardTaken` INTEGER NOT NULL DEFAULT 0 ," +
							//"    PRIMARY KEY (`FQID`)," +
							"    UNIQUE(`FQID`)" +
							") " +
							//"ENGINE=InnoDB " +
							//"DEFAULT CHARSET=utf8mb4 " +
							//"COLLATE=utf8mb4_unicode_ci " +
							//AUTO_INCREMENT=0
							"");
				finishedQuest.execute();
				finishedQuest.close();

				// Table friendPoint
				PreparedStatement friendPoint = conn.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `mq_friendpoint`(" +
							"	`FPID` INTEGER PRIMARY KEY AUTOINCREMENT ," +
							"	`PDID` INTEGER NOT NULL ," +
							"	`NPC` INTEGER NOT NULL ," +
							"	`FriendPoint` INTEGER DEFAULT 0 NOT NULL ," +
							//"	PRIMARY KEY (`FPID`)," +
							"	UNIQUE(`FPID`)" +
							") " +
							//"ENGINE=InnoDB " +
							//"DEFAULT CHARSET=utf8mb4 " +
							//"COLLATE=utf8mb4_unicode_ci " +
							//AUTO_INCREMENT=0
							"");
				friendPoint.execute();
				friendPoint.close();

				// Table finishedConv
				PreparedStatement finishedConv = conn.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `mq_finishedconv`(" +
							"	`FCID` INTEGER PRIMARY KEY AUTOINCREMENT ," +
							"	`PDID` INTEGER NOT NULL ," +
							"	`ConvID` VARCHAR(128) NOT NULL DEFAULT ''," +
							//"	PRIMARY KEY (`FCID`)," +
							"	UNIQUE(`FCID`)" +
							") " +
							//"ENGINE=InnoDB " +
							//"DEFAULT CHARSET=utf8mb4 " +
							//"COLLATE=utf8mb4_unicode_ci " +
							//AUTO_INCREMENT=0
							"");
				finishedConv.execute();
				finishedConv.close();
				for (Entry<String, YamlConfiguration> entry : ymls.entrySet()) {
					//PreparedStatement select1 = conn.prepareStatement("SELECT * FROM mq_playerdata");

					String UUID = entry.getKey();
					YamlConfiguration yml = entry.getValue();
					String LastKnownID = yml.getString("LastKnownID");
					PreparedStatement insertPD = conn.prepareStatement("INSERT INTO mq_playerdata (UUID, LastKnownID) values (?, ?)");
					insertPD.setString(1, UUID);
					insertPD.setString(2, LastKnownID);
					insertPD.execute();
					insertPD.close();
					PreparedStatement fetchPDID = conn.prepareStatement("SELECT * FROM mq_playerdata WHERE UUID = ?");
					fetchPDID.setString(1, UUID);
					ResultSet PDIDResult = fetchPDID.executeQuery();
					int PDID;
					if(PDIDResult.next()) {
						PDID = PDIDResult.getInt("PDID");
					}else {
						QuestChatManager.logCmd(Level.SEVERE,
								"Error while saving to db file (no PDID,please report to developer,aborting...");
						return false;
					}
					PDIDResult.close();
					fetchPDID.close();
					
					if (yml.getConfigurationSection("FinishedQuest") != null) {
						//PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedquest");

						for (String QuestID : yml.getConfigurationSection("FinishedQuest").getKeys(false)) {
							PreparedStatement insert = conn.prepareStatement(
									"INSERT INTO mq_finishedquest (PDID, QuestID, LastFinishTime, FinishedTimes, RewardTaken) values (?, ?, ?, ?, ?)");
							insert.setInt(1, PDID);
							insert.setString(2, QuestID);
							insert.setLong(3, yml.getLong("FinishedQuest." + QuestID + ".LastFinishTime"));
							insert.setInt(4, yml.getInt("FinishedQuest." + QuestID+ ".FinishedTimes"));
							insert.setBoolean(5, yml.getBoolean("FinishedQuest." + QuestID + ".RewardTaken"));
							insert.execute();
						}
					}
					if(yml.getConfigurationSection("QuestProgress")!=null) {
						//PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_questprogress");
						for(String QuestID:yml.getConfigurationSection("QuestProgress").getKeys(false)) {
							//String id = QuestID;
						    PreparedStatement insert = conn.prepareStatement(
									"INSERT INTO mq_questprogress (PDID, QuestID, QuestObjectProgress, QuestStage, TakeStamp, Version) values (?, ?, ?, ?, ?, ?)");
							insert.setInt(1, PDID);
							insert.setString(2, QuestID);
														
														
							JSONObject json = new JSONObject();
							Set<String> keys = yml.getConfigurationSection("QuestProgress." + QuestID + ".QuestObjectProgress").getKeys(false);
							for(int i = 0;i < keys.size()/2;i++) {
								long lastinvokedmilli = -1;
								if(yml.isLong("_"+i+"_lastinvokedmilli")) {
									lastinvokedmilli = yml.getLong("_"+i+"_lastinvokedmilli");
								}//else if(yml.isString("_"+i+"_lastinvokedmilli")) {
									//lastinvokedmilli = Long.parseLong(yml.getString("_"+i+"_lastinvokedmilli"));
								 else {
									QuestChatManager.logCmd(Level.SEVERE,
											"Corrupted YML file while trying to convert into DB file,Aborting...");
									return false;
								}
								json.put("_"+i+"_lastinvokedmilli", lastinvokedmilli);
								if(yml.isDouble(i+"")) {
									json.put(i+"", yml.getDouble(i+""));
								}else if(yml.isInt(i+"")) {
									json.put(i+"", yml.getInt(i+""));
								}
							}
							insert.setString(3, json.toJSONString());							
							insert.setInt(4, yml.getInt("QuestProgress." + QuestID + ".QuestStage"));
							insert.setLong(5, yml.getLong("QuestProgress." + QuestID + ".TakeStamp"));
							insert.setLong(6, yml.getLong("QuestProgress." + QuestID + ".Version"));
							insert.execute();
							insert.close();
						}
					}
					if(yml.getConfigurationSection("FriendPoint")!=null) {
						//PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_friendpoint");
	
						for(String NPC:yml.getConfigurationSection("FriendPoint").getKeys(false)) {
							int npcNumber = Integer.parseInt(NPC);
							if(!yml.isInt("FriendPoint."+NPC)) {
								QuestChatManager.logCmd(Level.SEVERE,
										"Corrupted YML file while trying to convert into DB file,Aborting...");
								return false;
							}
							
							PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_friendpoint (PDID, NPC, FriendPoint) values (?, ?, ?)");
							insert.setInt(1, PDID);
							insert.setInt(2, npcNumber);
							insert.setInt(3, yml.getInt("FriendPoint."+NPC));							
							insert.execute();
							insert.close();
						}
							
					}
					if(yml.isList("FinishedConversation")) {
							//PreparedStatement select = conn.prepareStatement("SELECT * FROM mq_finishedconv");

							
							for(String convID:yml.getStringList("FinishedConversation")) {
								PreparedStatement insert = conn.prepareStatement("INSERT INTO mq_finishedconv (PDID, ConvID) values (?, ?)");
								insert.setInt(1, PDID);
								insert.setString(2, convID);
								insert.execute();
								insert.close();
							}
						}
					
				}
			} catch (Exception e) {
				QuestChatManager.logCmd(Level.SEVERE,
						"Corrupted YML file while trying to convert into DB file,Aborting...");
				e.printStackTrace();
				return false;
			}
			return true;
		});
	}

}
