package me.Cutiemango.MangoQuest.compatutils.Minecraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.md_5.bungee.api.ChatColor;

public class MinecraftNew {

	public static final Pattern hex = Pattern.compile("\\<#[a-fA-F0-9]{6}\\>");
	public static Material getMaterial(String value) {
		return Material.valueOf(value);
	}
	public static ItemStack getColor(Material mat, int amount) {
		return new ItemStack(mat,amount);
	}
	public static ItemStack getItemInMainHand(Player p) {
		return p.getInventory().getItemInMainHand();
	}
	public static void setItemInMainHand(Player p, ItemStack item) {
		p.getInventory().setItemInMainHand(item);
	}
	public static String hexColor(String rough) {
		Matcher finder = hex.matcher(rough);
		while(finder.find()) {
	      String coloured = rough.substring(finder.start(),finder.end());
	      String hexColor = rough.substring(finder.start()+1,finder.end()-1);
	      rough = rough.replace(coloured, ChatColor.of(hexColor)+"");
	      finder = hex.matcher(rough);
		}
		return rough;
	}
	public static boolean isColor(char c) {
		if(ChatColor.getByChar(c).getColor() != null) {
			return true;
		}
		return false;
	}
	public static boolean stupidOffHandCheck(PlayerInteractEntityEvent e) {
		return e.getHand().equals(EquipmentSlot.OFF_HAND);
	}
	public static int getDurability(ItemStack newStack) {
		return newStack.getType().getMaxDurability() - ((Damageable) newStack.getItemMeta()).getDamage();
	}
	public static EntityType getEntityType(String newOne) {
		return EntityType.valueOf(newOne);
	}
	public static PlayerFishEvent.State getMidAir(){
		return PlayerFishEvent.State.REEL_IN;
	}
	public static ItemStack getItemInOffHand(Player p) {
		return p.getInventory().getItemInOffHand();
	}
}
