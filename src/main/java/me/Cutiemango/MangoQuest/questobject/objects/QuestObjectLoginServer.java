package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.TimeHandler;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectLoginServer extends NumerableObject implements EditorObject {
	private int interval = 10000;

	public QuestObjectLoginServer(int amount, int interval) {
		super.setAmount(amount);
		this.setInterval(interval);
	}

	// reserved for load
	public QuestObjectLoginServer() {
		
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		if(type.equals("interval")) {
			DebugHandler.log(5, "login server interval registered: %s",obj);
	        //Pattern pat = Pattern.compile("(\\d+[dD])(\\d+[hH])(\\d+[mM])(\\d+[sS])");			
			int days = Integer.parseInt(obj.split("[dD]")[0]);
			obj = obj.replaceAll("\\d+[dD]", "");
			int hours = Integer.parseInt(obj.split("[hH]")[0]);
			obj = obj.replaceAll("\\d+[hH]", "");
		    int minutes = Integer.parseInt(obj.split("[mM]")[0]);
		    obj = obj.replaceAll("\\d+[mM]","");
		    int seconds = Integer.parseInt(obj.split("[sS]")[0]);
		    int interval = 0;
		    interval += days*86400;
		    interval += hours*3600;
		    interval += minutes*60;
		    interval+= seconds;
		    this.setInterval(interval);
			return true;
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		if(type.equals("interval")) {
			eobj = new EditorListenerObject(ListeningType.STRING, command, Syntax.regexCreate("(\\d+[dD])(\\d+[hH])(\\d+[mM])(\\d+[sS])", I18n.locMsg(null,"Syntax.Date")));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterInterval"));
			return eobj;
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		//page.add(I18n.locMsg(null,"QuestEditor.LoginServer"));page.changeLine();
		page.add(I18n.locMsg(null, "QuestEditor.LoginInterval")+this.interval);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " interval"));
		page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.LoginServer",TimeHandler.convertTime(interval*1000),amount+"");
	}

	@Override
	public boolean load(QuestIO config, String path) {
		this.interval = config.getInt(path+"interval");
		//this.amount = config.getInt(path+"amount"); //use super load for elegancy
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath+"interval", interval);
		super.save(config, objpath);
	}

	@Override
	public String getConfigString() {
		return "LOGIN_SERVER";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.LoginServer");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.LoginServer")), isFinished, TimeHandler.convertTime(interval*1000),amount);
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}
