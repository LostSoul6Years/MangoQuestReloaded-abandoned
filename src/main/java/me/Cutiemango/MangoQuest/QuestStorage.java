package me.Cutiemango.MangoQuest;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import me.Cutiemango.MangoQuest.conversation.ConversationProgress;
import me.Cutiemango.MangoQuest.conversation.FriendConversation;
import me.Cutiemango.MangoQuest.conversation.QuestChoice;
import me.Cutiemango.MangoQuest.conversation.QuestConversation;
import me.Cutiemango.MangoQuest.conversation.StartTriggerConversation;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.model.Quest;
import net.md_5.bungee.api.ChatColor;

public class QuestStorage
{
	//timed quests and timed quest progresses
	//public static final HashMap<String, Quest> timedQuests = new HashMap<>();
	
	//stored with Player UUID in String format (idk why am i doing this)
	public static final HashMap<String, List<QuestProgress>> timedProgress = new HashMap<>();
	
	
	
	// Saved With InternalID
	public static final LinkedHashMap<String, Quest> localQuests = new LinkedHashMap<>();

	// Saved With PlayerName
	public static final HashMap<String, QuestPlayerData> playerData = new HashMap<>();
	
	public static final HashMap<String, ConversationProgress> conversationProgress = new HashMap<>();
	public static final HashMap<String, QuestChoice> choiceProgress = new HashMap<>();

	public static final HashMap<String, QuestConversation> localConversations = new HashMap<>();
	public static final HashSet<FriendConversation> friendConversations = new HashSet<>();
	public static final HashMap<Quest, StartTriggerConversation> startTriggerConversations = new HashMap<>();

	public static final HashMap<String, QuestChoice> localChoices = new HashMap<>();

	public static final EnumMap<Material, String> translationMap = new EnumMap<>(Material.class);
	public static final EnumMap<EntityType, String> entityTypeMap = new EnumMap<>(EntityType.class);
	

	public static String prefix = ChatColor.GOLD + "MangoQuest>";

	public static void clear()
	{
		timedProgress.clear();
		localQuests.clear();
		playerData.clear();
		conversationProgress.clear();
		friendConversations.clear();
		localChoices.clear();
		choiceProgress.clear();
		localConversations.clear();
	}
	public static QuestStorageTemporary copyToTemp() {
		QuestStorageTemporary qst = new QuestStorageTemporary();
		qst.timedProgress = new HashMap<>(timedProgress);
		qst.localQuests = new LinkedHashMap<>(localQuests);
		qst.playerData = new HashMap<>(playerData);
		qst.conversationProgress = new HashMap<>(conversationProgress);
		qst.friendConversations = new HashSet<>(friendConversations);
		qst.localChoices = new HashMap<>(localChoices);
		qst.choiceProgress = new HashMap<>(choiceProgress);
		qst.localConversations = new HashMap<>(localConversations);
		return qst;
	}

}
