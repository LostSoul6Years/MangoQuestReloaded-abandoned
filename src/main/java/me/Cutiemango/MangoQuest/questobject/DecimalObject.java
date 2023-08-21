package me.Cutiemango.MangoQuest.questobject;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;

public abstract class DecimalObject extends SimpleQuestObject implements EditorObject{
	protected double amount = 10;

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(p,"QuestEditor.TargetAmount") + amount);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " amount"));
		page.changeLine();
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		if (type.equals("amount") && QuestValidater.validateDouble(obj)) {
			setAmount(Double.parseDouble(obj));
			return true;
		}
		return false;
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj = null;
		if (type.equals("amount")) {
			obj = new EditorListenerObject(ListeningType.STRING, command, Syntax.regexCreate("(\\d+)(\\.?)(\\d*)", I18n.locMsg(null,"Syntax.Decimal")));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterAmount"));
		}
		return obj;
	}

	@Override
	public boolean load(QuestIO config, String path) {
		amount = config.getDouble(path + "Amount");
		return true;
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath + "Amount", amount);
	}
}
