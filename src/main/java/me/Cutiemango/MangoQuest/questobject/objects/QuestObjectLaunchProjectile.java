package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectLaunchProjectile extends NumerableObject implements EditorObject{
	//TODO: add a check 
	public enum ProjectileType{
		ARROW(false,false,"QuestObject.LaunchArrow"),FIREBALL(false,false,"QuestObject.LaunchFireball"),FISHING_ROD(false,false,"QuestObject.LaunchFishingRod"),SPLASH_POTIONS(true,false,"QuestObject.LaunchSplashPotions"),LINGERING_POTIONS(true,true,"QuestObject.LaunchLingeringPotions"),SNOWBALL(false,false,"QuestObject.LaunchSnowballs"),TRIDENT(true,true,"QuestObject.LaunchTrident"),BOTTLE_OF_ENCHANTMENT(false,false,"QuestObject.LaunchBottleOfEnchantment");
		private boolean legacyUnsupported;
		//og = 1.8
		private boolean ogUnsupported;
		private String displayPath;
		public String getDisplayPath() {
			return displayPath;
		}
		public boolean isLegacyUnsupported() {
			return legacyUnsupported;
		}
		public boolean isOgUnsupported() {
			return ogUnsupported;
		}
				
		ProjectileType(boolean legacyUnsupported,boolean ogUnsupported,String displayPath){
			this.legacyUnsupported = legacyUnsupported;
			this.ogUnsupported = ogUnsupported;
			this.displayPath = displayPath;
		}
	}
	private ProjectileType type = ProjectileType.ARROW;
	private ItemStack requiredItem;
	private Double distance;
	public ProjectileType getType() {
		return type;
	}

	public void setType(ProjectileType type) {
		this.type = type;
	}

	public ItemStack getRequiredItem() {
		return requiredItem;
	}

	public void setRequiredItem(ItemStack requiredItem) {
		this.requiredItem = requiredItem;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}


	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "projectiletype":{
			this.type = ProjectileType.valueOf(obj.toUpperCase());
			return true;
		}
		case "distance":{
			try {
				this.distance = Double.parseDouble(obj);
				return true;
			}catch (NumberFormatException e) {
				return false;
			}
		}
		case "requiredItem":{
			ItemStack item = Main.getInstance().mcCompat.getItemInMainHand(sender);
			if (item == null || item.getType() == Material.AIR) {
				QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.ItemInHand"));
				return false;
			} else {
				this.requiredItem = item.clone();
				QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.ItemRegistered"));
				return true;
			}
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {
		case "projectiletype":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null,(args)->{
				try {
					ProjectileType.valueOf(args.toUpperCase());
					return true;
				}catch(IllegalArgumentException e) {
					return false;
				}
				
			});
			//Editormessage.EnterProjectileType
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"Editormessage.EnterProjectileType"));
			return eobj;
		}
		case "distance":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,Syntax.regexCreate("(\\d+)(\\.?)(\\d*)", I18n.locMsg(null,"Syntax.Decimal")));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterDistance"));
			return eobj;
		}
		case "requiredItem":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.RightClick"));
			return eobj;
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(p,"QuestEditor.ProjectileType") + I18n.locMsg(p, type.getDisplayPath()));		
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " projectiletype"));
		page.changeLine();
		if(distance == null) {
			page.add(I18n.locMsg(p,"QuestEditor.Distance")+I18n.locMsg(p, "QuestEditor.NotSet"));
		}else {
			page.add(I18n.locMsg(p,"QuestEditor.Distance")+distance);			
		}
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " distance"));
		page.changeLine();
		if(requiredItem == null) {
			page.add(I18n.locMsg(p,"QuestEditor.RequiredItem")+I18n.locMsg(p, "QuestEditor.NotSet"));
		}else {
			page.add(I18n.locMsg(p,"QuestEditor.RequiredItem"));
			page.add(new InteractiveText(p, requiredItem));
		}
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " requiredItem"));
		page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {
		String displayText = I18n.locMsg(null, "QuestObject.LaunchProjectile",I18n.locMsg(null, type.getDisplayPath()),amount+"");
		if(distance != null) {
			displayText += I18n.locMsg(null, "QuestObject.LaunchProjectile.Distance",distance+"");
		}
		if(requiredItem !=null) {
			displayText += I18n.locMsg(null, "QuestObject.LaunchProjectile.Object",requiredItem.getItemMeta().getDisplayName());
		}
		return displayText;
	}

	@Override
	public boolean load(QuestIO config, String path) {
		this.type = ProjectileType.valueOf(config.getString(path+"type"));
		if(config.getConfig().isItemStack(path+"requiredItem")) {
			this.requiredItem = config.getItemStack(path+"requiredItem");
		}
		if(config.getConfig().isDouble(path+"distance")) {
			this.distance = config.getDouble(path+"distance");
		}
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String path) {
		config.set(path+"type", type.name());	
		if(requiredItem != null) {
			config.set(path+"requiredItem", requiredItem);
		}
		if(distance != null) {
			config.set(path+"distance", distance);
		}

		super.save(config, path);
	}

	@Override
	public String getConfigString() {
		return "LAUNCH_PROJECTILE";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.LaunchProjectile");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		String displayText = I18n.locMsg(null, "QuestObject.LaunchProjectile",I18n.locMsg(null, type.getDisplayPath()),amount+"");
		if(distance != null) {
			displayText += I18n.locMsg(null, "QuestObject.LaunchProjectile.Distance",distance+"");
		}
		if(requiredItem !=null) {
			displayText += I18n.locMsg(null, "QuestObject.LaunchProjectile.Object");
		}
		return super.toTextComponent(p,ChatColor.stripColor(displayText), isFinished, requiredItem);
	}

}
