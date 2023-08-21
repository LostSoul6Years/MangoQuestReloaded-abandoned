package me.Cutiemango.MangoQuest.versions;

import io.netty.buffer.Unpooled;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_13_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R2.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_13_R2.Particles;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version_v1_13_R2 implements VersionHandler
{
	private static final Pattern materialPattern = Pattern.compile("\"text\":\"transmat:(.+)\"");
	private static final Pattern entityPattern = Pattern.compile("\"text\":\"transentity:(.+)\"");
	@Override
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle)
	{
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + QuestChatManager.translateColor(title == null ? "" : title) + "\"}"), fadeIn, stay, fadeOut));
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + QuestChatManager.translateColor(subtitle == null ? "" : subtitle) + "\"}"), fadeIn, stay, fadeOut));
	}
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.server.v1_13_R2.Item nmsItem = org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers.getItem(material);
        if (nmsItem == null) return null;
        return nmsItem.getName();
    }
	@Override
	public void openBook(Player p, TextComponent... texts)
	{
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		CraftMetaBook meta = (CraftMetaBook)book.getItemMeta();
		try {
			Field f = null;
			f =  CraftMetaBook.class.getDeclaredField("pages");
			f.setAccessible(true);
			
			ParameterizedType listType = (ParameterizedType) f.getGenericType();
			Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
			if(listClass.getName().contains("Component")||listClass.getName().contains("Chat")) {
				List<IChatBaseComponent> pages = (List<IChatBaseComponent>) f.get(meta);
				if(pages == null) {
					f.set(meta, new ArrayList<IChatBaseComponent>());
				}
				pages = (List<IChatBaseComponent>)f.get(meta);
				for(TextComponent tc:texts) {
					String str = ComponentSerializer.toString(tc);
					Matcher materialMatcher = materialPattern.matcher(str);
					while(materialMatcher.find()) {
						String translateStr = str.substring(materialMatcher.start(),materialMatcher.end());
						str = str.replace(translateStr, "\"translate\":\""+getItemTypeTranslationKey(Material.valueOf(materialMatcher.group(1)))+"\"");
						materialMatcher = materialPattern.matcher(str);
						
					}
					
					Matcher entityMatcher = entityPattern.matcher(str);
					while(entityMatcher.find()) {
						String translateStr = str.substring(entityMatcher.start(),entityMatcher.end());
						
						str = str.replace(translateStr, "\"translate\":\""+getItemTypeTranslationKey(Material.valueOf(entityMatcher.group(1)))+"\"");
						entityMatcher = entityPattern.matcher(str);
						
					}
					pages.add(ChatSerializer.a(str));
				}
			}else if(listClass.getName().contains("String")) {
				List<String> pages =(List<String>)f.get(meta);
				if(pages == null) {
					f.set(meta, new ArrayList<String>());
				}
				pages = (List<String>) f.get(meta);
				for(TextComponent tc:texts) {
					String str = ComponentSerializer.toString(tc);
					Matcher materialMatcher = materialPattern.matcher(str);
					while(materialMatcher.find()) {
						String translateStr = str.substring(materialMatcher.start(),materialMatcher.end());
						str = str.replace(translateStr, "\"translate\":\""+getItemTypeTranslationKey(Material.valueOf(materialMatcher.group(1)))+"\"");
						materialMatcher = materialPattern.matcher(str);
						
					}
					
					Matcher entityMatcher = entityPattern.matcher(str);
					while(entityMatcher.find()) {
						String translateStr = str.substring(entityMatcher.start(),entityMatcher.end());
						
						str = str.replace(translateStr, "\"translate\":\""+getItemTypeTranslationKey(Material.valueOf(entityMatcher.group(1)))+"\"");
						entityMatcher = entityPattern.matcher(str);
						
					}
					pages.add(str);
				}
			}
		}catch(ClassCastException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		meta.setAuthor("MangoQuest");
		meta.setTitle("MangoQuest");
		book.setItemMeta(meta);
		
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);
		PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
		packetdataserializer.a(EnumHand.MAIN_HAND);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload(new MinecraftKey("minecraft", "book_open"), packetdataserializer));
		p.getInventory().setItem(slot, old);
	}

	@Override
	public TextComponent textFactoryConvertLocation(Player p,String name, Location loc, boolean isFinished)
	{
		if (loc == null)
			return new TextComponent("");

		ItemStack is = new ItemStack(Material.PAINTING);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);

		String displayMsg = I18n.locMsg(p,"QuestJourney.NPCLocDisplay",
				loc.getWorld().getName(),
				Integer.toString(loc.getBlockX()),
				Integer.toString(loc.getBlockY()),
				Integer.toString(loc.getBlockZ()));

		im.setLore(QuestUtil.createList(displayMsg));

		is.setItemMeta(im);
		TextComponent text = new TextComponent(isFinished ? QuestChatManager.finishedObjectFormat(name) : name);

		net.minecraft.server.v1_13_R2.ItemStack i = CraftItemStack.asNMSCopy(is);
		NBTTagCompound tag = i.save(new NBTTagCompound());
		String itemJson = tag.toString();

		BaseComponent[] hoverEventComponents = new BaseComponent[]
		{ new TextComponent(itemJson) };
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));

		return text;
	}

	
	@Override
	public TextComponent textFactoryConvertItem(ItemStack item, boolean finished)
	{
		String displayText = QuestUtil.translate(item);

		if (finished)
			displayText = QuestChatManager.finishedObjectFormat(displayText);
		else
			displayText = ChatColor.BLACK + displayText;

		TextComponent text = new TextComponent(displayText);
		if (item != null)
		{
			NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
			if (tag == null)
				return text;
			String itemTag = tag.toString();
			itemTag.replace("\"text\":\""+QuestUtil.translate(item)+"\""+QuestUtil.translate(item)+"","\"translate\":\""+getItemTypeTranslationKey(item.getType())+"\"");
			
			//itemTag.replace("");
			BaseComponent[] hoverEventComponents = new BaseComponent[] {
					new TextComponent(itemTag) // The only element of the hover
																						// events basecomponents is the item
																						// json
			};
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
		}
		return text;
	}

	@Override
	public boolean hasTag(Player p, String s)
	{
		return ((CraftPlayer) p).getHandle().getScoreboardTags().contains(s);
	}

	@Override
	public ItemStack addGUITag(ItemStack item)
	{
		net.minecraft.server.v1_13_R2.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound stag = (nmscopy.hasTag()) ? nmscopy.getTag() : new NBTTagCompound();
		stag.setBoolean("GUIitem", true);
		nmscopy.setTag(stag);
		return CraftItemStack.asBukkitCopy(nmscopy);
	}

	@Override
	public boolean hasGUITag(ItemStack item)
	{
		net.minecraft.server.v1_13_R2.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = (nmscopy.hasTag()) ? nmscopy.getTag() : new NBTTagCompound();
		return tag.hasKey("GUIitem");
	}
	
	@Override
	public void playNPCEffect(Player p, Location location)
	{
		location.setY(location.getY() + 2);
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(Particles.I, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 1, 1);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

}
