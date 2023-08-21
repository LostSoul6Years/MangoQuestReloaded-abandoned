package me.Cutiemango.MangoQuest.questobject.objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectReachLocation extends SimpleQuestObject implements EditorObject
{
	public QuestObjectReachLocation() {
	}

	public QuestObjectReachLocation(Location l, int i, String s) {
		loc = l;
		radius = i;
		name = QuestChatManager.translateColor(s);
	}

	@Override
	public String getConfigString() {
		return "REACH_LOCATION";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.ReachLocation");
	}

	private Location loc;
	private int radius;
	private String name;

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.ReachLocation")), isFinished, name, loc);
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.ReachLocation", name, "");
	}

	public Location getLocation() {
		return loc;
	}

	public String getName() {
		return name;
	}

	public int getRadius() {
		return radius;
	}

	public void setLocation(Location l) {
		loc = l;
	}

	public void setName(String s) {
		name = s;
	}

	public void setRadius(int i) {
		radius = i;
	}

	@Override
	public void formatEditorPage(Player p,FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null,"QuestEditor.ReachLocation"));
		page.add("(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
		page.changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.ReachRadius") + radius + I18n.locMsg(null,"QuestEditor.WithinBlocks"));
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " loc"));
		page.changeLine();
		page.add(I18n.locMsg(null,"QuestEditor.LocationName") + name);
		page.add(new InteractiveText(p,I18n.locMsg(null,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " locname"));
		page.changeLine();
	}

	@Override
	public boolean load(QuestIO config, String path) {
		String[] splited = config.getString(path + "Location").split(":");
		loc = new Location(Bukkit.getWorld(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]),
				Double.parseDouble(splited[3]));
		radius = config.getInt(path + "Range");
		name = config.getString(path + "LocationName");
		return true;
	}

	@Override
	public void save(QuestIO config, String objpath) {
		String location = loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
		config.set(objpath + "Location", location);
		config.set(objpath + "LocationName", name);
		config.set(objpath + "Range", radius);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch (type) {
			case "loc":
				if (!QuestValidater.validateInteger(obj))
					return false;
				setRadius(Integer.parseInt(obj));
				setLocation(sender.getLocation());
				break;
			case "locname":
				setName(QuestChatManager.translateColor(obj));
				break;
		}
		return true;
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject obj = null;
		switch (type) {
			case "loc":
				obj = new EditorListenerObject(ListeningType.LOCATION, command, Syntax.of("I", I18n.locMsg(null,"Syntax.Number"), ""));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.ReachLocation"));
				break;
			case "locname":
				obj = new EditorListenerObject(ListeningType.STRING, command, null);
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(null,"EditorMessage.LocationName"));
				break;
		}
		return obj;
	}

}
