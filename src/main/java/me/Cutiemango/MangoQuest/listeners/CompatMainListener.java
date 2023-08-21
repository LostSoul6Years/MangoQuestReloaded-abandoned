package me.Cutiemango.MangoQuest.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;

public class CompatMainListener implements Listener{
	//>legacy
		@EventHandler(priority = EventPriority.LOWEST)
		public void onPlayerSwim(EntityToggleSwimEvent e) {
			if(Main.lockDown.get()) {
				return;
			}
			if(e.getEntityType() == EntityType.PLAYER) {
				QuestPlayerData qd = QuestUtil.getData((Player)e.getEntity());
				if (qd == null) {
					DebugHandler.log(4, "[Listener] Player " + ((Player)e.getEntity()).getName() + " has no player data.");
					return;
				}
				qd.setSwimming(e.isSwimming());
			}
		}

		@EventHandler(priority=EventPriority.LOWEST)
		public void onMobBreed(EntityBreedEvent e) {
			if(Main.lockDown.get()) {
				return;
			}
			if(e.getBreeder() instanceof Player) {
				Player p = (Player) e.getBreeder();
				QuestPlayerData pd = QuestUtil.getData(p);
				if(pd == null) {
					DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
					return;
				}
				pd.breedMob(e.getFather(),e.getMother());
			}
		}

}
