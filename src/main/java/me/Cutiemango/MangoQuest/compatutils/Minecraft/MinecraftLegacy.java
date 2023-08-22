package me.Cutiemango.MangoQuest.compatutils.Minecraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class MinecraftLegacy {

	public MinecraftLegacy() {
		
	}
	public static Material getMaterial(String value) {
		return Material.valueOf(value);
	}
	public static ItemStack getColor(Material mat, int amount, byte legacyData) {
		return new ItemStack(mat,amount,legacyData);
	}
	public static ItemStack getItemInMainHand(Player p) {
		return p.getInventory().getItemInHand();
	}
	public static void setItemInMainHand(Player p,ItemStack item) {
		p.setItemInHand(item);
	}
	public static String hexColor(String idk) {
		return idk;
	}
	public static boolean isColor(char c) {
		String sth = "0123456789AaBbCcDdEeFf";
		for(char color:sth.toCharArray()) {
			if(color == c) {
				return true;
			}
		}
		return false;
	}
	public static short getDurability(ItemStack item) {
		return item.getDurability();
	}
	public static PlayerFishEvent.State getMidAir(){
		return PlayerFishEvent.State.FISHING;
	}
	public static EntityType getEntityType(String legacy) {
		// TODO Auto-generated method stub
		return EntityType.valueOf(legacy);
	}
}
