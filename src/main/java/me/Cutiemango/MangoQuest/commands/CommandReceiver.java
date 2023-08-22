package me.Cutiemango.MangoQuest.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.I18n.SupportedLanguage;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.config.QuestConfigManager;

public class CommandReceiver implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(Main.lockDown.get()) {
			return false;
		}
		if (!(sender instanceof Player))
			return false;
		Player p = (Player) sender;
		if (args.length == 0) {
			sendHelp(p);
			return false;
		}
		switch (args[0]) {
			case "conv":
			case "c":
				ConversationCommand.execute(p, args);
				break;
			case "quest":
			case "q":
				QuestCommand.execute(p, args);
				break;
			case "editor":
			case "e":
				QuestEditorCommand.execute(p, args);
				break;
			case "conveditor":
			case "ce":
				ConversationEditorCommand.execute(p, args);
				break;
			case "lang":
			case "language":
				if(args.length == 1) {
					QuestChatManager.info(p, I18n.locMsg(p, "Language.CurrentLanguage",I18n.getPlayerLang(p).equals(I18n.SupportedLanguage.DEFAULT)?ConfigSettings.DEFAULT_LOCALE.toString():I18n.getPlayerLang(p).toString()));
					return false;
				}
				if(args.length!=2) {
					sendHelp(p);
					return false;
				}
				if(Main.getInstance().configManager.getConfig().getConfig().get("playerLanguageEnabled") == null) {
					Main.getInstance().configManager.getConfig().set("playerLanguageEnabled", true);
					Main.getInstance().configManager.getConfig().save();
				}else if(!Main.getInstance().configManager.getConfig().getBoolean("playerLanguageEnabled")) {
					QuestChatManager.info(p, I18n.locMsg(p, "CommandInfo.LanguageDisabled"));
					return false;
				}
				
				String language = args[1];
				SupportedLanguage sl = SupportedLanguage.valueOfSave(language);
				if(ConfigSettings.PLAYER_LOCALE_CHOICES.get(sl)==null) {
					sl = SupportedLanguage.DEFAULT;					
				}
				I18n.appendLangData(p.getUniqueId(), sl);
				QuestConfigManager.getSaver().savePlayerLang(p, sl);
				
				QuestChatManager.info(p,I18n.locMsg(p, "Language.Changed",sl.name()));
				return false;
			case "scoreboard":
				if(!ConfigSettings.ENABLE_SCOREBOARD) {
					QuestChatManager.info(p, I18n.locMsg(p, "CommandInfo.ScoreboardIsNotEnabled"));
				}
				if(Main.disableScoreboard.contains(p.getUniqueId().toString())) {
					Main.disableScoreboard.remove(p.getUniqueId().toString());
					File file = new File(Main.getInstance().getDataFolder()+"/playerdata.yml");
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					fc.set(p.getUniqueId().toString()+".noscoreboard", false);
					try {
						fc.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					QuestChatManager.info(p, I18n.locMsg(p, "CommandInfo.EnabledScoreboard"));
				}else {
					Main.disableScoreboard.add(p.getUniqueId().toString());
					File file = new File(Main.getInstance().getDataFolder()+"/playerdata.yml");
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					fc.set(p.getUniqueId().toString()+".noscoreboard", true);
					try {
						fc.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
					QuestChatManager.info(p, I18n.locMsg(p, "CommandInfo.DisabledScoreboard"));
				}
				return false;
			default:
				sendHelp(p);
				break;
		}
		return false;
	}

	public void sendHelp(Player p) {
		QuestChatManager.info(p, I18n.locMsg(p,"CommandHelp.Title"));
		QuestChatManager.info(p, I18n.locMsg(p,"CommandHelp.Language"));
		QuestChatManager.info(p, I18n.locMsg(p,"CommandHelp.ToggleScoreboard"));
		QuestChatManager.info(p, I18n.locMsg(p,"CommandHelp.Quest"));
		QuestChatManager.info(p, I18n.locMsg(p,"CommandHelp.Editor"));
	}

}
