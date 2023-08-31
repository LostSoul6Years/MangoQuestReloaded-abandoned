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

public class QuestStorageTemporary {

	// timed quests and timed quest progresses
	// public HashMap<String, Quest> timedQuests = new HashMap<>();
	public boolean backupInProgress = false;
	// stored with Player UUID in String format (idk why am i doing this)
	public HashMap<String, List<QuestProgress>> timedProgress = new HashMap<>();

	// Saved With InternalID
	public LinkedHashMap<String, Quest> localQuests = new LinkedHashMap<>();

	// Saved With PlayerName
	public HashMap<String, QuestPlayerData> playerData = new HashMap<>();

	public HashMap<String, ConversationProgress> conversationProgress = new HashMap<>();
	public HashMap<String, QuestChoice> choiceProgress = new HashMap<>();

	public HashMap<String, QuestConversation> localConversations = new HashMap<>();
	public HashSet<FriendConversation> friendConversations = new HashSet<>();
	public HashMap<Quest, StartTriggerConversation> startTriggerConversations = new HashMap<>();

	public HashMap<String, QuestChoice> localChoices = new HashMap<>();

	public EnumMap<Material, String> translationMap = new EnumMap<>(Material.class);
	public EnumMap<EntityType, String> entityTypeMap = new EnumMap<>(EntityType.class);

	public String prefix = ChatColor.GOLD + "MangoQuest>";

	public void clear() {
		timedProgress.clear();
		localQuests.clear();
		playerData.clear();
		conversationProgress.clear();
		friendConversations.clear();
		localChoices.clear();
		choiceProgress.clear();
		localConversations.clear();
	}

}
