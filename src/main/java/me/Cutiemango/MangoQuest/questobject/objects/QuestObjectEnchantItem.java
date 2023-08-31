package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.QuestUtil.Comparison;
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

public class QuestObjectEnchantItem extends NumerableObject implements EditorObject{
	private Comparison compare = Comparison.EQUAL;
	private Enchantment type = null;
	private int level = 1;
	private Material itemType = Material.DIAMOND_SWORD;
	private boolean useExactItem = false;

	private ItemStack requiredItem;
	
	@SuppressWarnings("deprecation")
	@Override
public String toDisplayText(Player p) {
		String enchantSubject = "";
		if(useExactItem&&requiredItem.hasItemMeta()&&requiredItem.getItemMeta().hasDisplayName()) {
			enchantSubject = requiredItem.getItemMeta().getDisplayName();
		}else if(useExactItem){
			if(!requiredItem.hasItemMeta())
				enchantSubject = QuestUtil.translate(p, requiredItem);
			else
				enchantSubject = requiredItem.getItemMeta().getDisplayName();
		}else {
			enchantSubject = QuestUtil.translate(p, itemType);
		}
		String displayString = I18n.locMsg(null,"QuestObject.EnchantItem");
		if(type!=null) {
			displayString += I18n.locMsg(null,"QuestObject.EnchantItemIfSpecified",type.getName(),compare==Comparison.EQUAL?"":I18n.locMsg(null,compare.getDisplayPath()),level+"");
		}
		displayString += I18n.locMsg(null, "QuestObject.EnchantItemAmount");
		return displayString.replace("[%0]", enchantSubject).replace("[%1]", amount+"");
	}
		
	@SuppressWarnings("deprecation")
	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		String displayString = I18n.locMsg(p,"QuestObject.EnchantItem");
		if(type!=null) {
			displayString += I18n.locMsg(null,"QuestObject.EnchantItemIfSpecified",type.getName(),compare==Comparison.EQUAL?"":I18n.locMsg(null,compare.getDisplayPath()),level+"");
		}
		displayString += I18n.locMsg(p, "QuestObject.EnchantItemAmount");
		return super.toTextComponent(p,ChatColor.stripColor(displayString), isFinished,useExactItem?requiredItem:QuestUtil.translate(p, itemType)
				,amount+"");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean load(QuestIO config, String path) {
		useExactItem = config.getBoolean(path+"useExactItem");
		if(useExactItem) {
			requiredItem = config.getItemStack(path+"requiredItem");
		}else {
			itemType = Material.getMaterial(config.getString(path+"itemType"));
		}
		compare = Comparison.valueOf(config.getString(path+"comparison"));
		level = config.getInt(path+"level");
		if(config.contains(path+"enchantType"))
			type = Enchantment.getByName(config.getString(path+"enchantType"));
		return super.load(config,path);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath+"useExactItem", useExactItem);
        if(useExactItem) {
            config.set(objpath+"requiredItem", requiredItem);
        }else {
        	config.set(objpath+"itemType", itemType.name());
        }
        config.set(objpath+"comparison", compare.name());
        if(type!=null)
        	config.set(objpath+"enchantType",type.getName());
        
        config.set(objpath+"level", level);
		super.save(config, objpath);
		
	}

	@Override
	public String getConfigString() {
		return "ENCHANT_ITEM";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null, "QuestObjectName.EnchantItem");
	}



	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "useExactItem":{
			try {
				useExactItem = Boolean.valueOf(obj);
			}catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		case "requiredItem":{
			requiredItem = Main.getInstance().mcCompat.getItemInMainHand(sender);
			return true;
		}
		case "itemType":{
			itemType = Main.getInstance().mcCompat.getItemInMainHand(sender).getType();
			return true;
		}
		case "enchantmentType":{
			ItemStack book = Main.getInstance().mcCompat.getItemInMainHand(sender);
			if(book.getType()!=Material.ENCHANTED_BOOK) {
				return false;
			}
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();			
			this.type = meta.getStoredEnchants().keySet().toArray(new Enchantment[meta.getStoredEnchants().size()])[0];
			return true;
		}
		case "comparison":{
			compare = Comparison.valueOf(obj.toUpperCase());
			return true;
		}
		case "enchantmentLevel":{
			level = Integer.parseInt(obj);
			return true;
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {

		case "requiredItem":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.RightClick"));
			return eobj;
		}
		case "itemType":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.RightClick"));
			return eobj;
		}
		case "enchantmentType":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null,(arg)->{
				if(Main.getInstance().mcCompat.getItemInMainHand(sender).getType()!=Material.ENCHANTED_BOOK) {
					return false;
				}
				return true;
			});
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnchantmentType"));
			return eobj;
		}
		case "comparison":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null,(arg)->{
				try {
					Comparison.valueOf(arg.toUpperCase());
					return true;
				}catch(IllegalArgumentException e) {
					return false;
				}
				
			});
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.ComparisonType"));
			return eobj;
		}
		case "enchantmentLevel":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,Syntax.of("I", I18n.locMsg(sender, "Syntax.Number")));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterAmount"));
			return eobj;
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null, "QuestEditor.UseExactItem")+useExactItem);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " useExactItem "+!useExactItem));
		page.changeLine();
		if(useExactItem) {
			//QuestEditor.ConsumeItem
			if(requiredItem!=null) {
				page.add(I18n.locMsg(null, "QuestEditor.ConsumeItem"));
				page.add(new InteractiveText(p,requiredItem));
			}else {
				page.add(I18n.locMsg(null, "QuestEditor.ConsumeItem")+I18n.locMsg(p, "QuestEditor.NotSet"));
			}
			page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " requiredItem"));
		}else {
			//TODO: finish this piece of shit
			page.add(I18n.locMsg(null, "QuestEditor.MaterialType")+QuestUtil.translate(p, itemType));
			page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " itemType"));
		}
		page.changeLine();
		if(type!=null) {
			page.add(I18n.locMsg(p,"QuestEditor.EnchantmentType")+type.getName());
		}else {
			page.add(I18n.locMsg(p,"QuestEditor.EnchantmentType")+I18n.locMsg(p,"QuestEditor.NotSet"));}
		
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " enchantmentType"));
		page.changeLine();
		if(type!=null) {
		page.add(I18n.locMsg(p, "QuestEditor.ComparisonType")+I18n.locMsg(p, compare.getDisplayPath()));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " comparison"));
		page.changeLine();
		
		page.add(I18n.locMsg(p, "QuestEditor.EnchantmentLevel")+level);
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " enchantmentLevel"));
		page.changeLine();
		}
		
		super.formatEditorPage(p, page, stage, obj);
	}

	public Comparison getCompare() {
		return compare;
	}

	public void setCompare(Comparison compare) {
		this.compare = compare;
	}

	public Enchantment getType() {
		return type;
	}

	public void setType(Enchantment type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Material getItemType() {
		return itemType;
	}

	public void setItemType(Material itemType) {
		this.itemType = itemType;
	}

	public ItemStack getRequiredItem() {
		return requiredItem;
	}

	public void setRequiredItem(ItemStack requiredItem) {
		this.requiredItem = requiredItem;
	}
	
	public boolean isUseExactItem() {
		return useExactItem;
	}

	public void setUseExactItem(boolean useExactItem) {
		this.useExactItem = useExactItem;
	}

}
