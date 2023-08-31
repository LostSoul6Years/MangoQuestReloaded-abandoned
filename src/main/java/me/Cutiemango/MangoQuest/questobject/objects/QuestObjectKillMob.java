package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectKillMob extends NumerableObject implements EditorObject
{
	public QuestObjectKillMob() {
	}

	public QuestObjectKillMob(EntityType t, int i, String customname) {
		type = t;
		amount = i;
		customName = customname;
	}

	public QuestObjectKillMob(String mmMob, int i) {
		mtmMob = mmMob;
		amount = i;
		type = EntityType.valueOf(Main.getHooker().getMythicMobsAPI().getMythicEntityTypeRaw(mmMob).toUpperCase());
		customName = Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mmMob);
	}

	@Override
	public String getConfigString() {
		return "KILL_MOB";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.KillMob");
	}


	private boolean isBaby = false;
	private EntityType type = EntityType.SHEEP;
	private String rawType = "SHEEP";
	private String customName;
	private String mtmMob;//internal name

	public EntityType getType() {
		return type;
	}

	public boolean hasCustomName() {
		return !(customName == null);
	}

	public String getCustomName() {
		return customName;
	}

	public String getMythicMob() {
		return mtmMob;
	}

	public void setCustomName(String s) {
		customName = QuestChatManager.translateColor(s);
	}

	public boolean isBaby() {
		return isBaby;
	}

	public void setMythicMob(String m) {
		if(!Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ||!Main.getHooker().getMythicMobsAPI().isMythicMobPresent(m))
			return;
		mtmMob = m;
		customName = Main.getHooker().getMythicMobsAPI().hasDisplayName(m)?Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(m):null;
		if (Main.getHooker().getMythicMobsAPI().getMythicEntityTypeRaw(m)!=null && Main.getHooker().getMythicMobsAPI().getMythicEntityTypeRaw(m).contains("BABY"))
			isBaby = true;
		type = Main.getHooker().getMythicMobsAPI().getMythicEntityTypeRaw(m)!=null ? EntityType.valueOf(Main.getHooker().getMythicMobsAPI().getMythicEntityTypeRaw(m).replace("BABY_", "").toUpperCase()):null;
	}

	public void setType(EntityType t) {
		type = t;
	}

	public boolean isMythicObject() {
		return Bukkit.getPluginManager().isPluginEnabled("MythicMobs")&& Main.getHooker().getMythicMobsAPI().isMythicMobPresent(mtmMob);
	}

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		
		if (hasCustomName())
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.KillMob")), isFinished, amount, customName);
		else
			return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.KillMob")), isFinished, amount, type == null ? rawType : type);
		
		
		
	}

	@Override
public String toDisplayText(Player p) {
		if (hasCustomName())
			return I18n.locMsg(p,"QuestObject.KillMob", Integer.toString(amount), QuestChatManager.translateColor(customName));
		else
			return I18n.locMsg(p,"QuestObject.KillMob", Integer.toString(amount), mtmMob != null && hasCustomName() ? getCustomName() : type != null ? QuestUtil.translate(p, type) : rawType);
	}
	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		//page.add(new InteractiveText(p,Main.getHooker().getMythicMobsAPI().hasDisplayName(mtmMob)?Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mtmMob) + "(" + mtmMob + ")":Main.getHooker().getMythicMobsAPI().getInternalName()));
		if (Main.getHooker().hasMythicMobEnabled()) {
			page.add(I18n.locMsg(null,"QuestEditor.MythicMobs"));
			if (mtmMob != null && isMythicObject())
				page.add(new InteractiveText(p,Main.getHooker().getMythicMobsAPI().hasDisplayName(mtmMob)?Main.getHooker().getMythicMobsAPI().getMythicPlaceholderedName(mtmMob) + "(" + mtmMob + ")":Main.getHooker().getMythicMobsAPI().getMythicInternalName(mtmMob)));
			else
				page.add(I18n.locMsg(null,"QuestEditor.NotSet"));
			page.changeLine();
			page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " mtmmob"))
					.changeLine();
		}
		page.add(I18n.locMsg(null,"QuestEditor.MobName"));
		if (hasCustomName())
			page.add(new InteractiveText(p,customName));
		else
			page.add(I18n.locMsg(null,"QuestEditor.NotSet"));
		page.changeLine();
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " mobname"))
				.changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.MobType") + (type == null ? "N/A" : QuestUtil.translate(p, type)));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " mobtype"))
				.changeLine();
		super.formatEditorPage(p,page, stage, obj);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		if (config.getString(path + "MythicMob") != null) {
			
			if (!Main.getHooker().hasMythicMobEnabled()) {
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"Cmdlog.MTMNotInstalled"));
				return false;
			}
			String id = config.getString(path + "MythicMob");
			if (!QuestValidater.validateMythicMob(id)) {
				QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.MTMMobNotFound", id));
				return false;
			}
			mtmMob = id;
			customName = config.getString(path+"MobName");
			String typeName = config.getString(path+"MobType");
			if (typeName != null && typeName.contains("BABY")) {
				isBaby = true;
				typeName = typeName.replace("BABY_", "");
			}
			type = EntityType.valueOf(typeName);
			rawType = config.getString(path + "MobType");
		} else if (config.getString(path + "MobName") != null) {
			customName = config.getString(path + "MobName");
			type = EntityType.valueOf(config.getString(path + "MobType"));
			rawType = config.getString(path + "MobType");
		} else if (config.getString(path + "MobType") != null) {
			rawType = config.getString(path + "MobType");
			type = EntityType.valueOf(config.getString(path + "MobType"));
		}
		else
			return false;
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		if (isMythicObject()) {
			config.set(objpath + "MythicMob", mtmMob);
			if(type != null) {
				config.set(objpath + "MobType", type.toString());
			}
			if (hasCustomName())
				config.set(objpath + "MobName", customName);
		}
		else {
			config.set(objpath + "MobType", type.toString());
			if (hasCustomName())
				config.set(objpath + "MobName", customName);
		}
		super.save(config, objpath);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch (type) {
			case "mobname":
				setCustomName(obj);
				DebugHandler.log(5,"setting object custom name: "+obj);
				break;
			case "mobtype":
				setType(EntityType.valueOf(obj));
				break;
			case "mtmmob":
				if (!QuestValidater.validateMythicMob(obj)) {
					QuestChatManager.error(sender, I18n.locMsg(null,"Cmdlog.MTMMobNotFound", obj));
					return false;
				}
				setMythicMob(obj);
				
				break;
			default:
				return super.receiveCommandInput(sender, type, obj);
		}
		return true;
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj;
		switch (type) {
			case "mobtype":
				obj = new EditorListenerObject(ListeningType.MOB_LEFT_CLICK, command, null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.HitMob"));
				break;
			case "mtmmob":
				obj = new EditorListenerObject(ListeningType.MTMMOB_LEFT_CLICK, command, null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterMobID"));
				break;
			case "mobname":
				obj = new EditorListenerObject(ListeningType.STRING, command, null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.EnterMobName"));
				break;
			default:
				return super.createCommandOutput(sender, command, type);
		}
		return obj;
	}

}
