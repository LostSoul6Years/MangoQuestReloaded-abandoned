package me.Cutiemango.MangoQuest.versions;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.versions.utils.StupidReflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.world.EnumHand;

public class Version_v1_20_R1 implements VersionHandler{
	private static final Pattern materialPattern = Pattern.compile("\"text\":\"transmat:(.+)\"");
	private static final Pattern entityPattern = Pattern.compile("\"text\":\"transentity:(.+)\"");
	@Override
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		CraftPlayer cp = (CraftPlayer) p;
		ClientboundSetTitlesAnimationPacket times = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
		
		StupidReflection.callMethodWithObjectArgs(cp.getHandle().c,"a",(Packet<?>)times);
		if (title != null) {
			ClientboundSetTitleTextPacket packetTitle = new ClientboundSetTitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(title))[0]);
			StupidReflection.callMethodWithObjectArgs(cp.getHandle().c,"a",(Packet<?>)packetTitle);
		}
		if (subtitle != null) {
			ClientboundSetSubtitleTextPacket packetSubtitle = new ClientboundSetSubtitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(subtitle))[0]);
			StupidReflection.callMethodWithObjectArgs(cp.getHandle().c,"a",(Packet<?>)packetSubtitle);
		}
	}

	@Override
	public void openBook(Player p, TextComponent... texts) {
		//Bukkit.broadcastMessage(ComponentSerializer.toString(texts));
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		CraftMetaBook meta = (CraftMetaBook) book.getItemMeta();

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
					/*Bukkit.broadcastMessage("entry");
					Bukkit.broadcastMessage(str);
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
						
					}*/
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
					/*Bukkit.broadcastMessage("entry");
					Bukkit.broadcastMessage(str);
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
						
					}*/
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
		StupidReflection.callMethodWithObjectArgs(((CraftPlayer)p).getHandle().c, "a", (Packet<?>)new PacketPlayOutOpenBook(EnumHand.a));
		p.getInventory().setItem(slot, old);
		
	}

	@Override
	public net.md_5.bungee.api.chat.TextComponent textFactoryConvertLocation(Player p,String name, Location loc, boolean isFinished) {
		if (loc == null)
			return new TextComponent("");

		ItemStack is = new ItemStack(Material.PAINTING);
		ItemMeta im = is.getItemMeta();
		DebugHandler.log(4, "raw npc displayname");
		DebugHandler.log(4, name);
		im.setDisplayName(name);

		String displayMsg = I18n.locMsg(p,"QuestJourney.NPCLocDisplay", loc.getWorld().getName(),
				Integer.toString(loc.getBlockX()), Integer.toString(loc.getBlockY()),
				Integer.toString(loc.getBlockZ()));
		DebugHandler.log(4, "after loc msg");
		DebugHandler.log(4, displayMsg);
		im.setLore(QuestUtil.createList(displayMsg));

		is.setItemMeta(im);
		TextComponent text = new TextComponent(isFinished ? QuestChatManager.finishedObjectFormat(name) : name);
		DebugHandler.log(4, "textcomponent message");
		DebugHandler.log(4, ComponentSerializer.toString(text));
		NBTTagCompound tag = (NBTTagCompound)StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(is),"v").get();
		//b should be the same
		
		//ItemTag itemTag = ItemTag.ofNbt(CraftItemStack.asNMSCopy(is).getTag().asString());
		//BaseComponent[] hoverEventComponents = new BaseComponent[] {	
		//		new TextComponent(tag.toString()) // The only element of the hover
																					// events basecomponents is the item
																					// json
		//};
		
		//DebugHandler.log(2,tag.toString());
	    //net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
	    
		//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(name,1,ItemTag.ofNbt(nmsItem.s().toString()))));
	    //text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,new Item(is.getData()., 1, ItemTag.ofNbt(nmsItem.s().toString()))));
	    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item("minecraft:oak_planks", 1, ItemTag.ofNbt(tag.toString()))));
		return text;
	}
	
	
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.world.item.Item nmsItem = CraftMagicNumbers.getItem(material);
        if (nmsItem == null) return null;
        return (String)StupidReflection.callMethodWithObjectArgs(nmsItem,"a").get();
    }

	
	@Override
	public TextComponent textFactoryConvertItem(final ItemStack item, boolean finished) {
		
		String displayText = QuestUtil.translate(item);

		if (finished)
			displayText = QuestChatManager.finishedObjectFormat(displayText);
		else
			displayText = ChatColor.BLACK + displayText;

		TextComponent text = new TextComponent(displayText);
		DebugHandler.log(1,text.toString());
		if (item != null) {
			NBTTagCompound tag = new NBTTagCompound();
			//b same
			StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(item),"c",tag);
			
			//DebugHandler.log(5,"created tag");
			
			//from s to u in 1.18.2
			if (StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(item),"v").isEmpty() && false) {
				//displayText = ChatColor.WHITE + ChatColor.stripColor(displayText);
				//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(displayText)));
				
				return text;
			}
			String itemTag = tag.toString();
			
			//itemTag.replace("\"text\":\""+QuestUtil.translate(item)+"\""+QuestUtil.translate(item)+"","\"translate\":\""+getItemTypeTranslationKey(item.getType())+"\"");
			
			//itemTag.replace("");
			BaseComponent[] hoverEventComponents = new BaseComponent[] {
					new TextComponent(itemTag) // The only element of the hover
																						// events basecomponents is the item
																						// json
			};


			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(item.getType().getKey().toString(), 1, ItemTag.ofNbt(tag.toString()))));
			DebugHandler.log(2, "pleasework: "+item.getType().getKey().toString());
			//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hoverEventComponents));
		}
		return text;
	}

	//useless function
	@Override
	public boolean hasTag(Player p, String s) {
		return false;
		//return ((CraftPlayer) p).getHandle().af().contains(s);
	}

	@Override
	public ItemStack addGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		//this.u is the tag
		//r becomes s in 1.18.2                       //s to u in 1.18.2
		NBTTagCompound stag = (!StupidReflection.callMethodWithObjectArgs(nmscopy,"v").isEmpty())? ((NBTTagCompound)StupidReflection.callMethodWithObjectArgs(nmscopy,"v").get()): new NBTTagCompound();
		//setboolean = a(string x,boolean y)
		//same
		StupidReflection.callMethodWithObjectArgs(stag,"a","GUIitem",true);
		
		//DebugHandler.log(2, "pleasework tag just after added:"+(StupidReflection.callMethodWithObjectArgs(stag,"c","GUIitem").isPresent()));
		//c becomes a
		//net.minecraft.world.item.ItemStack nmscopy1 = net.minecraft.world.item.ItemStack.a(stag);
		StupidReflection.callMethodWithObjectArgs(nmscopy,"c",stag);
		
		return CraftItemStack.asBukkitCopy(nmscopy);
	}
	
	

	@Override
	public boolean hasGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack  nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = (!StupidReflection.callMethodWithObjectArgs(nmscopy,"v").isEmpty())? ((NBTTagCompound)StupidReflection.callMethodWithObjectArgs(nmscopy,"v").get()): new NBTTagCompound();
		//DebugHandler.log(2, "pleasework tag:"+(StupidReflection.callMethodWithObjectArgs(tag,"c","GUIitem").isPresent()));
		//same
		return StupidReflection.callMethodWithObjectArgs(tag,"c","GUIitem").isPresent();
	}	

	@Override
	public void playNPCEffect(Player p, Location location) {
		location.setY(location.getY() + 2);
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(Particles.V, false,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 1, 1);
		//same
		//DebugHandler.log(5, "sent particle packet");
		StupidReflection.callMethodWithObjectArgs(((CraftPlayer)p).getHandle().c,"a",packet);
	}
}
