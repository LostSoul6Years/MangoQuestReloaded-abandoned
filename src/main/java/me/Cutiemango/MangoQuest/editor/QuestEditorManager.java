package me.Cutiemango.MangoQuest.editor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.sucy.skill.SkillAPI;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.book.QuestBookPage;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.manager.TimeHandler;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.model.QuestSetting;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject.TriggerObjectType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestEditorManager
{

	private static HashMap<String, Quest> isEditing = new HashMap<>();

	public static Quest getCurrentEditingQuest(Player p) {
		return isEditing.get(p.getName());
	}

	public static void edit(Player p, Quest q) {
		isEditing.put(p.getName(), q);
		if(q == null) {
			throw new IllegalArgumentException("this is not possible! show the developer it!");
		}
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
		EditorListenerHandler.unregister(p);
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.Title")).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.NewQuest")).clickCommand("/mq e newquest")
				.showText(I18n.locMsg(p,"QuestEditor.NewQuest.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.EditQuest")).clickCommand("/mq e edit")
				.showText(I18n.locMsg(p,"QuestEditor.EditQuest.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.RemoveQuest")).clickCommand("/mq e remove")
				.showText(I18n.locMsg(p,"QuestEditor.RemoveQuest.ShowText"))).changeLine();
		p1.changeLine();

		if (checkEditorMode(p, false) && QuestEditorManager.getCurrentEditingQuest(p).getInternalID() != null) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ReturnToEditor")).clickCommand("/mq e gui")
					.showText(I18n.locMsg(p,"QuestEditor.ReturnToEditor.ShowText"))).changeLine();
			p1.changeLine();
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ExitEditor")).clickCommand("/mq e exit")
					.showText(I18n.locMsg(p,"QuestEditor.ExitEditor.ShowText"))).changeLine();
		}
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void editGUI(Player p) {		
		EditorListenerHandler.unregister(p);
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"QuestEditor.Title")).changeLine();
		book.add(I18n.locMsg(p,"QuestEditor.ChooseEditQuest")).changeLine();
		for (Quest q : QuestStorage.localQuests.values()) {
			book.add(new InteractiveText(p,"&0- &0&l" + q.getQuestName() + "&0(" + q.getInternalID() + ")")
					.clickCommand("/mq e select " + q.getInternalID())).changeLine();
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e"));
		//p.sendMessage(book.toSendableBook().toString());
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void removeGUI(Player p) {
		EditorListenerHandler.unregister(p);
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"QuestEditor.Title")).changeLine();
		book.add(I18n.locMsg(p,"QuestEditor.ChooseRemoveQuest")).changeLine();
		for (Quest q : QuestStorage.localQuests.values()) {
			book.add(new InteractiveText(p,"&0- &0&l" + q.getQuestName() + "&0(" + q.getInternalID() + ")")
					.clickCommand("/mq e remove confirm " + q.getInternalID()));
			book.changeLine();
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e"));
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void removeConfirmGUI(Player p, Quest q) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveQuest1")).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveQuest2", q.getQuestName())).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.WarnRemoveQuest3")).changeLine();
		p1.changeLine();
		p1.changeLine();
		p1.add("  ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.WarnAccept")).clickCommand("/mq e remove quest " + q.getInternalID()));
		p1.add(" &8&l/ ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.WarnDeny")).clickCommand("/mq e remove"));
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void editQuest(Player p) {
		if (!checkEditorMode(p, true))
			return;
		Quest q = getCurrentEditingQuest(p);
		QuestSetting vs = q.getSettings();
		EditorListenerHandler.unregister(p);
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.BasicInfo")).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.QuestInternalID", q.getInternalID())).changeLine();

		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.QuestName", q.getQuestName())).clickCommand("/mq e edit name")
				.showText(I18n.locMsg(p,"QuestEditor.QuestName.ShowText"))).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.QuestNPC")).clickCommand("/mq e edit npc")
				.showText(I18n.locMsg(p,"QuestEditor.QuestNPC.ShowText")));
		if (q.isCommandQuest())
			p1.add(I18n.locMsg(p,"QuestEditor.false"));
		else
			p1.add(new InteractiveText(p,"").showNPCInfo(q.getQuestNPC()));
		p1.changeLine();

		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.RedoSetting", I18n.locMsg(p, q.getRedoSetting().getName()))).clickCommand("/mq e edit redo")
				.showText(I18n.locMsg(p,"QuestEditor.RedoSetting.ShowText"))).changeLine();

		switch (q.getRedoSetting()) {
			case COOLDOWN:
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.RedoDelay", TimeHandler.convertTime(q.getRedoDelay())))
						.clickCommand("/mq e edit redodelay").showText(I18n.locMsg(p,"QuestEditor.RedoDelay.ShowText"))).changeLine();
				break;
			case WEEKLY:
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ResetDay", I18n.locMsg(p,"QuestEditor.ResetDay." + q.getResetDay())))
						.clickCommand("/mq e edit resetday").showText(I18n.locMsg(p,"QuestEditor.ResetDay.ShowText"))).changeLine();
			case DAILY:
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.ResetHour", Integer.toString(q.getResetHour())))
						.clickCommand("/mq e edit resethour").showText(I18n.locMsg(p,"QuestEditor.ResetHour.ShowText"))).changeLine();
				break;
		}
		p1.changeLine();
		p1.createNewPage();
		//QuestBookPage settings = new QuestBookPage();
		p1.add(I18n.locMsg(p,"QuestEditor.QuestSettings")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.IsQuitable")).clickCommand("/mq e edit quit " + !q.isQuitable())
				.showText(I18n.locMsg(p,"QuestEditor.IsQuitable.ShowText." + !q.isQuitable())));
		p1.add(I18n.locMsg(p,"QuestEditor." + q.isQuitable())).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.UsePermission")).clickCommand("/mq e edit perm " + !q.usePermission())
				.showText(I18n.locMsg(p,"QuestEditor.UsePermission.ShowText." + !q.usePermission())));
		p1.add(I18n.locMsg(p,"QuestEditor." + q.usePermission())).changeLine();
		p1.add(new InteractiveText(p,(I18n.locMsg(p,"QuestEditor.WorldLimit"))).clickCommand("/mq e edit world")
				.showText(I18n.locMsg(p,"QuestEditor.WorldLimit.ShowText")));
		if (q.hasWorldLimit())
			p1.add(q.getWorldLimit().getName()).changeLine();
		else
			p1.add(I18n.locMsg(p,"QuestEditor.NotSet")).changeLine();

		p1.changeLine();

		// Display settings
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestVisibility.OnTake")).clickCommand("/mq e edit vis take " + !vs.displayOnTake())
				.showText(I18n.locMsg(p,"QuestVisibility." + !vs.displayOnTake())));
		p1.add(I18n.locMsg(p,"QuestEditor." + vs.displayOnTake())).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestVisibility.OnProgress")).clickCommand("/mq e edit vis prog " + !vs.displayOnProgress())
				.showText(I18n.locMsg(p,"QuestVisibility." + !vs.displayOnProgress())));
		p1.add(I18n.locMsg(p,"QuestEditor." + vs.displayOnProgress())).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestVisibility.OnFinish")).clickCommand("/mq e edit vis finish " + !vs.displayOnFinish())
				.showText(I18n.locMsg(p,"QuestVisibility." + !vs.displayOnFinish())));
		p1.add(I18n.locMsg(p,"QuestEditor." + vs.displayOnFinish())).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestVisibility.OnInteraction"))
				.clickCommand("/mq e edit vis interact " + !vs.displayOnInteraction())
				.showText(I18n.locMsg(p,"QuestVisibility." + !vs.displayOnInteraction())));
		p1.add(I18n.locMsg(p,"QuestEditor." + vs.displayOnInteraction())).changeLine();

		// Time Limit
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.IsTimeLimited")).clickCommand("/mq e edit limit " + !q.isTimeLimited())
				.showText(I18n.locMsg(p,"QuestEditor.IsTimeLimited.ShowText." + !q.isTimeLimited())));
		p1.add(I18n.locMsg(p,"QuestEditor." + q.isTimeLimited())).changeLine();
		if (q.isTimeLimited()) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.TimeLimit")).clickCommand("/mq e edit timelimit")
					.showText(I18n.locMsg(p,"QuestEditor.TimeLimit.ShowText")));
			p1.add(TimeHandler.convertTime(q.getTimeLimit())).changeLine();
		}

		p1.createNewPage();
		p1.add(I18n.locMsg(p,"QuestEditor.ReqEventStageInfo")).changeLine();
		p1.changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.Requirement")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.GoToEditPage")).clickCommand("/mq e edit req")
				.showText(I18n.locMsg(p,"QuestEditor.Requirement.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.QuestEvent")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.GoToEditPage")).clickCommand("/mq e edit evt")
				.showText(I18n.locMsg(p,"QuestEditor.Event.ShowText"))).changeLine();
		p1.changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.QuestStage")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.GoToEditPage")).clickCommand("/mq e edit stage")
				.showText(I18n.locMsg(p,"QuestEditor.QuestStage.ShowText"))).changeLine();

		//QuestBookPage p3 = new QuestBookPage();
		p1.createNewPage();
		p1.add(I18n.locMsg(p,"QuestEditor.Outline")).changeLine();
		for (String out : q.getQuestOutline()) {
			p1.add(out).changeLine();
		}
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit outline")).changeLine();

		//QuestBookPage p4 = new QuestBookPage();
		p1.createNewPage();
		p1.add(I18n.locMsg(p,"QuestEditor.Reward")).changeLine();

		p1.add(I18n.locMsg(p,"QuestEditor.RewardMoney", Double.toString(q.getQuestReward().getMoney())));
		p1.add(new InteractiveText(p," " + I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward money")
				.showText(I18n.locMsg(p,"QuestEditor.RewardMoney.ShowText"))).changeLine();

		p1.add(I18n.locMsg(p,"QuestEditor.RewardExp", Integer.toString(q.getQuestReward().getExp())));
		p1.add(new InteractiveText(p," " + I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward exp")
				.showText(I18n.locMsg(p,"QuestEditor.RewardExp.ShowText"))).changeLine();

		if (Main.getHooker().hasSkillAPIEnabled()) {
			p1.add(I18n.locMsg(p,"QuestEditor.RewardRPGExp", Integer.toString(q.getQuestReward().getSkillAPIExp())));
			p1.add(new InteractiveText(p," " + I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward saexp")
					.showText(I18n.locMsg(p,"QuestEditor.RewardRPGExp.ShowText"))).changeLine();
		}

		if (Main.getHooker().hasQuantumRPGEnabled()) {
			p1.add(I18n.locMsg(p,"QuestEditor.RewardRPGExp", Integer.toString(q.getQuestReward().getQRPGExp())));
			p1.add(new InteractiveText(p," " + I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward qrpgexp")
					.showText(I18n.locMsg(p,"QuestEditor.RewardRPGExp.ShowText"))).changeLine();
		}

		p1.add(I18n.locMsg(p,"QuestEditor.RewardFriendPoint"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward fp")
				.showText(I18n.locMsg(p,"QuestEditor.RewardFriendPoint.ShowText"))).changeLine();

		if (q.getQuestReward().hasFriendPoint()) {
			for (Integer n : q.getQuestReward().getFriendPointMap().keySet()) {
				NPC npc = CitizensAPI.getNPCRegistry().getById(n);
				if (npc == null)
					continue;
				p1.add("- ");
				p1.add(new InteractiveText(p,"").showNPCInfo(npc));
				p1.add("&0 " + q.getQuestReward().getFriendPointMap().get(n) + " " + I18n.locMsg(p,"QuestEditor.Point"));
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove reward fp " + n));
				p1.changeLine();
			}
		}

		p1.add(I18n.locMsg(p,"QuestEditor.RewardCommand"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew reward command")
				.showText(I18n.locMsg(p,"QuestEditor.RewardCommand.ShowText"))).changeLine();
		if (q.getQuestReward().hasCommand()) {
			int counter = 1;
			for (String s : q.getQuestReward().getCommands()) {
				p1.add(new InteractiveText(p,"- &0" + I18n.locMsg(p,"QuestEditor.Command") + "(" + counter + ")").showText("&f/" + s));
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward command " + (counter - 1)));
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove reward command " + (counter - 1)));
				p1.changeLine();
				counter++;
			}
		}
		p1.add(I18n.locMsg(p,"QuestEditor.RewardItem"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit reward item")
				.showText(I18n.locMsg(p,"QuestEditor.RewardItem.ShowText"))).changeLine();

		//QuestBookPage p5 = new QuestBookPage();
		p1.createNewPage();
		p1.add(I18n.locMsg(p,"QuestEditor.SaveAndExit")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncSevAndLoc")).clickCommand("/mq e sa").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncSev")).clickCommand("/mq e sl").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.SyncLoc")).clickCommand("/mq e sc").showText(I18n.locMsg(p,"QuestEditor.WarnSave")))
				.changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.QuitEditor")).clickCommand("/mq e exit")
				.showText(I18n.locMsg(p,"QuestEditor.ExitEditor.ShowText"))).changeLine();

		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void editQuestTrigger(Player p, TriggerType type, int stage) {
		if (!checkEditorMode(p, true))
			return;
		FlexibleBook book = new FlexibleBook();
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		book.add(I18n.locMsg(p,"QuestEditor.EditTrigger") + q.getQuestName()).changeLine();
		book.add(I18n.locMsg(p,"QuestEditor.EditTriggerType") + type.toCustomString(p,stage)).changeLine();
		int index = 0;
		int realIndex = -1;

		if (q.hasTrigger(type)) {
			for (TriggerObject obj : q.getTriggerMap().get(type)) {
				realIndex++;
				if (obj.getStage() != stage)
					continue;
				book.add("- " + index + ".");
				book.add(new InteractiveText(p,obj.getObjType().toCustomString(p))
						.showText(QuestChatManager.toNormalDisplay(I18n.locMsg(p,"QuestEditor.EditTriggerObjectType") + obj.getObject())));

				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add"))
						.clickCommand("/mq e addnew evt " + type.toString() + " " + stage + " " + (realIndex + 1) + " "));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit"))
						.clickCommand("/mq e edit evt " + type.toString() + " " + stage + " " + realIndex + " " + obj.getObjType().toString()));
				book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove"))
						.clickCommand("/mq e remove evt " + type.toString() + " " + stage + " " + realIndex));

				book.changeLine();
				index++;
			}
		}

		if (index == 0)
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add"))
					.clickCommand("/mq e addnew evt " + type.toString() + " " + stage + " " + 0 + " ")).changeLine();

		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e edit evt"));
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void editQuestStages(Player p) {
		if (!checkEditorMode(p, true))
			return;
		FlexibleBook book = new FlexibleBook();
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		book.add(I18n.locMsg(p,"QuestEditor.EditQuestStage")).changeLine();
		book.add(I18n.locMsg(p,"QuestEditor.ChooseStage")).changeLine();
		for (int i = 1; i <= q.getStages().size(); i++) {
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Stage", Integer.toString(i))).clickCommand("/mq e edit stage " + i));
			book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove stage " + i)).changeLine();
		}
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew stage")).changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e gui")).changeLine();
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void editQuestObjects(Player p, int stage) {
		if (!checkEditorMode(p, true))
			return;
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.EditStage", Integer.toString(stage))).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.ChooseObject")).changeLine();
		for (int i = 1; i <= q.getStage(stage - 1).getObjects().size(); i++) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Object", Integer.toString(i))).clickCommand("/mq e edit object " + stage + " " + i));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove object " + stage + " " + i)).changeLine();
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew object " + stage)).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e edit stage")).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void editQuestObject(Player p, int stage, int obj) {
		if (!checkEditorMode(p, true))
			return;
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		SimpleQuestObject o = q.getStage(stage - 1).getObject(obj - 1);
		if (!(o instanceof EditorObject)) {
			QuestEditorManager.editQuestObjects(p, stage);
			QuestChatManager.error(p, I18n.locMsg(p,"CustomObject.NotEditable"));
			return;
		}
		FlexibleBook p1 = new FlexibleBook();
		
		p1.add(I18n.locMsg(p,"QuestEditor.EditObject", Integer.toString(stage), Integer.toString(obj))).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.ObjectName") + o.getObjectName()).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " type")).changeLine();
		((EditorObject) o).formatEditorPage(p,p1, stage, obj);
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e edit stage " + stage)).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	//unfinished
	@SuppressWarnings("unchecked")
	public static void editQuestRequirement(Player p) {
		if (!checkEditorMode(p, true))
			return;
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		FlexibleBook p1 = new FlexibleBook();
		//FlexibleBook p1 = new FlexibleBook();
		//QuestBookPage p2 = new QuestBookPage();
		p1.add(I18n.locMsg(p,"QuestEditor.EditRequirement")).changeLine();

		// Level Req
		String infoLevel = ""+(q.getRequirements().get(RequirementType.LEVEL)==null?I18n.locMsg(p,"QuestEditor.NotSet"):q.getRequirements().get(RequirementType.LEVEL));
		p1.add(I18n.locMsg(p,"QuestEditor.LevelReq") + infoLevel + " ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req LEVEL")).changeLine();
		if(q.getRequirements().get(RequirementType.LEVEL)!=null){
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req LEVEL")).changeLine();
		}
	    //p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req LEVEL")).changeLine();

		// Money Req
		String infoMoney = ""+(q.getRequirements().get(RequirementType.MONEY)==null?I18n.locMsg(p,"QuestEditor.NotSet"):q.getRequirements().get(RequirementType.MONEY));
		p1.add(I18n.locMsg(p,"QuestEditor.MoneyReq") + infoMoney+ " ");
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req MONEY")).changeLine();
		if(q.getRequirements().get(RequirementType.MONEY)!=null){
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req MONEY")).changeLine();
		}

		// Item Req
		p1.add(I18n.locMsg(p,"QuestEditor.ItemReq"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req ITEM")).changeLine();
		
		p1.changeLine();
        
		// SkillAPI rubbish
		if (Main.getHooker().hasSkillAPIEnabled()) {
			p1.add(I18n.locMsg(p,"QuestEditor.SkillAPIReq")).changeLine();

			String classID = q.getRequirements().get(RequirementType.SKILLAPI_CLASS).toString();
			String displayName = classID.equalsIgnoreCase("none") ? "null" : SkillAPI.getClass(classID).getName();
			p1.add(I18n.locMsg(p,"QuestEditor.RPGClassReq", displayName, classID));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req SKILLAPI_CLASS")).changeLine();

			p1.add(I18n.locMsg(p,"QuestEditor.RPGLevelReq", q.getRequirements().get(RequirementType.SKILLAPI_LEVEL).toString()));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req SKILLAPI_LEVEL")).changeLine();

			boolean allow = (Boolean) q.getRequirements().get(RequirementType.ALLOW_DESCENDANT);
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.AllowDescendant"))
					.showText(I18n.locMsg(p,"QuestEditor.AllowDescendant.ShowText." + !allow))
					.clickCommand("/mq e edit req ALLOW_DESCENDANT " + !allow));
			p1.add(I18n.locMsg(p,"QuestEditor." + allow)).changeLine();
		}
		
		//Quantumrpg rubbish
		if (Main.getHooker().hasQuantumRPGEnabled()) {
			p1.add(I18n.locMsg(p,"QuestEditor.QRPGReq")).changeLine();

			String classID = q.getRequirements().get(RequirementType.QRPG_CLASS).toString();
			String displayName = classID.equalsIgnoreCase("none") ?
					"null" :
					Main.getHooker().getQuantumRPG().getModuleCache().getClassManager().getClassById(classID).getName();
			p1.add(I18n.locMsg(p,"QuestEditor.RPGClassReq", displayName, classID));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req QRPG_CLASS")).changeLine();

			p1.add(I18n.locMsg(p,"QuestEditor.RPGLevelReq", q.getRequirements().get(RequirementType.QRPG_LEVEL).toString()));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req QRPG_LEVEL")).changeLine();

			boolean allow = (Boolean) q.getRequirements().get(RequirementType.ALLOW_DESCENDANT);
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.AllowDescendant"))
					.showText(I18n.locMsg(p,"QuestEditor.AllowDescendant.ShowText." + !allow))
					.clickCommand("/mq e edit req ALLOW_DESCENDANT " + !allow));
			p1.add(I18n.locMsg(p,"QuestEditor." + allow)).changeLine();
		}
		


		// Page 2 (not sure,cuz using flexible book now)
		// Quest Req
		p1.add(I18n.locMsg(p,"QuestEditor.QuestReq")).changeLine();
		int counter = 0;
		if(q.getRequirements().get(RequirementType.QUEST)!=null) {
			for (String s : (List<String>) q.getRequirements().get(RequirementType.QUEST)) {
				if (QuestUtil.getQuest(s) == null)
					continue;
				Quest quest = QuestUtil.getQuest(s);
				p1.add("&0&l- " + quest.getQuestName() + "&0(" + s + ")");
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req QUEST " + counter)).changeLine();
				counter++;
			}
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew req QUEST")).changeLine();

		counter = 0;
		// Permission Req
		p1.add(I18n.locMsg(p,"QuestEditor.PermissionReq")).changeLine();
		for (String s : (List<String>) q.getRequirements().get(RequirementType.PERMISSION)) {
			p1.add("&0&l- " + s);
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req PERMISSION " + counter)).changeLine();
			counter++;
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew req PERMISSION")).changeLine();

		// Friend Points Req
		p1.add(I18n.locMsg(p,"QuestEditor.FriendPointReq"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req FRIEND_POINT")).changeLine();
		for (Integer id : ((HashMap<Integer, Integer>) q.getRequirements().get(RequirementType.FRIEND_POINT)).keySet()) {
			if (!QuestValidater.validateNPC(Integer.toString(id)))
				continue;
			NPC npc = Main.getHooker().getNPC(id);
			p1.add("- ");
			p1.add(new InteractiveText(p,"").showNPCInfo(npc));
			p1.add("&0 " + ((HashMap<Integer, Integer>) q.getRequirements().get(RequirementType.FRIEND_POINT)).get(npc.getId()) + " " + I18n
					.locMsg(p,"QuestEditor.Point"));
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req FRIEND_POINT " + npc.getId()));
			p1.changeLine();
		}


		
		String startTime = "none";
		String endTime = "none";
		if(q.getRequirements().get(RequirementType.SERVER_TIME)!=null && !((List<Long>)q.getRequirements().get(RequirementType.SERVER_TIME)).isEmpty()) {
			List<Long> time = (List<Long>) q.getRequirements().get(RequirementType.SERVER_TIME);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			startTime = sdf.format(new Date(time.get(0)));
			endTime = sdf.format(new Date(time.get(1)));
		}
		p1.add(I18n.locMsg(p,"QuestEditor.ServerTime",startTime,endTime));
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req SERVER_TIME"));
		if(q.getRequirements().get(RequirementType.SERVER_TIME)!=null&&!((List<Long>)q.getRequirements().get(RequirementType.SERVER_TIME)).isEmpty()) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req SERVER_TIME 69696969"));

			//the last argument is useless in this case
		}
		p1.changeLine();
		
		startTime = "none";
		endTime = "none";
		if(q.getRequirements().get(RequirementType.WORLD_TIME)!=null &&  !((List<Long>)q.getRequirements().get(RequirementType.WORLD_TIME)).isEmpty()) {
			List<Long> time = (List<Long>) q.getRequirements().get(RequirementType.WORLD_TIME);
			startTime = time.get(0)+"";
			endTime = time.get(1)+"";
		}
		p1.add(I18n.locMsg(p,"QuestEditor.WorldTime",startTime,endTime));
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit req WORLD_TIME"));
		if(q.getRequirements().get(RequirementType.WORLD_TIME)!=null&&!((List<Long>)q.getRequirements().get(RequirementType.WORLD_TIME)).isEmpty()) {
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req WORLD_TIME 69696969"));
			//the last argument is useless in this case
		}
		p1.changeLine();
		if(Main.getHooker().hasPlaceholderAPIEnabled()) {
			p1.add(I18n.locMsg(p,"QuestEditor.PlaceholderAPI")).changeLine();
			for (String s : ((Map<String,String>) q.getRequirements().get(RequirementType.PLACEHOLDER_API)).keySet()) {
				p1.add("&0&l- " + s + ":"+ ((Map<String,String>) q.getRequirements().get(RequirementType.PLACEHOLDER_API)).get(s));
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req PLACEHOLDER_API " + s)).changeLine();
			}
			p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Add")).clickCommand("/mq e addnew req PLACEHOLDER_API")).changeLine();
		}
		
		p1.changeLine();
		
		
		if(Main.getHooker().hasmcMMOEnabled()) {
			p1.add(I18n.locMsg(p, "QuestEditor.mcMMO")).changeLine();
			for(Entry<PrimarySkillType,Integer> entry:((Map<PrimarySkillType,Integer>)q.getRequirements().get(RequirementType.MCMMO_LEVEL)).entrySet()) {
				if(p1.getCurrentPage().isOutOfBounds()) {
					p1.createNewPage();
				}
				p1.add("&0&l- "+I18n.locMsg(p, "QuestEditor.mcMMOClass") + entry.getKey().name()+" "+I18n.locMsg(p, "QuestEditor.mcMMOLevel")+entry.getValue());
				p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Remove")).clickCommand("/mq e remove req MCMMO_LEVEL "+entry.getKey().name()));
				p1.changeLine();
			}
			p1.add(new InteractiveText(p,I18n.locMsg(p, "QuestEditor.Add")).clickCommand("/mq e addnew req MCMMO_LEVEL"));
			p1.changeLine();
		}
		
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e gui")).changeLine();
		
		QuestBookGUIManager.openBook(p,p1.toSendableBook());
	}

	public static void selectTriggerType(Player p, String mode) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.SelectTriggerType")).changeLine();
		for (TriggerType t : TriggerType.values()) {
			p1.add(new InteractiveText(p,"- [" + t.toCustomString(p) + "]").clickCommand("/mq e " + mode + " evt " + t.toString()));
			p1.changeLine();
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e gui")).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectTriggerStage(Player p, String mode, TriggerType t) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.SelectTriggerStage")).changeLine();
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		for (int s = 1; s <= q.getStages().size(); s++) {
			p1.add(new InteractiveText(p,"- " + I18n.locMsg(p,"QuestEditor.Stage", Integer.toString(s)))
					.clickCommand("/mq e " + mode + " evt " + t.toString() + " " + s));
			p1.changeLine();
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e " + mode + " evt")).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectTriggerObjType(Player p, TriggerType t, int stage, int index) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.SelectTriggerObj")).changeLine();
		for (TriggerObjectType otype : TriggerObjectType.values()) {
			p1.add(new InteractiveText(p,"- [" + otype.toCustomString(p) + "]")
					.clickCommand("/mq e addnew evt " + t.toString() + " " + stage + " " + index + " " + otype.name()));
			p1.changeLine();
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e edit evt " + t.toString())).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectObjectType(Player p, int stage, int obj) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.SelectObjectType")).changeLine();
		for (String s : SimpleQuestObject.ALL_OBJECTS.keySet()) {
			SimpleQuestObject.initObjectNames(p);
			p1.add(new InteractiveText(p,"- [" + SimpleQuestObject.ALL_OBJECTS.get(s) + "]")
					.clickCommand("/mq e edit object " + stage + " " + obj + " type " + s));
			p1.changeLine();
		}
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e edit object " + stage + " " + obj)).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void selectQuest(Player p, String cmd) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p,"QuestEditor.ChooseTargetQuest")).changeLine();
		for (Quest q : QuestStorage.localQuests.values()) {
			book.add(new InteractiveText(p,"&0- &l" + q.getQuestName() + "&0(" + q.getInternalID() + ")").clickCommand(cmd + " " + q.getInternalID()));
			book.changeLine();
		}
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void createQuest(Player p) {
		FlexibleBook p1 = new FlexibleBook();
		p1.add(I18n.locMsg(p,"QuestEditor.NewQuestTitle")).changeLine();
		p1.add(I18n.locMsg(p,"QuestEditor.NewQuestDesc")).changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.NewQuestButton")).clickCommand("/mq e newquest create"));
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		p1.changeLine();
		String id = (q.getInternalID() != null) ? q.getInternalID() : I18n.locMsg(p,"QuestEditor.NotSet");
		p1.add(I18n.locMsg(p,"QuestEditor.NewQuestID"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e newquest id")).changeLine();
		p1.add(id).changeLine();
		String name = (q.getQuestName() != null) ? q.getQuestName() : I18n.locMsg(p,"QuestEditor.NotSet");
		p1.add(I18n.locMsg(p,"QuestEditor.NewQuestName"));
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e newquest name")).changeLine();
		p1.add(name).changeLine();
		p1.changeLine();
		p1.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Return")).clickCommand("/mq e")).changeLine();
		QuestBookGUIManager.openBook(p, p1.toSendableBook());
	}

	public static void generateEditItemGUI(Player p, String type, List<ItemStack> list) {
		if (checkEditorMode(p, false)) {
			Quest q = QuestEditorManager.getCurrentEditingQuest(p);
			Inventory inv = Bukkit.createInventory(null, 27, "[" + type + "]" + q.getQuestName());
			inv.addItem(list.toArray(new ItemStack[0]));
			p.openInventory(inv);
		}
	}
}
