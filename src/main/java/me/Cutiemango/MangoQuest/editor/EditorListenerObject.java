package me.Cutiemango.MangoQuest.editor;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;

public class EditorListenerObject
{
	
	public EditorListenerObject(ListeningType ltype, String cmd, Syntax s)
	{
		type = ltype;

		// Prevents "/" in the command.
		command = cmd.replaceFirst("/", "");
		syntax = s;
	}
	public EditorListenerObject(ListeningType ltype,String cmd,Syntax s,Predicate<String> extraChecks) {
		this.extraChecks=extraChecks;
		type = ltype;

		// Prevents "/" in the command.
		command = cmd.replaceFirst("/", "");
		syntax = s;
	}
	private Predicate<String> extraChecks;
	private final Syntax syntax;
	private final ListeningType type;
	// No "/" needed
	private final String command;

	public void execute(Player p, String obj)
	{
		
		if (type == ListeningType.OPEN_INVENTORY)
			return;
		if (syntax != null && !syntax.matches(p, obj))
		{			
			EditorListenerHandler.unregister(p);
			p.sendMessage(QuestChatManager.translateColor(I18n.locMsg(p,"SyntaxError.IllegalArgument")));
			return;
		}
		if(extraChecks != null) {
			if(!extraChecks.test(obj)) {
				p.sendMessage(QuestChatManager.translateColor(I18n.locMsg(p,"SyntaxError.IllegalArgument")));
				return;
			}
		}
		DebugHandler.log(5, "bro lmao i love hacking "+ command + " " + obj);
		QuestUtil.executeSyncCommand(p, command + " " + obj);
		EditorListenerHandler.currentListening.remove(p.getName());
	}

	public ListeningType getType()
	{
		return type;
	}

	public String getCommand()
	{
		return command;
	}

	public enum ListeningType
	{
		STRING,

		LOCATION,
		NPC_LEFT_CLICK,
		MOB_LEFT_CLICK,
		MTMMOB_LEFT_CLICK,
		ITEM,
		OPEN_INVENTORY,
		CRAFTING_INVENTORY,
		BLOCK,

	}
}
