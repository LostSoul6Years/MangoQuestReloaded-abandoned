package me.Cutiemango.MangoQuest.compatutils.MythicMob;

import org.bukkit.entity.Entity;

import me.Cutiemango.MangoQuest.Main;

public class MythicMobCompatability {
	private boolean useLegacy = true;
	public MythicMobCompatability() {
		useLegacy = Main.getHooker().isMmIsLegacy();
	}
	public boolean isMythicMobPresent(String id) {
		if(id == null) {
			return false;
		}
		if(useLegacy) {			
			return MythicMobLegacy.getMythicMob(id)!=null;
		}else {
			return MythicMobNew.getMythicMob(id)!=null;
		}
	}
	public boolean isEntityMythic(Entity e) {
		if(e == null) {
			return false;
		}
		if(useLegacy) {
			return MythicMobLegacy.isMythicMob(e);
		}else {
			return MythicMobNew.isMythicMob(e);
		}
	}
	public String getMythicInternalName(Entity e) {
		if(useLegacy) {
			return MythicMobLegacy.getInternalNameFromMythicInstance(e);
		}else {
			return MythicMobNew.getInternalNameFromMythicInstance(e);
		}
	}
	public String getMythicEntityTypeRaw(String id) {
		if(useLegacy) {
			return MythicMobLegacy.getEntityType(id);
		}else {
			return MythicMobNew.getEntityType(id);
		}
	}
	public String getMythicPlaceholderedName(String id) {
		if(useLegacy) {
			return MythicMobLegacy.getMythicMobPlaceholderedName(id);
		}else {
			return MythicMobNew.getMythicMobPlaceholderedName(id);
		}
	}
	public String getMythicInternalName(String mtmMob) {
		if(useLegacy) {
			return MythicMobLegacy.getInternalNameFromId(mtmMob);
		}else {
			return MythicMobNew.getInternalNameFromId(mtmMob);
		}
	}
	
	public boolean hasDisplayName(String internal) {
		if(useLegacy) {
			//return MythicMobLegacy.getInternalNameFromId(mtmMob);
			return MythicMobLegacy.hasDisplayName(internal);
		}else {
			//return MythicMobNew.getInternalNameFromId(mtmMob);
			return MythicMobNew.hasDisplayName(internal);
		}
	}
	
	
	
}
