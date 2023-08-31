package me.Cutiemango.MangoQuest.manager.config;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n.SupportedLanguage;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;

public class QuestConfigManager
{
	//private QuestIO translation;
	private QuestIO npcData;
	private QuestIO config;
	
	private Map<SupportedLanguage, QuestIO> langConfigs = new EnumMap<>(SupportedLanguage.class);

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
		
		//translation = new QuestIO("translations.yml", true, true, true);
		for(SupportedLanguage lang:SupportedLanguage.values()) {
			if(lang.equals(SupportedLanguage.DEFAULT)) {
				continue;
			}
			if(lang.equals(SupportedLanguage.zh_TW)) {
				langConfigs.put(lang,new QuestIO("translations_zh_TW.yml",true,true,true,"translations.yml"));
			}else {
				langConfigs.put(lang,new QuestIO("translations_"+lang.name()+".yml",true,false,false));
			}
		}
		langConfigs.put(SupportedLanguage.DEFAULT, langConfigs.get(SupportedLanguage.valueOf(ConfigSettings.LOCALE_USING.toLanguageTag().replace("-", "_"))));
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
	
	public QuestIO getTranslationFromLanguage(SupportedLanguage lang) {
		return langConfigs.get(lang);
	}
	
	public Map<SupportedLanguage,QuestIO> getLangConfigMap(){
		return langConfigs;
	}

	//public QuestIO getTranslation() {
	//	return translation;
	//}

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
