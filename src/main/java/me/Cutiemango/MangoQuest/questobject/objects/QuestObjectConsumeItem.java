package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.questobject.ItemObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectConsumeItem extends ItemObject implements EditorObject
{
	// Reserved for initializing with load()
	public QuestObjectConsumeItem() {
	}

	public QuestObjectConsumeItem(ItemStack is, int i) {
		item = is;
		amount = i;
	}
	

	@Override
	public String getConfigString() {
		return "CONSUME_ITEM";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.ConsumeItem");
	}

	@Override
	public void setAmount(int i) {
		item.setAmount(i);
		amount = i;
	}

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.ConsumeItem")), isFinished, amount, item);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.ConsumeItem", Integer.toString(amount), QuestUtil.getItemName(item));
	}

	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null,"QuestEditor.ConsumeItem"));
		page.add(new InteractiveText(p,item));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " item")).changeLine();
		super.formatEditorPage(p,page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		item = config.getItemStack(path + "Item");
		amount = item.getAmount();
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath + "Item", item);
		super.save(config, objpath);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		return super.createCommandOutput(sender, command, type);
	}

}
