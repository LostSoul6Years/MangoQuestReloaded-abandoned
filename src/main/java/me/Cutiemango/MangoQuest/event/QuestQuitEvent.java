package me.Cutiemango.MangoQuest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.Cutiemango.MangoQuest.model.Quest;

public class QuestQuitEvent extends Event{
	public Quest getQ() {
		return q;
	}


	public Player getPlayer() {
		return player;
	}


	private static final HandlerList HANDLERS = new HandlerList();
	private Quest q;
	private Player player;
	
	
	public QuestQuitEvent(Player player,Quest q) {
		this.player = player;
		this.q = q;
	}
	
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return HANDLERS;
	}
	

}
