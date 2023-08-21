package me.Cutiemango.MangoQuest.conversation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.model.Quest;
import net.citizensnpcs.api.npc.NPC;

public class StartTriggerConversation extends QuestConversation
{

	public StartTriggerConversation(String s, String internal, NPC n, List<QuestBaseAction> list, Quest q) {
		super(s, internal, n, list);
		startQuest = q;
	}

	public StartTriggerConversation(QuestConversation conv, Quest q) {
		super(conv.getName(), conv.getInternalID(), conv.getNPC(), conv.getActions());
		startQuest = q;
	}

	private Quest startQuest;
	private List<QuestBaseAction> acceptActions = new ArrayList<>();
	private List<QuestBaseAction> denyActions = new ArrayList<>();
	private String questFullMessage = I18n.locMsg(null,"Conversation.DefaultQuestFullMessage");

	private String acceptMessage = I18n.locMsg(null,"Conversation.DefaultQuestAcceptMessage");
	private String denyMessage = I18n.locMsg(null,"Conversation.DefaultQuestDenyMessage");

	public Quest getQuest() {
		return startQuest;
	}

	public void setQuest(Quest q) {
		startQuest = q;
	}

	public List<QuestBaseAction> getAcceptActions() {
		return acceptActions;
	}

	public List<QuestBaseAction> getDenyActions() {
		return denyActions;
	}

	public String getAcceptMessage(Player p) {
		if (acceptMessage == null)
			return I18n.locMsg(p,"Conversation.DefaultQuestAcceptMessage");
		return acceptMessage;
	}

	public void setAcceptMessage(String acceptMessage) {
		this.acceptMessage = acceptMessage;
	}

	public String getDenyMessage(Player p) {
		if (denyMessage == null)
			return I18n.locMsg(p,"Conversation.DefaultQuestDenyMessage");
		return denyMessage;
	}

	public void setDenyMessage(String denyMessage) {
		this.denyMessage = denyMessage;
	}

	public String getQuestFullMessage(Player p) {
		if (questFullMessage == null)
			return I18n.locMsg(p,"Conversation.DefaultQuestFullMessage");
		return questFullMessage;
	}

	public void setQuestFullMessage(String s) {
		questFullMessage = s;
	}

	public void setAcceptActions(List<QuestBaseAction> accept) {
		acceptActions = accept;
	}

	public void setDenyActions(List<QuestBaseAction> deny) {
		denyActions = deny;
	}

	@Override
	public StartTriggerConversation clone() {
		StartTriggerConversation conv = new StartTriggerConversation(name, id, npc, action, startQuest);
		conv.setAcceptActions(acceptActions);
		conv.setDenyActions(denyActions);
		conv.setAcceptMessage(acceptMessage);
		conv.setDenyMessage(denyMessage);
		conv.setQuestFullMessage(questFullMessage);
		return conv;
	}

	public QuestConversation simpleClone() {
		return new QuestConversation(name, id, npc, action);
	}
}
