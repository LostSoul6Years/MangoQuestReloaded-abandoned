package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectBreakBlock extends NumerableObject implements EditorObject
{
	// Reserved for initializing with load()
	public QuestObjectBreakBlock() {
	}

	public QuestObjectBreakBlock(Material m, int i) {
		if (!m.isBlock())
			return;
		block = m;
		amount = i;
	}

	@Override
	public String getConfigString() {
		return "BREAK_BLOCK";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.BreakBlock");
	}

	private Material block;

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(p,"QuestObject.BreakBlock")), isFinished, amount, block);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.BreakBlock", Integer.toString(amount), QuestUtil.translate(block), "");
	}

	public Material getType() {
		return block;
	}

	public void setType(Material m) {
		block = m;
	}

	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null,"QuestEditor.BreakBlock") + QuestUtil.translate(block));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " block"));
		page.changeLine();
		super.formatEditorPage(p,page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		block = Material.getMaterial(config.getString(path + "BlockType"));
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath + "BlockType", block.toString());
		super.save(config, objpath);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		if (type.equals("block")) {
			Material mat = Material.getMaterial(obj);
			setType(mat);
			DebugHandler.log(5, "Material registered: " + block.toString());
			return true;
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj;
		if (type.equals("block")) {
			obj = new EditorListenerObject(ListeningType.BLOCK, command, null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.BreakBlock"));
			return obj;
		}
		return super.createCommandOutput(sender, command, type);
	}
}
