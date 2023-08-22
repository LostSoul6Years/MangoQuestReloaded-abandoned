package me.Cutiemango.MangoQuest.questobject.interfaces;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;

public interface EditorObject
{
	boolean receiveCommandInput(Player sender, String type, String obj);
	EditorListenerObject createCommandOutput(Player sender, String command, String type);
	void formatEditorPage(Player p, FlexibleBook page, int stage, int obj);
}
