package me.Cutiemango.MangoQuest.conversation;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.book.QuestBookPage;
import me.Cutiemango.MangoQuest.conversation.QuestBaseAction.EnumAction;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;
import org.bukkit.entity.Player;

public class ConversationManager
{
	
	public static void startConversation(Player p, QuestConversation conv) {
		ConversationProgress cp = new ConversationProgress(p, conv);
		QuestStorage.conversationProgress.put(p.getName(), cp);
		DebugHandler.log(5, "[Conversations] Player %s started conversation %s(%s).", p.getName(), conv.getName(), conv.getInternalID());
		cp.nextAction();
		openConversation(p, cp);
	}

	
	public static void simulateConversation(Player p, QuestConversation conv) {
		ConversationProgress cp = new ModelConvProgress(p, conv);
		QuestStorage.conversationProgress.put(p.getName(), cp);
		cp.nextAction();
		openConversation(p, cp);
	}

	
	public static void openConversation(Player p, ConversationProgress cp) {
		QuestBookGUIManager.openBook(p, cp.getCurrentBook().toSendableBook());
	}

	
	public static QuestConversation getConversation(String s) {
		return QuestStorage.localConversations.get(s);
	}

	
	public static ConversationProgress getConvProgress(Player p) {
		return QuestStorage.conversationProgress.get(p.getName());
	}

	
	public static QuestChoice getChoiceByName(String s) {
		return QuestStorage.localChoices.get(s);
	}

	
	public static QuestChoice getChoiceProgress(Player p) {
		return QuestStorage.choiceProgress.get(p.getName());
	}

	public static StartTriggerConversation getStartConversation(Quest q) {
		return QuestStorage.startTriggerConversations.get(q);
	}

	public static boolean hasConvProgress(Player p) {
		return QuestStorage.conversationProgress.containsKey(p.getName());
	}

	
	public static boolean isInConvProgress(Player p, QuestConversation conv) {
		if (QuestStorage.conversationProgress.get(p.getName()) == null)
			return false;
		return QuestValidater.detailedValidate(conv, QuestStorage.conversationProgress.get(p.getName()).getConversation());
	}

	public static void forceQuit(Player p, QuestConversation conv) {
		if (!isInConvProgress(p, conv))
			return;
		ConversationProgress cp = QuestStorage.conversationProgress.get(p.getName());
		cp.getCurrentPage().add(I18n.locMsg(p,"CommandInfo.ForceQuitConv")).changeLine();
		cp.getActionQueue().push(new QuestBaseAction(p,EnumAction.FINISH, "false"));
	}

	public static void finishConversation(Player p) {
		QuestStorage.conversationProgress.remove(p.getName());
	}

	public static QuestBookPage generateNewPage(Player p,QuestConversation conv) {
		QuestBookPage page = new QuestBookPage().add(I18n.locMsg(p,"Conversation.Title", conv.getName()));
		if (ConfigSettings.ENABLE_SKIP)
			page.add(new InteractiveText(p,"➔").clickCommand("/mq conv skip").showText(I18n.locMsg(null,"Conversation.SkipConv")));
		page.changeLine();
		return page;
	}

}
