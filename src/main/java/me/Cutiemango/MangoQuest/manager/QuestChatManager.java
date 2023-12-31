package me.Cutiemango.MangoQuest.manager;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability;
import net.md_5.bungee.api.ChatColor;

public class QuestChatManager
{
	
	  
	public static final Pattern hex = Pattern.compile("\\<#[a-fA-F0-9]{6}\\>");
	public static String translateColor(String s) {
		String rough = ChatColor.translateAlternateColorCodes('&', s);
		rough = Main.getInstance().mcCompat.hexColor(rough);
		return rough;
	}

	public static String toNormalDisplay(String s) {
		return QuestChatManager.translateColor(s.replaceAll("&0", "&f").replaceAll("§0", "§f"));
	}

	public static String toBookDisplay(String s) {
		return QuestChatManager.translateColor(s.replaceAll("&f", "&0").replaceAll("§f", "§0"));
	}

	public static String finishedObjectFormat(String s) {
		return I18n.locMsg(null,"QuestGUI.ColorFormat.FinishedObject") + ChatColor.stripColor(s);
	}

	public static String unavailableQuestFormat(String s) {
		return I18n.locMsg(null,"QuestGUI.ColorFormat.UnavailableQuest") + ChatColor.stripColor(s);
	}

	public static String trimColor(String s) {
		
		String targetText = translateColor(s);
		boolean escape = false;
		boolean nextTextSplit = false;
		int index = 0;
		String savedText = "";
		for (int i = 0; i < targetText.toCharArray().length; i++) {
			if (escape) {
				escape = false;
				continue;
			}
			if (targetText.charAt(i) == '§') {
				if (nextTextSplit) {
					String split = targetText.substring(index, i);
					savedText += getLastColors(split) + ChatColor.stripColor(split);
					index = i;
					nextTextSplit = false;
				}
				escape = true;
				continue;
			}
			nextTextSplit = true;
		}
		String split = targetText.substring(index, targetText.toCharArray().length);
		savedText += getLastColors(split) + ChatColor.stripColor(split);
		return savedText;
	}

	public static void info(CommandSender p, String s) {
		p.sendMessage(QuestStorage.prefix + " " + translateColor(s));
	}

	public static void error(CommandSender p, String s) {
		p.sendMessage(translateColor("&c&lError> " + s));
	}

	public static void logCmd(Level lv, String msg) {
		Bukkit.getLogger().log(lv, "[MangoQuest] " + msg);
	}

	public static void syntaxError(Player p, String req, String entry) {
		info(p, I18n.locMsg(null,"SyntaxError.IllegalArgument"));
		info(p, I18n.locMsg(null,"SyntaxError.ReqEntry") + req);
		info(p, I18n.locMsg(null,"SyntaxError.YourEntry") + entry);
	}
    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == '§' && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    result = color.toString() + result;

                    // Once we find a color or reset we can stop searching
                    if (Main.getInstance().mcCompat.isColor(c)|| color.equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}
