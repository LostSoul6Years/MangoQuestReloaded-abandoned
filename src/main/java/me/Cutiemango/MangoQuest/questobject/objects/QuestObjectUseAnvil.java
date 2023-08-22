package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Material;
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
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectUseAnvil  extends NumerableObject implements EditorObject{
	private ItemStack first;
	private ItemStack second;
	private ItemStack result = new ItemStack(Material.APPLE);
	private int durabilityThreshold = 0;
		
	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "first":{
			first = Main.getInstance().mcCompat.getItemInMainHand(sender).clone();
			return true;
		}
		case "second":{
			second = Main.getInstance().mcCompat.getItemInMainHand(sender).clone();
			return true;
		}
		case "result":{
			result = Main.getInstance().mcCompat.getItemInMainHand(sender).clone();
			return true;
		}
		case "durability":{
			durabilityThreshold = Integer.parseInt(obj);
			return true;
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {
		case "first":
		case "second":
		case "result":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.RightClick"));
			return eobj;
		}
		case "durability":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,Syntax.of("I", I18n.locMsg(sender, "Syntax.Number")),(arg)->{
				try {
					Integer.parseInt(arg);					
					return true;
				}catch(IllegalArgumentException e) {
					return false;
				}
				
			});
			return eobj;
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		if(first == null)
			page.add(I18n.locMsg(p, "QuestEditor.First")+I18n.locMsg(p, "QuestEditor.NotSet"));
		else {
			page.add(I18n.locMsg(p, "QuestEditor.First"));
			page.add(new InteractiveText(p, first));
		}
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " first"));
		page.changeLine();
		
		if(second == null)
			page.add(I18n.locMsg(p, "QuestEditor.Second")+I18n.locMsg(p, "QuestEditor.NotSet"));
		else {
			page.add(I18n.locMsg(p, "QuestEditor.Second"));
			page.add(new InteractiveText(p, second));
		}
		
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " second"));
		page.changeLine();
		
		page.add(I18n.locMsg(p, "QuestEditor.Result"));
		page.add(new InteractiveText(p, result));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " result"));
		page.changeLine();
		
		page.add(I18n.locMsg(p, "QuestEditor.DurabilityThreshold")+durabilityThreshold);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " durability"));
		page.changeLine();
		
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {
		String first = "";
		String second = "";
		String result = "";
		if(first!= null) {
			if(this.first.hasItemMeta()&&this.first.getItemMeta().hasDisplayName()) {
				first = this.first.getItemMeta().getDisplayName();
			}else {
				first = QuestUtil.translate(this.first);
			}
		}
		if(second!= null) {
			if(this.second.hasItemMeta()&&this.second.getItemMeta().hasDisplayName()) {
				second = this.second.getItemMeta().getDisplayName();
			}else {
				second = QuestUtil.translate(this.second);
			}
		}
		if(this.result.hasItemMeta()&&this.result.getItemMeta().hasDisplayName()) {
			result = this.result.getItemMeta().getDisplayName();
		}else {
			result = QuestUtil.translate(this.result);
		}
		return I18n.locMsg(null, "QuestObject.UseAnvil",first,second,result,
						amount+"");
	}
	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.UseAnvil")), isFinished,first==null?"":first,
				second==null?"":second,result,amount+"");
	}

	@Override
	public boolean load(QuestIO config, String path) {
		if(config.contains(path+"first")) {
			first = config.getItemStack(path+"first");
		}
		if(config.contains(path+"second")) {
			second = config.getItemStack(path+"second");
		}
		result = config.getItemStack(path+"result");
		durabilityThreshold = config.getInt(path+"durability");
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {		
		if(first!=null) {
			config.set(objpath+"first", first);
		}
		if(second!=null) {
			config.set(objpath+"second", second);
		}
		config.set(objpath+"result", result);
		config.set(objpath+"durability", durabilityThreshold);
		super.save(config, objpath);
	}

	@Override
	public String getConfigString() {
		return "USE_ANVIL";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null, "QuestObjectName.UseAnvil");
	}



	public ItemStack getFirst() {
		return first;
	}

	public void setFirst(ItemStack first) {
		this.first = first;
	}

	public ItemStack getSecond() {
		return second;
	}

	public void setSecond(ItemStack second) {
		this.second = second;
	}

	public ItemStack getResult() {
		return result;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public int getDurabilityThreshold() {
		return durabilityThreshold;
	}

	public void setDurabilityThreshold(int durabilityThreshold) {
		this.durabilityThreshold = durabilityThreshold;
	}

}
