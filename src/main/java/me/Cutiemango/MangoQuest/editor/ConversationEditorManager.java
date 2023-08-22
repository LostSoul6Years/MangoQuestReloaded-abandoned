package me.Cutiemango.MangoQuest.editor;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.book.QuestBookPage;
import me.Cutiemango.MangoQuest.conversation.FriendConversation;
import me.Cutiemango.MangoQuest.conversation.QuestBaseAction;
import me.Cutiemango.MangoQuest.conversation.QuestBaseAction.EnumAction;
import me.Cutiemango.MangoQuest.conversation.QuestConversation;
import me.Cutiemango.MangoQuest.conversation.StartTriggerConversation;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ConversationEditorManager
{
	public static HashMap<String, QuestConversation> isEditing = new HashMap<>();

	public static QuestConversation getEditingConversation(Player p) {
		return isEditing.get(p.getName());
	}

	public static void edit(Player p, QuestConversation conv) {
		isEditing.put(p.getName(), conv);
	}

	public static void exit(Player p) {
		isEditing.remove(p.getName());
		QuestChatManager.info(p, I18n.locMsg(p,"EditorMessage.Exited"));
	}

	public static boolean checkEditorMode(Player p, boolean msg) {
		if (!isEditing.containsKey(p.getName()) && msg)
			QuestChatManager.error(p, I18n.locMsg(p,"EditorMessage.NotInEditor"));
		return isEditing.containsKey(p.getName());
	}

	public static void mainGUI(Player p) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.Title")).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.NewConversation")).clickCommand("/mq ce newconv")
				.showText(I18n.locMsg(p,"ConversationEditor.NewConversation.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.EditConversation")).clickCommand("/mq ce edit")
				.showText(I18n.locMsg(p,"ConversationEditor.EditConversation.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.RemoveConversation")).clickCommand("/mq ce remove")
				.showText(I18n.locMsg(p,"ConversationEditor.RemoveConversation.ShowText"))).changeLine();
		p1.changeLine();

		if (checkEditorMode(p, false) && ConversationEditorManager.getEditingConversation(p).getInternalID() != null) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ReturnToEditor")).clickCommand("/mq ce gui")
					.showText(I18n.locMsg(p,"QuestEditor.ReturnToEditor.ShowText"))).changeLine();
			p1.changeLine();
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ExitEditor")).clickCommand("/mq ce exit")
					.showText(I18n.locMsg(p,"QuestEditor.ExitEditor.ShowText"))).changeLine();
		}
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void editGUI(Player p) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"ConversationEditor.Title")).changeLine();
		book.add(I18n.locMsg(p,"ConversationEditor.ChooseEditConv")).changeLine();
		for (QuestConversation conv : QuestStorage.localConversations.values()) {
			book.add(new InteractiveText(p,"&0- &l" + conv.getName() + "&0(" + conv.getInternalID() + ")")
					.clickCommand("/mq ce select " + conv.getInternalID()));
			book.changeLine();
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq ce"));
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void removeGUI(Player p) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"ConversationEditor.Title")).changeLine();
		book.add(I18n.locMsg(p,"ConversationEditor.ChooseRemoveConv")).changeLine();
		for (QuestConversation conv : QuestStorage.localConversations.values()) {
			book.add(new InteractiveText(p,"&0- &l" + conv.getName() + "&0(" + conv.getInternalID() + ")")
					.clickCommand("/mq ce remove confirm " + conv.getInternalID()));
			book.changeLine();
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq ce"));
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void editConversation(Player p) {
		if (!checkEditorMode(p, true))
			return;
		QuestConversation conv = getEditingConversation(p);
		FlexibleBook book = new FlexibleBook();
		// Basic Info
		book.add(I18n.locMsg(p,"QuestEditor.BasicInfo")).changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.ConvInternalID", conv.getInternalID()))
				.showText(I18n.locMsg(p,"ConversationEditor.ConvInternalID.ShowText"))).changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.ConvName", conv.getName()))
				.showText(I18n.locMsg(p,"ConversationEditor.ConvName.ShowText")).clickCommand("/mq ce edit name")).changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.ConvNPC")).showText(I18n.locMsg(p,"ConversationEditor.ConvNPC.ShowText"))
				.clickCommand("/mq ce edit npc"));

		if (conv.getNPC() != null)
			book.add(new InteractiveText(p,"").showNPCInfo(conv.getNPC())).changeLine();
		else
			book.add(I18n.locMsg(p,"QuestEditor.NotSet")).changeLine();

		book.changeLine();
		book.changeLine();

		book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.StartSimulation")).clickCommand("/mq ce modconv")).changeLine();

		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.changeLine();

		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq ce edit")).changeLine();

		// Speical conv
		book.createNewPage();
		book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.ConvType")).showText(I18n.locMsg(p,"ConversationEditor.ConvType.ShowText"))
				.clickCommand("/mq ce edit convtype"));

		int counter = 0;
		if (conv instanceof FriendConversation) {
			book.add(I18n.locMsg(p,"ConversationEditor.FriendConvText")).changeLine();

			book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.FriendConvReqPoint"))
					.showText(I18n.locMsg(p,"ConversationEditor.FriendConvReqPoint.ShowText")).clickCommand("/mq ce edit fconvp"));
			book.add(Integer.toString(((FriendConversation) conv).getReqPoint())).changeLine();
		} else if (conv instanceof StartTriggerConversation) {
			StartTriggerConversation sconv = (StartTriggerConversation) conv;
			book.add(I18n.locMsg(p,"ConversationEditor.StartTriggerConvText")).changeLine();
			book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.QuestTrigger")).clickCommand("/mq ce edit quest"));
			book.add(new InteractiveText(p,"").showQuest(sconv.getQuest())).changeLine();

			book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.QuestFullMessage")).clickCommand("/mq ce edit fullmsg")
					.showText(I18n.locMsg(p,"ConversationEditor.QuestFullMessage.ShowText"))).changeLine();
			book.add(sconv.getQuestFullMessage(p)).changeLine();

			book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.QuestAcceptMessage")).clickCommand("/mq ce edit acceptmsg")
					.showText(I18n.locMsg(p,"ConversationEditor.QuestAcceptMessage.ShowText")));
			book.add(sconv.getAcceptMessage(p)).changeLine();

			book.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.QuestDenyMessage")).clickCommand("/mq ce edit denymsg")
					.showText(I18n.locMsg(p,"ConversationEditor.QuestDenyMessage.ShowText")));
			book.add(sconv.getDenyMessage(p)).changeLine();

			book.createNewPage();

			book.add(I18n.locMsg(p,"ConversationEditor.QuestAcceptActions")).changeLine();

			for (QuestBaseAction act : ((StartTriggerConversation) conv).getAcceptActions()) {
				book.add("- " + counter + ".");
				if (EnumAction.NO_OBJ_ACTIONS.contains(act.getActionType()))
					book.add(new InteractiveText(p,act.getActionType().toCustomString(p))
							.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + I18n.locMsg(p,"ConversationEditor.NoObjectAction"))
							.clickCommand("/mq ce edit acceptact" + counter));
				else if (act.getActionType().equals(EnumAction.NPC_TALK)) {
					String[] split = act.getObject().split("@");
					NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(split[1]));
					book.add(new InteractiveText(p,act.getActionType().toCustomString(p)).showText(
							I18n.locMsg(p,"ConversationEditor.ActionObject") + QuestChatManager
									.toNormalDisplay(I18n.locMsg(p,"QuestJourney.NPCFriendMessage", me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), split[0]))));
				} else
					book.add(new InteractiveText(p, act.getActionType().toCustomString(p))
							.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + act.getObject()));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new acceptact " + (counter + 1)));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce edit acceptact " + counter));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq ce remove acceptact " + counter));
				book.changeLine();
				counter++;
			}
			if (((StartTriggerConversation) conv).getAcceptActions().size() == 0)
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new acceptact " + 0)).changeLine();

			book.changeLine();

			book.createNewPage();
			book.add(I18n.locMsg(p,"ConversationEditor.QuestDenyActions")).changeLine();

			counter = 0;
			for (QuestBaseAction act : ((StartTriggerConversation) conv).getDenyActions()) {
				book.add("- " + counter + ".");
				if (EnumAction.NO_OBJ_ACTIONS.contains(act.getActionType()))
					book.add(new InteractiveText(p,act.getActionType().toCustomString(p))
							.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + I18n.locMsg(p,"ConversationEditor.NoObjectAction")));
				else if (act.getActionType().equals(EnumAction.NPC_TALK)) {
					String[] split = act.getObject().split("@");
					NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(split[1]));
					book.add(new InteractiveText(p,act.getActionType().toCustomString(p)).showText(
							I18n.locMsg(p,"ConversationEditor.ActionObject") + QuestChatManager
									.toNormalDisplay(I18n.locMsg(p,"QuestJourney.NPCFriendMessage", me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), split[0]))));
				} else
					book.add(new InteractiveText(p,act.getActionType().toCustomString(p))
							.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + act.getObject()));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new denyact " + (counter + 1)));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce edit denyact " + counter));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq ce remove denyact " + counter));
				book.changeLine();
				counter++;
			}

			if (((StartTriggerConversation) conv).getDenyActions().size() == 0)
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new denyact " + counter));

			book.changeLine();
		} else {
			book.add(I18n.locMsg(p,"ConversationEditor.QuestConvText")).changeLine();
			book.changeLine();
			book.add(I18n.locMsg(p,"ConversationEditor.Empty")).changeLine();
			book.changeLine();
		}

		book.createNewPage();
		book.add(I18n.locMsg(p,"ConversationEditor.ConvActionInfo")).changeLine();
		counter = 0;
		for (QuestBaseAction act : conv.getActions()) {
			book.add("- " + counter + ".");
			if (EnumAction.NO_OBJ_ACTIONS.contains(act.getActionType()))
				book.add(new InteractiveText(p,act.getActionType().toCustomString(p))
						.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + I18n.locMsg(p,"ConversationEditor.NoObjectAction")));
			else if (act.getActionType().equals(EnumAction.NPC_TALK)) {
				String[] split = act.getObject().split("@");
				NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(split[1]));
				book.add(new InteractiveText(p,act.getActionType().toCustomString(p)).showText(
						I18n.locMsg(p,"ConversationEditor.ActionObject") + QuestChatManager
								.toNormalDisplay(I18n.locMsg(p,"QuestJourney.NPCFriendMessage", me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), split[0]))));
			} else
				book.add(new InteractiveText(p,act.getActionType().toCustomString(p))
						.showText(I18n.locMsg(p,"ConversationEditor.ActionObject") + act.getObject()));
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new act " + (counter + 1)));
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce edit act " + counter));
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq ce remove act " + counter));
			book.changeLine();
			counter++;
		}

		if (conv.getActions().size() == 0)
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq ce new act " + 0)).changeLine();

		book.changeLine();

		// Saving Page
		book.createNewPage();
		book.add(I18n.locMsg(p,"QuestEditor.SaveAndExit")).changeLine();
		book.add(
				new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncSevAndLoc")).clickCommand("/mq ce sa").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncSev")).clickCommand("/mq ce sl").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncLoc")).clickCommand("/mq ce sc").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.QuitEditor")).clickCommand("/mq ce exit")
				.showText(I18n.locMsg(p,"QuestEditor.ExitEditor.ShowText"))).changeLine();

		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void createConversation(Player p) {
		FlexibleBook page = new FlexibleBook();
		page.add(I18n.locMsg(p,"ConversationEditor.CreateConv")).changeLine();
		page.add(I18n.locMsg(p,"ConversationEditor.CreateConvDesc")).changeLine();
		QuestConversation qc = ConversationEditorManager.getEditingConversation(p);
		page.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.CreateConvButton")).clickCommand("/mq ce newconv create")).changeLine();

		page.changeLine();

		String id = (qc.getInternalID() != null) ? qc.getInternalID() : I18n.locMsg(p,"QuestEditor.NotSet");
		page.add(I18n.locMsg(p,"ConversationEditor.NewConvID"));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce newconv id")).changeLine();
		page.add(id).changeLine();

		String name = (qc.getName() != null) ? qc.getName() : I18n.locMsg(p,"QuestEditor.NotSet");
		page.add(I18n.locMsg(p,"ConversationEditor.NewConvName"));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce newconv name")).changeLine();
		page.add(name).changeLine();

		InteractiveText npc = (qc.getNPC() != null) ?
				new InteractiveText(p,"").showNPCInfo(qc.getNPC()) :
				new InteractiveText(p,I18n.locMsg(p,"QuestEditor.NotSet"));
		page.add(I18n.locMsg(p,"ConversationEditor.NewConvNPC"));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq ce newconv npc")).changeLine();
		page.add(npc).changeLine();

		page.changeLine();
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq ce")).changeLine();
		QuestBookGUIManager.openBook(p, page.toSendableBook());
	}

	public static void removeConfirmGUI(Player p, QuestConversation conv) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveConv")).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveQuest2", conv.getName())).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveQuest3")).changeLine();
		p1.changeLine();
		p1.changeLine();
		p1.add("  ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.WarnAccept")).clickCommand("/mq ce remove conv " + conv.getInternalID()));
		p1.add(" &8&l/ ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.WarnDeny")).clickCommand("/mq ce remove"));
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectActionType(Player p, String type, String mode, int index) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"ConversationEditor.SelectActionType")).changeLine();
		for (EnumAction act : EnumAction.values()) {
			p1.add(new InteractiveText(p,"- [" +act.toCustomString(p) + "]")
					.clickCommand("/mq ce " + mode + " " + type + " " + index + " " + act.name()));
			p1.changeLine();
		}
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectConvType(Player p) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"ConversationEditor.SelectConvType")).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.QuestConv")).clickCommand("/mq ce edit convtype normal"));
		p1.changeLine();
		p1.changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.FriendConv")).clickCommand("/mq ce edit convtype friend"));
		p1.changeLine();
		p1.changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"ConversationEditor.StartTriggerConv")).clickCommand("/mq ce edit convtype start"));
		p1.changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectConversation(Player p, String cmd) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"ConversationEditor.ChooseTargetConv")).changeLine();
		for (QuestConversation conv : QuestStorage.localConversations.values()) {
			book.add(new InteractiveText(p,"&0- &l" + conv.getName() + "&0(" + conv.getInternalID() + ")")
					.clickCommand("/" + cmd + " " + conv.getInternalID()));
			book.changeLine();
		}
		if(QuestStorage.localConversations.isEmpty()) {
			book.add(I18n.locMsg(p, "QuestEditor.NotSet"));
		}
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

}
