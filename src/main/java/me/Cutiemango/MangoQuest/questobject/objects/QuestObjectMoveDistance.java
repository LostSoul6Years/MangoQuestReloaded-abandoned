package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectMoveDistance extends DecimalObject implements EditorObject{
	public enum VehicleDefault{
		FOOT("QuestObject.MoveOnFoot",EntityType.PLAYER),SWIMMING("QuestObject.MoveBySwimming",EntityType.PLAYER),ELYTRA("QuestObject.MoveWithElytra",EntityType.PLAYER)
		,MINECART("QuestObject.MoveInMinecart",EntityType.MINECART),HORSE("QuestObject.MoveByHorse",EntityType.HORSE),BOAT("QuestObject.MoveByBoat",EntityType.BOAT),PIG("QuestObject.MoveByPig",EntityType.PIG),MYTHIC_MOB("QuestEditor.MoveDistance.MythicMobs",null);
		private String path;
		private EntityType vehicle;
		VehicleDefault(String path,EntityType vehicle){
			this.path = path;
			this.vehicle = vehicle;
		}
		
		public String getPath() {
			return path;
		}
		public EntityType getVehicle() {
			return vehicle;
		}
		public static VehicleDefault valueOfSave(String get) {
			try {
				return VehicleDefault.valueOf(get.toUpperCase());
			}catch(IllegalArgumentException e) {
				e.printStackTrace();
				return VehicleDefault.FOOT;
			}
			
		}
	}
	private VehicleDefault method = VehicleDefault.FOOT;
	
	private boolean isBaby = false;
	private EntityType genericMobType;
	private EntityType type;
	private String customName="";
	private String mtmMob;
	public VehicleDefault getMethod() {
		return method;
	}
	public void setMethod(VehicleDefault method) {
		this.method = method;
	}
	
	/*
	 * @Deprecated
	 * 
	 * @Override public String toDisplayText(Player p) { return
	 * I18n.locMsg(null,"QuestObject.MoveDistance",I18n.locMsg(null,method.getPath()
	 * ),Double.toString(amount)); }
	 */
	
	public String toDisplayText(Player p) {
		if(mtmMob==null)
		    return I18n.locMsg(p,"QuestObject.MoveDistance",I18n.locMsg(p, method.getPath()),Double.toString(amount));
		else
			return I18n.locMsg(p,"QuestObject.MoveDistance",I18n.locMsg(p, "QuestObject.MoveByMythicMob",QuestChatManager.translateColor(customName)),Double.toString(amount));
	}
	@Override
	public String getConfigString() {
		return "MOVE_DISTANCE";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.MoveDistance");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {
		if(mtmMob==null) {
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(p,"QuestObject.MoveDistance")), isFinished,I18n.locMsg(p, method.getPath()) ,Double.toString(amount));
		}else {
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(p,"QuestObject.MoveDistance")), isFinished,I18n.locMsg(p, "QuestObject.MoveByMythicMob",QuestChatManager.translateColor(customName)) ,Double.toString(amount));
		}
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "method":{
			try {
				method = VehicleDefault.valueOf(obj);
			}catch(IllegalArgumentException e) {
				return false;
			}
			return true;
		}
		case "mtmmobid":{
			mtmMob = obj;
			if(!QuestValidater.validateMythicMob(obj)) {
				QuestChatManager.error(sender, I18n.locMsg(null,"Cmdlog.MTMMobNotFound", obj));
				return false;
			}
			customName = Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mtmMob);
			return true;
		}
		case "vehiclecustomname":{
			customName = QuestChatManager.translateColor(obj);
			return true;
		}
		default :{
			return super.receiveCommandInput(sender, type, obj);
		}
		}
		
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj = null;
		switch(type) {
		case "method":{
			eobj = new EditorListenerObject(ListeningType.STRING,command,null,(args)->{
				try{
					VehicleDefault.valueOf(args);
				}catch(IllegalArgumentException e) {
					return false;
				}
				return true;
			});
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterTraverseEnum"));
			break;
		}
		case "mtmmobid":{
			eobj = new EditorListenerObject(ListeningType.MTMMOB_LEFT_CLICK, command, null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterMobID"));
			break;
		}
		case "vehiclecustomname":{
			eobj = new EditorListenerObject(ListeningType.STRING, command, null);
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterMobVehicleName"));
			break;
		}
		default:{
			return super.createCommandOutput(sender, command, type);
		}
		}
		return eobj;
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(p, "QuestEditor.MoveType")+I18n.locMsg(p,method.getPath()));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " method"))
		.changeLine();
		if(method.equals(VehicleDefault.MYTHIC_MOB)) {
			page.add(I18n.locMsg(p,"QuestEditor.MythicMobs"));
			if(!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")&& Main.getHooker().getMythicMobsAPI().isMythicMobPresent(mtmMob)) {
				page.add(new InteractiveText(p,Main.getHooker().getMythicMobsAPI().hasDisplayName(mtmMob)?Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mtmMob) + "(" + mtmMob + ")":Main.getHooker().getMythicMobsAPI().getMythicInternalName(mtmMob)));
			}else {
				page.add(I18n.locMsg(null,"QuestEditor.NotSet"));
			}
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " mtmmobid"))
			.changeLine();
		}else {
			page.add(I18n.locMsg(p, "QuestEditor.MoveDistance.VehicleCustomName"));
			page.add(this.customName);
			page.changeLine();
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " vehiclecustomname"));
			page.changeLine();
		}
		
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		if(config.getString(path+"Method").equals(VehicleDefault.MYTHIC_MOB.name())) {
			this.method = VehicleDefault.MYTHIC_MOB;
			if(!Main.getHooker().hasMythicMobEnabled()) {		
				return false;
				//String mythicMobEntity = config.getString(path+"MythicMob");
				//if(mythicMobEntity.contains("baby")) {
				//	mythicMobEntity.replace("baby_","");
				//}
				//if(config.getString(path+"customName")!=null) {
				//	customName = config.getString(path+"customName");
				//}
				//this.genericMobType = EntityType.valueOf(mythicMobEntity.toUpperCase());
				//QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null, "Cmdlog.MTMNotInstalled"));				
				//return true;//not gonna just disable the load just because mythic mob is disabled
			}
			String mythicMobEntity = config.getString(path+"MythicMob");
			//isBaby = config.getBoolean(path+"mythicBaby");
			if(mythicMobEntity.contains("baby")) {
				mythicMobEntity.replace("baby_","");
				isBaby = true;
			}
			this.mtmMob = mythicMobEntity;
			this.customName=Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mtmMob).toString();
			
		}else {
			if(config.getString(path+"customName")!=null) {
				customName = config.getString(path+"customName");
			}
			String methodRaw = config.getString(path+"Method");
			this.method = VehicleDefault.valueOfSave(methodRaw);
		}
		
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		if(!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")&& Main.getHooker().getMythicMobsAPI().isMythicMobPresent(mtmMob)) {			
			config.set(objpath + "MythicMob", mtmMob);			
		}else {
			config.set(objpath+"Method",method.name());
			if(customName!=null && customName.length()>0) {
				config.set(objpath+"customName", customName);
			}
		}

		super.save(config, objpath);		
	}
	public boolean isBaby() {
		return isBaby;
	}
	public void setBaby(boolean isBaby) {
		this.isBaby = isBaby;
	}
	public EntityType getGenericMobType() {
		return genericMobType;
	}
	public void setGenericMobType(EntityType genericMobType) {
		this.genericMobType = genericMobType;
	}
	public EntityType getType() {
		return type;
	}
	public void setType(EntityType type) {
		this.type = type;
	}
	public String getCustomName() {
		return customName;
	}
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getMtmMob() {
		return mtmMob;
	}
	public void setMtmMob(String mtmMob) {
		this.mtmMob = mtmMob;
	}

}
