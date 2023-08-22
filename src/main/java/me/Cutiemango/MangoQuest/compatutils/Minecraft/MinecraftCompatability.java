package me.Cutiemango.MangoQuest.compatutils.Minecraft;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.Main;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.BaseComponent;

public class MinecraftCompatability {
	private boolean isLegacy = false;
	private boolean is1_8 = false;

	
	public MinecraftCompatability() {
		this.isLegacy = ConfigSettings.LEGACY_MC;
		this.is1_8 = ConfigSettings.MC_1_8;
	}
	public int getCompatMaxLen() {
		if(isLegacy) {
			return 16;
		}else {
			return 40;
		}
	}
	public int getDurability(ItemStack item) {
		if(isLegacy) {
			return MinecraftLegacy.getDurability(item);
		}else {
			return MinecraftNew.getDurability(item);
		}
	}
	
	public Material getCompatMaterial(String legacy,String newOne) {
		if(isLegacy) {
			return MinecraftLegacy.getMaterial(legacy);
		}else {
			return MinecraftNew.getMaterial(newOne);
		}
	}
	public EntityType getCompatEntityType(String legacy,String newOne) {
		if(isLegacy) {
			return MinecraftLegacy.getEntityType(legacy);
		}else {
			return MinecraftNew.getEntityType(newOne);
		}
	}
	public ItemStack getColouredItem(Material mat,int amount, byte legacyData) {
		if(isLegacy) {
			return MinecraftLegacy.getColor(mat, amount, legacyData);
		}else {
			return MinecraftNew.getColor(mat, amount);
		}
	}
	
	public String hexColor(String rough) {
		
		if(Main.getInstance().hexColorSupport) {
			return MinecraftNew.hexColor(rough);
		}else {
			return rough;
		}
		
	}
	public boolean isColor(char c) {
		if(Main.getInstance().hexColorSupport) {
			return MinecraftNew.isColor(c);
		}else {
			return MinecraftLegacy.isColor(c);
		}
	}
	
	public ItemStack getCompatColouredItem(String legacy,String newOne ,int amount, byte legacyData) {
		if(isLegacy) {
			return MinecraftLegacy.getColor(getCompatMaterial(legacy,newOne), amount, legacyData);
		}else {
			return MinecraftNew.getColor(getCompatMaterial(legacy,newOne), amount);
		}
	}
	public ItemStack getItemInMainHand(Player p) {
		if(is1_8) {
			return MinecraftLegacy.getItemInMainHand(p);
		}else {
			return MinecraftNew.getItemInMainHand(p);
		}
	}
	public PlayerFishEvent.State getMidAirState(){
		if(isLegacy) {
			return MinecraftLegacy.getMidAir();
		}else {
			return MinecraftNew.getMidAir();
		}
	}

	public static boolean isUnbreakable(ItemMeta meta) {
		if (ConfigSettings.LEGACY_MC) {
			Method spigot;
			try {
				spigot = meta.getClass().getDeclaredMethod("spigot");
				spigot.setAccessible(true);
				Object spigotInternal = spigot.invoke(meta);
				Method isUnbreakable = spigotInternal.getClass().getDeclaredMethod("isUnbreakable");
				isUnbreakable.setAccessible(true);
				boolean answer = (boolean) isUnbreakable.invoke(spigotInternal);
				return answer;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			// Class<?> spigotClass =
			// Class.forName("org.bukkit.inventory.meta.ItemMeta.Spigot");
			return false;
		} else {
			try {
				Method isUnbreakable = meta.getClass().getDeclaredMethod("isUnbreakable");
				isUnbreakable.setAccessible(true);
				boolean answer = (boolean) isUnbreakable.invoke(meta);
				return answer;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return false;
			// return meta.isUnbreakable();
		}
	}
	public ItemStack getItemInOffHand(Player p) {
		if(is1_8) {
			return null;
		}else {
			return MinecraftNew.getItemInOffHand(p);
		}
	}
	public void setItemInMainHand(Player p,ItemStack item) {
		if(is1_8) {
			MinecraftLegacy.setItemInMainHand(p, item);
		}else {
			MinecraftNew.setItemInMainHand(p, item);
		}
	}

	public boolean runStupidOffHandCheck(PlayerInteractEntityEvent e) {
		if(is1_8) {
			return false;
		}else {
			return MinecraftNew.stupidOffHandCheck(e);
		}
	}

	public static String join(Iterable<String> pieces, String separator) {
		StringBuilder buffer = new StringBuilder();
		for (Iterator<String> iter = pieces.iterator(); iter.hasNext();) {
			buffer.append(iter.next());
			if (iter.hasNext())
				buffer.append(separator);
		}
		return buffer.toString();
	}
	public enum FormatRetention {
		NONE, FORMATTING, EVENTS, ALL;
	}

	public static BaseComponent copyFormatting(BaseComponent toBeActed, BaseComponent component) {
		return copyFormatting(toBeActed, component, FormatRetention.ALL, true);
	}

	public static BaseComponent copyFormatting(BaseComponent toBeActed, BaseComponent component, boolean replace) {
		return copyFormatting(toBeActed, component, FormatRetention.ALL, replace);
	}

	public static BaseComponent copyFormatting(BaseComponent toBeActed, BaseComponent component,
			FormatRetention retention, boolean replace) {
		if (retention == FormatRetention.EVENTS || retention == FormatRetention.ALL) {
			if (replace || toBeActed.getClickEvent() == null)
				toBeActed.setClickEvent(component.getClickEvent());
			if (replace || toBeActed.getHoverEvent() == null)
				toBeActed.setHoverEvent(component.getHoverEvent());
		}
		if (retention == FormatRetention.FORMATTING
				|| retention == FormatRetention.ALL) {
			if (replace || toBeActed.getColorRaw() == null)
				toBeActed.setColor(component.getColorRaw());
			if (replace || toBeActed.isBoldRaw() == null)
				toBeActed.setBold(component.isBoldRaw() == null ?false:component.isBoldRaw());
			if (replace || toBeActed.isItalicRaw() == null)
				toBeActed.setItalic(component.isItalicRaw() == null ?false:component.isItalicRaw());
			if (replace || toBeActed.isUnderlinedRaw() == null)
				toBeActed.setUnderlined(component.isUnderlinedRaw() == null ?false:component.isUnderlinedRaw());
			if (replace || toBeActed.isStrikethroughRaw() == null)
				toBeActed.setStrikethrough(component.isStrikethroughRaw() == null ?false:component.isStrikethroughRaw());
			if (replace || toBeActed.isObfuscatedRaw() == null)
				toBeActed.setObfuscated(component.isObfuscatedRaw() == null ?false:component.isObfuscatedRaw());
			if (!ConfigSettings.LEGACY_MC) {
				Method getInsertionActed;
				Object getInsertionActedObj = null;
				Object getInsertionComponentObj = null;
				try {
					getInsertionActed = toBeActed.getClass().getDeclaredMethod("getInsertion");
					getInsertionActed.setAccessible(true);
					getInsertionActedObj = getInsertionActed.invoke(toBeActed);
					Method getInsertionComponent = component.getClass().getDeclaredMethod("getInsertion");
					getInsertionComponent.setAccessible(true);
					getInsertionComponentObj = getInsertionComponent.invoke(component);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (replace || getInsertionActedObj == null) {
					// Method getInsertion = component.getClass().getDeclaredMethod("getInsertion");
					// getInsertion.setAccessible(true);
					Method setInsertion;
					try {
						if(getInsertionComponentObj == null) {
							return toBeActed;
						}
						setInsertion = toBeActed.getClass().getDeclaredMethod("setInsertion", String.class);
						setInsertion.setAccessible(true);
						setInsertion.invoke(toBeActed, getInsertionComponentObj);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// setInsertion(component.getInsertion());
				}
			}
		}
		return toBeActed;
	}
	
	public static String getName(NPC npc) {
		try {
			Class<?> clazz = npc.getClass();
			//DebugHandler.log(4, Arrays.toString(clazz.getDeclaredMethods()));
			Method m = clazz.getMethod("getFullName");
			//m.setAccessible(true);
			String name = (String) m.invoke(npc);
			return name;
		}catch(NoSuchMethodException e) {
			DebugHandler.log(4, "dude stop 1");			
			return npc.getName();
		}catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
