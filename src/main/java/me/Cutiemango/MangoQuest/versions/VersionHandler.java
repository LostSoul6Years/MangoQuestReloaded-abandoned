package me.Cutiemango.MangoQuest.versions;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionHandler
{
	void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle);

	void openBook(Player p, TextComponent... texts);

	TextComponent textFactoryConvertLocation(Player p,String name, Location loc, boolean isFinished);

	TextComponent textFactoryConvertItem(Player p,final ItemStack item, boolean f);

	String getItemTypeTranslationKey(Material material);
	
	boolean hasTag(Player p, String s);
	
	ItemStack addGUITag(ItemStack is);
	
	boolean hasGUITag(ItemStack is);
	
	void playNPCEffect(Player p, Location location);
}
