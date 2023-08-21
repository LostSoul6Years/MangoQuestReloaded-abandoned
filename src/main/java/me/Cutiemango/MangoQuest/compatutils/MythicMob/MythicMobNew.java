package me.Cutiemango.MangoQuest.compatutils.MythicMob;

import org.bukkit.entity.Entity;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;





public class MythicMobNew {
	
	public static MythicMob getMythicMob(String id) {
		return MythicBukkit.inst().getAPIHelper().getMythicMob(id);
	}
	public static String getMythicMobPlaceholderedName(String id) {
		return MythicBukkit.inst().getAPIHelper().getMythicMob(id).getDisplayName().get();
	}
	public static boolean isMythicMob(Entity e) {
		return MythicBukkit.inst().getAPIHelper().isMythicMob(e);
	}
	public static ActiveMob getMythicMobInstance(Entity e) {
		return MythicBukkit.inst().getAPIHelper().getMythicMobInstance(e);
	}
	public static String getEntityType(String id) {
		return getMythicMob(id).getEntityTypeString();
	}
	public static String getInternalNameFromMythicInstance(Entity e) {
		return getMythicMobInstance(e).getType().getInternalName();
	}
	public static String getInternalNameFromId(String id) {
		return getMythicMob(id).getInternalName();
	}
	public static boolean hasDisplayName(String id) {
		return getMythicMob(id).getDisplayName() != null && getMythicMob(id).getDisplayName().get() != null;
	}
}
