package me.Cutiemango.MangoQuest.compatutils.MythicMob;

import org.bukkit.entity.Entity;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class MythicMobLegacy {
	
	public static MythicMob getMythicMob(String id) {
		return MythicMobs.inst().getAPIHelper().getMythicMob(id);
	}
	public static String getMythicMobPlaceholderedName(String id) {
		return MythicMobs.inst().getAPIHelper().getMythicMob(id).getDisplayName().get();
	}
	public static boolean isMythicMob(Entity e) {
		return MythicMobs.inst().getAPIHelper().isMythicMob(e);
	}
	public static ActiveMob getMythicMobInstance(Entity e) {

		return MythicMobs.inst().getAPIHelper().getMythicMobInstance(e);
	}
	public static String getEntityType(String id) {
		return getMythicMob(id).getEntityType();
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
