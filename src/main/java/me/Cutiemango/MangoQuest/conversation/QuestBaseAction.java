package me.Cutiemango.MangoQuest.conversation;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;

public class QuestBaseAction
{
	private final EnumAction action;
	private String obj;
	private Player p;

	public QuestBaseAction(Player p,EnumAction act, String st) {
		action = act;
		obj = st;
		this.p = p;
	}

	public enum EnumAction
	{
		CHANGE_PAGE("EnumAction.ChangePage"),
		CHANGE_LINE("EnumAction.ChangeLine"),
		SENTENCE("EnumAction.Sentence"),
		NPC_TALK("EnumAction.NPCTalk"),
		CHOICE("EnumAction.Choice"),
		BUTTON("EnumAction.Button"),
		COMMAND("EnumAction.Command"),
		COMMAND_PLAYER("EnumAction.CommandPlayer"),
		COMMAND_PLAYER_OP("EnumAction.CommandPlayerOP"),
		WAIT("EnumAction.Wait"),
		FINISH("EnumAction.Finish"),
		TAKE_QUEST("EnumAction.TakeQuest"),
		EXIT("EnumAction.Exit");

		EnumAction(String s) {
			name = s;
		}

		public static final List<EnumAction> NO_OBJ_ACTIONS = Arrays
				.asList(EnumAction.CHANGE_LINE, EnumAction.CHANGE_PAGE, EnumAction.BUTTON, EnumAction.TAKE_QUEST, EnumAction.EXIT);
		private final String name;

		public String toCustomString(Player p) {
			return I18n.locMsg(p, name);
		}
	}

	public void execute(final ConversationProgress cp) {
		String target = obj != null ? obj.replace("<player>", cp.getOwner().getName()) : null;
		switch (action) {
			case BUTTON:
				cp.getCurrentPage().add(new InteractiveText(cp.getOwner(),I18n.locMsg(cp.getOwner(),"Conversation.Button")).clickCommand("/mq conv next")).changeLine();
				break;
			case CHANGE_LINE:
				cp.getCurrentPage().changeLine();
				break;
			case CHANGE_PAGE:
				cp.newPage();
				break;
			case CHOICE:
				QuestChoice c = ConversationManager.getChoiceByName(target);
				if (c == cp.getOwner())
					break;
				c.apply(cp);
				break;
			case COMMAND:
				QuestUtil.executeSyncConsoleCommand(target);
				break;
			case COMMAND_PLAYER:
				QuestUtil.executeSyncCommand(cp.getOwner(), target);
				break;
			case COMMAND_PLAYER_OP:
				QuestUtil.executeSyncOPCommand(cp.getOwner(), target);
				break;
			case SENTENCE:
				cp.getCurrentPage().add(QuestChatManager.translateColor(target)).changeLine();
				break;
			case NPC_TALK:
				String[] split = target.split("@");

				// the plugin user does not set a narrator name
				if (split.length == 1 || !QuestValidater.validateNPC(split[1]))
					cp.getCurrentPage().add(I18n.locMsg(cp.getOwner(),"QuestJourney.NPCMessage", split[0])).changeLine();
				else
					cp.getCurrentPage().add(I18n.locMsg(cp.getOwner(),"QuestJourney.NPCFriendMessage", Main.getHooker().getNPC(split[1]).getName(), split[0]))
							.changeLine();
				break;
			case WAIT:
				new BukkitRunnable()
				{
					@Override
					public void run() {
						cp.nextAction();
					}
				}.runTaskLater(Main.getInstance(), Long.parseLong(target));
				break;
			case FINISH:
				cp.finish(Boolean.parseBoolean(target));
				break;
			case TAKE_QUEST:
				if (!(cp.getConversation() instanceof StartTriggerConversation))
					break;
				StartTriggerConversation conv = (StartTriggerConversation) cp.getConversation();
				QuestPlayerData data = QuestUtil.getData(cp.getOwner());
				if (!data.checkQuestSize(false)) {
					cp.getCurrentPage().add(conv.getQuestFullMessage(cp.getOwner())).changeLine();
					cp.getActionQueue().add(new QuestBaseAction(cp.owner,EnumAction.FINISH, "false"));
					break;
				}
				cp.newPage();
				cp.getCurrentPage().add(I18n.locMsg(cp.getOwner(),"Conversation.ChooseAnOption")).changeLine();
				cp.getCurrentPage().changeLine();
				cp.getCurrentPage().changeLine();
				cp.getCurrentPage().add(new InteractiveText(cp.getOwner(),I18n.locMsg(cp.getOwner(),"Conversation.DefaultQuestAcceptMessage"))).changeLine();
				cp.getCurrentPage().add(new InteractiveText(cp.getOwner(),conv.getAcceptMessage(cp.getOwner())).clickCommand("/mq conv takequest")).changeLine();
				cp.getCurrentPage().changeLine();
				cp.getCurrentPage().changeLine();
				cp.getCurrentPage().add(new InteractiveText(cp.getOwner(),I18n.locMsg(cp.getOwner(),"Conversation.DefaultQuestDenyMessage"))).changeLine();
				cp.getCurrentPage().add(new InteractiveText(cp.getOwner(),conv.getDenyMessage(cp.getOwner())).clickCommand("/mq conv denyquest")).changeLine();
				break;
			case EXIT:
				ConversationManager.finishConversation(cp.getOwner());
				break;
			default:
				break;
		}

	}

	public EnumAction getActionType() {
		return action;
	}

	public String getObject() {
		return obj;
	}

	public String toConfigFormat() {
		if (obj == null)
			obj = "";
		return action.toString() + "#" + obj;
	}

}