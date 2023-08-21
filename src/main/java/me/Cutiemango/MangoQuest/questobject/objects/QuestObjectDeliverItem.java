package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestNPCManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.ItemObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.NPCObject;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectDeliverItem extends ItemObject implements NPCObject, EditorObject
{

	// Reserved for initializing with load()
	public QuestObjectDeliverItem() {
	}

	public QuestObjectDeliverItem(NPC n, ItemStack is, int deliverAmount) {
		npc = n;
		item = is;
		amount = deliverAmount;
	}

	@Override
	public String getConfigString() {
		return "DELIVER_ITEM";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.DeliverItem");
	}

	private NPC npc;

	@Override
	public void setAmount(int i) {
		amount = i;
	}

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
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.DeliverItem")), isFinished, amount, item, npc);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.DeliverItem", Integer.toString(amount), QuestUtil.getItemName(item), me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc));
	}

	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null,"QuestEditor.DeliverItem"));
		page.add(new InteractiveText(p,item));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " item")).changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.DeliverNPC"));
		if (npc == null)
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.NotSet")));
		else
			page.add(new InteractiveText(p,"").showNPCInfo(npc));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " itemnpc"))
				.changeLine();
		super.formatEditorPage(p,page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		String s = config.getString(path + "TargetNPC");
		if(s == null) {
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NPCNotValid", s));
		}
		if (!QuestValidater.validateNPC(s)) {
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NPCNotValid", s));
			return false;
		}
		npc = Main.getHooker().getNPC(s);
		if (!QuestNPCManager.hasData(npc.getId()))
			QuestNPCManager.registerNPC(npc);
		item = config.getItemStack(path + "Item");
		amount = item.getAmount();
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath + "TargetNPC", npc.getId());
		config.set(objpath + "Item", item);
		super.save(config, objpath);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		if (type.equals("itemnpc")) {
			if (!QuestValidater.validateNPC(obj))
				return false;
			setTargetNPC(Main.getHooker().getNPC(obj));
			return true;
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj;
		if (type.equals("itemnpc")) {
			obj = new EditorListenerObject(ListeningType.NPC_LEFT_CLICK, command, Syntax.of("N", I18n.locMsg(null,"Syntax.NPCID"), ""));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.ClickNPC"));
			return obj;
		}
		return super.createCommandOutput(sender, command, type);
	}

}
