package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
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

public class QuestObjectBucketFill extends NumerableObject implements EditorObject{

	private FillType type = FillType.WATER;
	private ItemStack requiredItem;
	
	public ItemStack getRequiredItem() {
		return requiredItem;
	}

    public void setRequiredItem(ItemStack requiredItem) {
		this.requiredItem = requiredItem;
	}

	public FillType getType() {
		return type;
	}

	public  void setType(FillType type) {
		this.type = type;
	}

	public static enum FillType{
		LAVA("QuestObject.FillLava"),WATER("QuestObject.FillWater");
		private String displayPath;
		public String getDisplayPath() {
			return displayPath;
		}
		private FillType(String displayPath) {
			this.displayPath = displayPath;
		}
	}
	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "filltype":{
			this.type = FillType.valueOf(obj.toUpperCase());
			return true;
		}
		case "item":{
			this.requiredItem = Main.getInstance().mcCompat.getItemInMainHand(sender);
			
			return true;
		}
		}
		
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {
		case "item":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender, "EditorMessage.RightClick"));
			return eobj;
		}
		case "filltype":{
			eobj = new EditorListenerObject(ListeningType.STRING,command, null,(arg)->{
				try {
					FillType.valueOf(arg.toUpperCase());
					return true;
				}catch(IllegalArgumentException e) {
					return false;
				}

			});
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender, "EditorMessage.FillType"));
			return eobj;
		}
		}
		return super.createCommandOutput(sender, command, type);
		
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		if(requiredItem == null) {
			page.add(I18n.locMsg(null,"QuestEditor.FillItemStack")+I18n.locMsg(null, "QuestEditor.NotSet"));
		}else {
			page.add(new InteractiveText(p, I18n.locMsg(null, "QuestEditor.FillItemStack")).showItem(requiredItem));
		}
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " item"));
		page.changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.FillType") + I18n.locMsg(null, type.getDisplayPath()));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " filltype"));
		page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(null, "QuestObject.Fill",I18n.locMsg(null, type.getDisplayPath()),super.getAmount()+"");
	}

	@Override
	public boolean load(QuestIO config, String path) {
		this.type = FillType.valueOf(config.getString(path+"type"));	
		this.requiredItem = config.getItemStack(path+"requiredItem");
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath+"type", type.name());
		config.set(objpath+"requiredItem", requiredItem);
		super.save(config, objpath);
	}

	@Override
	public String getConfigString() {		
		return "BUCKET_FILL";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null, "QuestObjectName.Fill");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.Fill")), isFinished, I18n.locMsg(null, type.getDisplayPath()),super.getAmount()+"");
	}

}
