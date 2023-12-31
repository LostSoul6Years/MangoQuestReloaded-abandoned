package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.editor.ConversationEditorManager;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestNPCManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.NPCObject;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectTalkToNPC extends SimpleQuestObject implements NPCObject, EditorObject
{

	public QuestObjectTalkToNPC() {
	}

	public QuestObjectTalkToNPC(NPC n) {
		npc = n;
	}

	@Override
	public String getConfigString() {
		return "TALK_TO_NPC";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.TalkToNPC");
	}

	private NPC npc;

	public NPC getTargetNPC() {
		return npc;
	}

	public void setTargetNPC(NPC targetNPC) {
		npc = targetNPC;
		if (!QuestNPCManager.hasData(npc.getId()))
			QuestNPCManager.registerNPC(npc);
	}

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.TalkToNPC")), isFinished, npc);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.TalkToNPC", MinecraftCompatability.getName(getTargetNPC()));
	}

	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null,"QuestEditor.TalkNPC"));
		if (npc == null)
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.NotSet")));
		else
			page.add(new InteractiveText(p,"").showNPCInfo(npc));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " npc")).changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.ActivateConversation"));
		if (!hasConversation())
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.NotSet")));
		else
			page.add(new InteractiveText(p,getConversation().getName() + "(" + activateConversation + ")"));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " conv")).changeLine();
		page.changeLine();
	}

	@Override
	public boolean load(QuestIO config, String path) {
		String id = config.getString(path + "TargetNPC");
		if (!QuestValidater.validateNPC(id)) {
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NPCNotValid", id));
			return false;
		}
		npc = Main.getHooker().getNPC(id);
		if (!QuestNPCManager.hasData(npc.getId()))
			QuestNPCManager.registerNPC(npc);
		return true;
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath + "TargetNPC", npc.getId());
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch (type) {
			case "npc":
				if (!QuestValidater.validateNPC(obj))
					return false;
				setTargetNPC(Main.getHooker().getNPC(obj));
				break;
			case "conv":
				if (ConversationManager.getConversation(obj) != null)
					setConversation(obj);
				break;
		}
		return true;
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj = null;
		switch (type) {
			case "npc":
				obj = new EditorListenerObject(ListeningType.NPC_LEFT_CLICK, command, Syntax.of("N", I18n.locMsg(null,"Syntax.NPCID"), ""));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.ClickNPC"));
				break;
			case "conv":
				ConversationEditorManager.selectConversation(sender, command);
				break;
		}
		return obj;
	}



}
