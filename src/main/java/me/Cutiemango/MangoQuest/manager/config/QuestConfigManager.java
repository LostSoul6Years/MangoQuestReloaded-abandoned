package me.Cutiemango.MangoQuest.manager.config;

import java.io.File;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;

public class QuestConfigManager
{
	private QuestIO translation;
	private QuestIO npcData;
	private QuestIO config;

	private QuestIO globalQuest;
	private QuestIO globalConv;
	private QuestIO playerData;
	private QuestIO discordSRVMessages;
	private File backups;

	public QuestIO getNpcData() {
		return npcData;
	}

	public QuestIO getGlobalConv() {
		return globalConv;
	}

	public QuestIO getPlayerData() {
		return playerData;
	}

	public File getBackups() {
		if(!backups.isDirectory()) {
			backups.mkdir();
		}
		return backups;
	}

	public QuestIO getDiscordSRVMessages() {
		return discordSRVMessages;
	}

	public void setDiscordSRVMessages(QuestIO discordSRVMessages) {
		this.discordSRVMessages = discordSRVMessages;
	}

	private static QuestConfigLoader loader;
	private static QuestConfigSaver saver;

	public QuestConfigManager() {
		
		config = new QuestIO("config.yml", false, true, false);
		
		File backupDir = new File(Main.getInstance().getDataFolder().getPath()+"/backups");
		if(!backupDir.isDirectory()) {
			backupDir.mkdir();
		}
		this.backups = backupDir;
		
		loader = new QuestConfigLoader(this);
		loader.loadConfig();
		saver = new QuestConfigSaver(this);
		
		loadFile();
	}
	
	

	public void loadFile() {
		globalQuest = new QuestIO("quest", "globalquest.yml",true,true,false);
		globalConv = new QuestIO("conversation", "globalconv.yml",true,true,false);

		translation = new QuestIO("translations.yml", true, true, true);
		npcData = new QuestIO("npc.yml", true, false, false);
		playerData = new QuestIO("playerdata.yml",true,false,false);
		discordSRVMessages = new QuestIO("discordsrvmessages.yml",true,true,false);
		
		DebugHandler.log(1, "[Config] File Loaded.");
	}

	public static QuestConfigLoader getLoader() {
		return loader;
	}
	
	public QuestIO getPlayerLang() {
		return playerData;
	}

	public static QuestConfigSaver getSaver() {
		return saver;
	}

	public QuestIO getTranslation() {
		return translation;
	}

	public QuestIO getNPC() {
		return npcData;
	}

	public QuestIO getConfig() {
		return config;
	}

	public QuestIO getGlobalQuest() {
		return globalQuest;
	}

	public QuestIO getGlobalConversation() {
		return globalConv;
	}
}
