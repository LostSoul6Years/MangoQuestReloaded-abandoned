package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectShearSheep extends NumerableObject implements EditorObject{
	
	public QuestObjectShearSheep() {
		
	}
	
	public QuestObjectShearSheep(int amount) {
		super.setAmount(amount);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.ShearSheep", Integer.toString(amount));
	}

	@Override
	public String getConfigString() {
		return "SHEAR_SHEEP";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.ShearSheep");
	}
	
	@Override
	public boolean load(QuestIO config, String path) {
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		super.save(config, objpath);
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.ShearSheep")), isFinished, amount);
	}
	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		return super.receiveCommandInput(sender, type, obj);
	};
	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		return super.createCommandOutput(sender, command, type);
	}
	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		//page.add(I18n.locMsg(null,"QuestEditor.ShearSheep"));
		//page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}



}
