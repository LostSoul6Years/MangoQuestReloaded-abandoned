package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectPlayerChat extends NumerableObject implements EditorObject{
	private String filter="(.+)你好(.+)";
	//the displayname used in locMsg
	private String displayName="你好";
    private boolean useRegex = false;
	@Override
public String toDisplayText(Player p) {
		if(useRegex) {
			return I18n.locMsg(p,"QuestObject.PlayerChat",displayName,amount+"");
		}else {
			return I18n.locMsg(p,"QuestObject.PlayerChat",filter,amount+"");
		}
	}

	@Override
	public String getConfigString() {
		return "PLAYER_CHAT";
	}

	@Override
	public String getObjectName() {		
		return I18n.locMsg(null,"QuestObjectName.PlayerChat");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		if(useRegex) {
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.PlayerChat")), isFinished, displayName, amount);
		}else {
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.PlayerChat")), isFinished, filter, amount);
		}
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "useRegex":{
			if(obj.equalsIgnoreCase("true")) {
				useRegex = true;
			}else if(obj.equalsIgnoreCase("false")) {
				useRegex = false;
			}else {
				return false;
			}
			return true;
		}
		case "filter":{
			this.filter = obj;
			return true;
		}
		case "displayName":{
			if(!useRegex) {
				return false;
			}
			this.displayName = obj;
			return true;
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}
	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj;
		switch(type) {
		case "filter":{
			if(!useRegex) {
				obj = new EditorListenerObject(ListeningType.STRING, command, null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterCondition"));
			}else {
				obj = new EditorListenerObject(ListeningType.STRING,command,null,(regex)->{
					try {
						Pattern.compile(regex);
					}catch(Exception e) {
						return false;
					}				
					
					return true;
				});
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterRegexCondition"));
			}
			
			return obj;
		}
		case "displayName":{
			if(!useRegex) {
				break;
			}
			obj = new EditorListenerObject(ListeningType.STRING,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterConditionDisplayName"));
			return obj;
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
      page.add(I18n.locMsg(p, "QuestEditor.UseRegex")+String.valueOf(useRegex));
      page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " useRegex "+String.valueOf(!useRegex)));
      page.changeLine();
      page.add(I18n.locMsg(p, "QuestEditor.Filter")+filter);
      page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " filter"));
      page.changeLine();
      if(useRegex) {
          page.add(I18n.locMsg(p, "QuestEditor.DisplayName")+displayName);
          page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " displayName"));
          page.changeLine();
      }
      super.formatEditorPage(p, page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		useRegex = config.getBoolean(path+"useRegex");
		displayName = config.getString(path+"displayName");
		filter = config.getString(path+"filter");
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath+"filter", filter);
		config.set(objpath+"displayName", displayName);
		config.set(objpath+"useRegex", useRegex);
	    super.save(config, objpath);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isUseRegex() {
		return useRegex;
	}

	public void setUseRegex(boolean useRegex) {
		this.useRegex = useRegex;
	}
	

}
