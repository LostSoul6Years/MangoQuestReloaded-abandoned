package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.QuestUtil.Comparison;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.QuestEditorManager;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

//to be continued...
public class QuestObjectPlaceholderAPI extends NumerableObject implements EditorObject{
	private String placeholder = "player_name";
	private String placeholderDisplayName=null;
	public String getPlaceholderDisplayName() {
		return placeholderDisplayName;
	}

	public void setPlaceholderDisplayName(String placeholderDisplayName) {
		this.placeholderDisplayName = placeholderDisplayName;
	}


	private int expectedInteger = 1;
	public int getExpectedInteger() {
		return expectedInteger;
	}

	public void setExpectedInteger(int expectedInteger) {
		this.expectedInteger = expectedInteger;
	}

	public double getExpectedDecimal() {
		return expectedDecimal;
	}

	public void setExpectedDecimal(double expectedDecimal) {
		this.expectedDecimal = expectedDecimal;
	}


	private String expectedString = "SakurajiKanade";
	private double expectedDecimal = 3.1415926535;
	private Mode mode = Mode.String;
	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public enum Mode{
		Integer("QuestEditor.PAPIInteger"),Decimal("QuestEditor.PAPIDecimal"),String("QuestEditor.PAPIString");
		
		private String displayPath;
		public String getDisplayPath() {
			return displayPath;
		}
		private Mode(String displayPath) {
			this.displayPath = displayPath;
		}
		
	}


	public String getExpectedString() {
		return expectedString;
	}

	public void setExpectedString(String expectedString) {
		this.expectedString = expectedString;
	}

	public Comparison getCompare() {
		return compare;
	}

	public void setCompare(Comparison compare) {
		this.compare = compare;
	}

	
	private Comparison compare = Comparison.EQUAL;
	
	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
	

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type){
		case "placeholder":{
			placeholder = obj.replace("%", "");
			return true;
		}
		case "placeholderdisplayname":{
			placeholderDisplayName = QuestChatManager.translateColor(obj);
			return true;
		}
		case "compare":{
			compare = QuestUtil.Comparison.valueOf(obj);
			return true;
		}
		case "value":{
			switch(mode){
			case String:{
				expectedString = obj;
				return true;
			}
			case Integer:{
				expectedInteger = Integer.parseInt(obj);
				return true;
			}
			case Decimal:{
				expectedDecimal = Double.parseDouble(obj);
				return true;
			}
			}
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {
		case "placeholder":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterValue"));
			return eobj;
		}
		case "placeholderdisplayname":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterValue"));
			return eobj;
		}
		case "mode":{
			String[] args = command.split(" ");
			switch(mode) {
			case Integer:
				mode = Mode.Decimal;
				break;
			case Decimal:
				mode = Mode.String;
				break;
			case String:
				mode = Mode.Integer;
				break;
			}
			sender.sendMessage(command);
			QuestEditorManager.editQuestObject(sender,Integer.parseInt(args[4]),Integer.parseInt(args[5]));
			return null;
		}
		case "compare":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null,(arg)->{
				try {
					QuestUtil.Comparison.valueOf(arg);
				}catch(IllegalArgumentException e) {
					return false;
				}
				return true;
			});
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender, "EditorMessage.ComparisonType"));
			return eobj;
		}
		case "value":{
			switch(mode){
			case String:{
				eobj = new EditorListenerObject(ListeningType.STRING,command,null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterValue"));
				return eobj;
			}
			case Integer:{
				eobj = new EditorListenerObject(ListeningType.STRING,command,Syntax.of("I", I18n.locMsg(sender, "QuestEditor.PAPIInteger")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterValue"));
				return eobj;
			}
			case Decimal:{
				eobj = new EditorListenerObject(ListeningType.STRING,command,Syntax.of("F", I18n.locMsg(sender, "QuestEditor.PAPIDecimal")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterValue"));
				return eobj;
			}
			}
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(p, "QuestEditor.Placeholder")+placeholder);		
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " placeholder"));
		page.changeLine();
		String papiDisplay = placeholderDisplayName;
		if(papiDisplay == null) {
			papiDisplay = I18n.locMsg(p, "QuestEditor.NotSet");			
		}
		page.add(I18n.locMsg(p, "QuestEditor.PlaceholderDisplayName")+papiDisplay);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " placeholderdisplayname"));
		page.changeLine();
		page.add(I18n.locMsg(p, "QuestEditor.PlaceholderValueType")+I18n.locMsg(p, mode.displayPath));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " mode"));
		page.changeLine();
		page.add(I18n.locMsg(p, "QuestEditor.PAPIComparison")+I18n.locMsg(p, compare.getDisplayPath()));
		if(mode!=Mode.String) {
			page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " compare"));
		}
		page.changeLine();
		String value = "error haha";
		switch(mode) {
		case Integer:
			value = expectedInteger+"";
			break;
		case Decimal:
			value = expectedDecimal+"";
			break;
		case String:
			value = expectedString;
			break;
		}
		page.add(I18n.locMsg(p, "QuestEditor.PAPIValue")+value);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " value"));
		page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {
		String display;
		if(placeholderDisplayName == null) {
			display = placeholder;
		}else {
			display = placeholderDisplayName;
		}
		String value = "error haha";
		switch(mode) {
		case Integer:
			value = expectedInteger+"";
			break;
		case Decimal:
			value = expectedDecimal+"";
			break;
		case String:
			value = expectedString;
			break;
		}
		return I18n.locMsg(null, "QuestObject.PlaceholderAPI", display,I18n.locMsg(null, compare.getDisplayPath()),value,amount+"");
	}

	
	@Override
	public boolean load(QuestIO config, String path) {
        placeholder = config.getString(path+"placeholder");
        placeholderDisplayName = config.getString(path+"placeholderDisplayName");
        mode = Mode.valueOf(config.getString(path+"mode"));
        compare = Comparison.valueOf(config.getString(path+"compare"));
		switch(mode) {
		case Integer:
			expectedInteger = Integer.parseInt(config.getString(path+"value"));
			break;
		case Decimal:
			expectedDecimal = Double.parseDouble(config.getString(path+"value"));
			break;
		case String:
			expectedString = config.getString(path+"value");
			break;
		}
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {

		config.set(objpath+"placeholder", placeholder);
		if(placeholderDisplayName != null) {
			config.set(objpath+"placeholderDisplayName", placeholderDisplayName);
		}
		config.set(objpath+"mode", mode.name());
		if(mode.equals(Mode.String)) {
			config.set(objpath+"compare", Comparison.EQUAL.name());
		}else {
		config.set(objpath+"compare", compare.name());
		}
		switch(mode) {
		case Integer:
			config.set(objpath+"value", expectedInteger);
			break;
		case Decimal:
			config.set(objpath+"value", expectedDecimal);
			break;
		case String:
			config.set(objpath+"value", expectedString);
			break;
		}
		super.save(config, objpath);
	}

	@Override
	public String getConfigString() {
		return "PLACEHOLDER_API";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null, "QuestObjectName.PlaceholderAPI");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		String display;
		if(placeholderDisplayName == null) {
			display = placeholder;
		}else {
			display = placeholderDisplayName;
		}
		String value = "error haha";
		switch(mode) {
		case Integer:
			value = expectedInteger+"";
			break;
		case Decimal:
			value = expectedDecimal+"";
			break;
		case String:
			value = expectedString;
			break;
		}
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.PlaceholderAPI")), isFinished,display,I18n.locMsg(null, compare.getDisplayPath()),value,amount+"");
	}
	
}
