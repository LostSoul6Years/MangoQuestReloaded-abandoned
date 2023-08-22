package me.Cutiemango.MangoQuest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import me.Cutiemango.MangoQuest.conversation.FriendConversation;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class QuestUtil {

	public static enum Comparison {
		BIGGEREQUAL("QuestObject.BiggerEqual"), SMALLEREQUAL("QuestObject.SmallerEqual"), EQUAL("QuestObject.Equal"),
		BIGGER("QuestObject.Bigger"), SMALLER("QuestObject.Smaller");

		private String DisplayPath;

		public String getDisplayPath() {
			return DisplayPath;
		}

		private Comparison(String DisplayPath) {
			this.DisplayPath = DisplayPath;
		}
	}

	public static String getFileNameWithoutExtension(File file) {
		String fileName = "";

		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fileName = "";
		}

		return fileName;

	}

	public static void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title,
			String subtitle) {
		Main.getInstance().handler.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
	}
	

	public static int randomInteger(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}

	public static <T> List<T> convert(Set<T> set) {
		return new ArrayList<T>(set);
	}

	public static QuestPlayerData getData(Player p) {
		return QuestStorage.playerData.get(p.getName());
	}
	
	public static QuestPlayerData recompileEffectTasks(QuestPlayerData qpd) {
		qpd.getNpcEffects().clear();
		qpd.getRedoableQuests().clear();
		for (Quest q : QuestStorage.localQuests.values()) {
			if (q.getQuestNPC() == null) {
				continue;
			}			
			if (qpd.canTake(q, false)) {
				List<Quest> questList = new ArrayList<>();
				if (qpd.getNpcEffects().containsKey(q.getQuestNPC())) {
					questList = qpd.getNpcEffects().get(q.getQuestNPC());
				}
				questList.add(q);
				qpd.putNPCEffectsQuestList(q.getQuestNPC(), questList);

			} else if (!qpd.isCurrentlyDoing(q) && q.isRedoable() && q.getQuestNPC() != null) {
				qpd.getRedoableQuests().clear();
				qpd.getRedoableQuests().add(q);
			}
		}
		return qpd;
	}

	public static QuestPlayerData compileEffectTasks(QuestPlayerData qpd) {
		for (Quest q : QuestStorage.localQuests.values()) {
			if (q.getQuestNPC() == null) {
				DebugHandler.log(5, "Rejected quest "+q.getQuestName()+" for invalid npc/");
				continue;
			}			
			if (qpd.canTake(q, false)) {
				List<Quest> questList = new ArrayList<>();
				
				if (qpd.getNpcEffects().containsKey(q.getQuestNPC())) {
					questList = qpd.getNpcEffects().get(q.getQuestNPC());
				}
				questList.add(q);
				qpd.putNPCEffectsQuestList(q.getQuestNPC(), questList);
				
			} else if (!qpd.isCurrentlyDoing(q) && q.isRedoable() && q.getQuestNPC() != null) {
				DebugHandler.log(3, q.getInternalID()+" what is happening i honestly have no idea");
				qpd.getRedoableQuests().add(q);
			}
		}
		return qpd;
	}

	public static QuestPlayerData recompileEffectTask(QuestPlayerData qpd, String q) {
		Quest quest;
		if ((quest = QuestStorage.localQuests.get(q)) != null) {
			//remove previous redoable quests check
			qpd.getRedoableQuests().removeIf((que) -> {
				return que.getInternalID().equals(quest.getInternalID());
			});
			//remove previous npc effect tasks
			Map<NPC, List<Quest>> tempMap = new HashMap<>();
			Map<NPC, List<Quest>> copiedEffects = new HashMap<>(qpd.getNpcEffects());
			for (Entry<NPC, List<Quest>> entry : qpd.getNpcEffects().entrySet()) {
				
				List<Quest> quests = new ArrayList<>(entry.getValue());
				if (quests.stream().anyMatch((toRemove) -> {
					return toRemove.getInternalID().equals(quest.getInternalID());
				})) {
					quests.removeIf((toRemove) -> {
						return toRemove.getInternalID().equals(quest.getInternalID());
					});
				}
				tempMap.put(entry.getKey(), quests);
			}
			for(Entry<NPC, List<Quest>> entry :tempMap.entrySet()) {
				copiedEffects.put(entry.getKey(), entry.getValue());
			}
			qpd.setNpcEffects(copiedEffects);
			
			//Below will start recompiling the task and add them back in		
			if(quest.getQuestNPC()!=null) {
				if (qpd.canTake(quest, false)) {
					List<Quest> questList = new ArrayList<>();
					if (qpd.getNpcEffects().containsKey(quest.getQuestNPC())) {
						questList = qpd.getNpcEffects().get(quest.getQuestNPC());
					}
					questList.add(quest);
					qpd.putNPCEffectsQuestList(quest.getQuestNPC(), questList);

				} else if (!qpd.isCurrentlyDoing(quest) && quest.isRedoable() && quest.getQuestNPC() != null) {
					qpd.getRedoableQuests().add(quest);
				}
			}
		}
		return qpd;
	}
	public static void recompileEffectsWithQuestName(String q) {
		for(Player p:Bukkit.getOnlinePlayers()) {
			QuestPlayerData qpd;
			if((qpd=getData(p))!=null) {
				qpd = recompileEffectTask(qpd,q);
				QuestStorage.playerData.put(p.getName(), qpd);
			}						
		}
	}

	public static double cut(double d) {
		return Math.floor((d * 100)) / 100;
	}

	public static void executeSyncCommand(Player p, String command) {
		if (p == null || command == null)
			return;
		if (!executeBungeeCommand(p, command)) {
			/*
			 * Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> {
			 * 
			 * try { return p.performCommand(command); }catch(Exception e) {
			 * e.printStackTrace(); return false; } });
			 */
			 Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> {
				 return Bukkit.dispatchCommand(p, command);
			 });
		}
	}

	public static void executeSyncOPCommand(Player p, String command) {
		if (p == null || command == null)
			return;
		boolean op = p.isOp();
		Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
			try {
				if (!op)
					p.setOp(true);
				if (!executeBungeeCommand(p, command)) {
					Bukkit.dispatchCommand(p, command);
				}
			} catch (Exception e) {
				QuestChatManager.logCmd(Level.SEVERE,
						"The server encountered an unchecked exception when making player execute OP commands.");
				QuestChatManager.logCmd(Level.SEVERE, "Please report this to the plugin author.");
				e.printStackTrace();
			} finally {
				if (!op)
					p.setOp(false);
			}
		});
	}

	public static <T> boolean isArrayEmpty(T[] arr) {
		for (T idk : arr) {
			if (idk == null) {
				return true;
			}
		}
		return false;
	}

	public static void executeSyncConsoleCommand(String command) {
		Optional.ofNullable(command).ifPresent(cmd -> {
			if (!executeBungeeCommand(Main.getInstance().getServer().getConsoleSender(), command)) {
				Bukkit.getScheduler().callSyncMethod(Main.getInstance(),
						() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
			}
		});
	}

	public static boolean executeBungeeCommand(CommandSender sender, String command) {

		if (!ConfigSettings.ENABLE_BUNGEECORD_SUPPORT) {
			return false;
		}
		if (!sender.hasPermission(ConfigSettings.BUNGEECORD_PERMISSION) && !sender.isOp()) {
			return false;
		}
		String[] args = command.split(" +");
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		switch (args[0]) {
		case "send": {
			if (args.length == 3) {
				Player p = Bukkit.getPlayerExact(args[1]);
				if (p == null) {
					return false;
				}
				String server = args[2];
				out.writeUTF("ConnectOther");
				out.writeUTF(p.getName());
				out.writeUTF(server);
				Main.getInstance().getServer().sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return true;
			}
			break;
		}
		case "message": {
			if (args.length == 3) {
				Player p = Bukkit.getPlayerExact(args[1]);
				if (p == null) {
					return false;
				}
				String message = args[2];
				out.writeUTF("Message");
				out.writeUTF(p.getName());
				out.writeUTF(QuestChatManager.translateColor(message));
				Main.getInstance().getServer().sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return true;
			}
break;
		}
		case "messageraw": {
			if (args.length == 3) {
				Player p = Bukkit.getPlayerExact(args[1]);
				if (p == null) {
					return false;
				}
				String message = args[2];
				out.writeUTF("MessageRaw");
				out.writeUTF(p.getName());
				out.writeUTF(QuestChatManager.translateColor(message));
				Main.getInstance().getServer().sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return true;
			}
			break;
		}
		case "kick": {
			if (args.length == 3) {
				Player p = Bukkit.getPlayerExact(args[1]);
				if (p == null) {
					return false;
				}
				String message = args[2];
				out.writeUTF("KickPlayer");
				out.writeUTF(p.getName());
				out.writeUTF(QuestChatManager.translateColor(message));
				Main.getInstance().getServer().sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return true;
			}
			break;
		}
		default:{
			return false;
		}
		}
		return false;
	}

	public static String convertArgsString(String[] array, int startIndex) {
		return String.join(" ", Arrays.copyOfRange(array, startIndex, array.length));
	}

	public static Set<FriendConversation> getConversations(NPC npc, int fp) {
		Set<FriendConversation> set = new HashSet<>();
		for (FriendConversation conv : QuestStorage.friendConversations) {
			if (conv.getNPC() == null) {
				continue;
			}
			if (conv.getNPC().equals(npc) && (fp >= conv.getReqPoint()))
				set.add(conv);
		}
		return set;
	}

	public static Quest getQuest(String s) {
		return QuestStorage.localQuests.get(s);
	}

	

	public static String getItemName(ItemStack is) {
		if (is.hasItemMeta() && is.getItemMeta().hasDisplayName())
			return is.getItemMeta().getDisplayName();
		else
			return translate(is.getType());
	}

	public static String getItemName(QuestValidater.WrappedWeakItem wrapper) {
		if (wrapper.hasItemMeta() && wrapper.hasDisplayName())
			return wrapper.getDisplayName();
		else
			return translate(wrapper.getType());
	}

	@SafeVarargs
	public static <T> List<T> createList(T... args) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, args);
		return list;
	}

	public static String translate(Material mat) {
		//try {
		//	throw new Exception();
		//}catch(Exception e) {
		//	e.printStackTrace();
		//}
		//return "transmaterial:"+mat.name();
		return Optional.ofNullable(QuestStorage.translationMap.get(mat)).orElseGet(() -> {
			try (InputStream is = QuestUtil.class.getResourceAsStream("/translations.yml");
					InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
					BufferedReader br = new BufferedReader(isr);) {
				
				YamlConfiguration defaultTranslateYml = YamlConfiguration.loadConfiguration(br);
				if (defaultTranslateYml.getString(mat.name()) != null) {
					String translated = defaultTranslateYml.getString(mat.name());
					QuestStorage.translationMap.put(mat, translated);
					File eTrans = new File(Main.getInstance().getDataFolder(), "translations.yml");
					if (eTrans.exists()) {
						YamlConfiguration badConfig = YamlConfiguration.loadConfiguration(eTrans);
						badConfig.set(mat.name(), translated);
						badConfig.save(eTrans);
					}
					return translated;
				}
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			// return I18n.locMsg(null, "Translation.UnknownItem");
				return mat.name(); 
			
		});
	}

	// unfinished
	public static String translate(ItemStack item) {
		if (item == null)
			return I18n.locMsg(null, "Translation.UnknownItem");
		return getItemName(item);
	}

	public static List<String> translateMultiple(ItemStack... item) {
		List<String> translated = new ArrayList<>();
		for (ItemStack i : item) {
			if (i == null) {
				translated.add("");
				continue;
			}
			translated.add(" " + translate(i));
		}
		String last = translated.get(translated.size() - 1);
		last += " ";
		translated.set(translated.size() - 1, last);
		// return (String[]) translated.toArray();
		return translated;
	}

	public static String[] translateCraftItem(List<String> translatedTable, ItemStack result, int amount) {
		List<String> translatedTable1 = new ArrayList<>(translatedTable);
		translatedTable1.add(translate(result));
		translatedTable1.add(amount + "");
		return translatedTable1.toArray(new String[translatedTable1.size()]);
	}

	public static Object[] translateCraftItemComponent(ItemStack[] craftReq, ItemStack result, int amount) {
		List<Object> objs = new ArrayList<>();
		for (ItemStack i : craftReq) {
			objs.add(i);
		}
		objs.add(result);
		objs.add(amount);
		return objs.toArray();
	}

	public static String translate(EntityType e) {
		return Optional.ofNullable(QuestStorage.entityTypeMap.get(e)).orElseGet(() -> {
			try (InputStream is = QuestUtil.class.getResourceAsStream("/translations.yml");
					InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
					BufferedReader br = new BufferedReader(isr);) {
				YamlConfiguration defaultTranslateYml = YamlConfiguration.loadConfiguration(br);
				if (defaultTranslateYml.getString(e.name()) != null) {
					String translated = defaultTranslateYml.getString(e.name());
					QuestStorage.entityTypeMap.put(e, translated);
					File eTrans = new File(Main.getInstance().getDataFolder(), "translations.yml");
					if (eTrans.exists()) {
						YamlConfiguration badConfig = YamlConfiguration.loadConfiguration(eTrans);
						badConfig.set(e.name(), translated);
						badConfig.save(eTrans);
					}
					return translated;
				}
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			//return e.name();
			// return I18n.locMsg(null, "Translation.UnknownEntity");
			//return "transentity:"+e.name();
			return e.name();
		});
	}

	public static TextChannel findChannel(String channel) {
		channel = channel.trim().replace(" ", "");
		if (DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel) != null) {
			return DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel);
		} else if (DiscordSRV.getPlugin().getMainGuild().getTextChannelsByName(channel, false) != null
				&& !DiscordSRV.getPlugin().getMainGuild().getTextChannelsByName(channel, false).isEmpty()) {
			return DiscordSRV.getPlugin().getMainGuild().getTextChannelsByName(channel, false).get(0);
		} else {
			try {
				Long.parseUnsignedLong(channel);
				return DiscordSRV.getPlugin().getMainGuild().getTextChannelById(channel);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

	public static boolean doesChannelExist(String channel) {
		channel = channel.trim().replace(" ", "");

		if (DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel) != null) {
			return true;
		} else if (DiscordSRV.getPlugin().getMainGuild() == null) {
			return false;
		} else if (DiscordSRV.getPlugin().getMainGuild().getTextChannelsByName(channel, false) != null
				&& !DiscordSRV.getPlugin().getMainGuild().getTextChannelsByName(channel, false).isEmpty()) {
			return true;
		} else {
			try {
				Long.parseUnsignedLong(channel);
				return DiscordSRV.getPlugin().getMainGuild().getTextChannelById(channel) != null;
			} catch (NumberFormatException e) {
				return false;
			}
		}

	}
}
