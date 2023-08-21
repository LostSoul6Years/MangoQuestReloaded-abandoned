package me.Cutiemango.MangoQuest.versions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.versions.utils.StupidReflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
public class Version_v1_18_R1 implements VersionHandler {
	private static final Pattern materialPattern = Pattern.compile("\"text\":\"transmat:(.+)\"");
	private static final Pattern entityPattern = Pattern.compile("\"text\":\"transentity:(.+)\"");
	//itemstack getNBTTag from 1.18:s becomes 1.18.2 t
	@Override
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		CraftPlayer cp = (CraftPlayer) p;
		ClientboundSetTitlesAnimationPacket times = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
		
		StupidReflection.callMethodWithObjectArgs(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").get(),"a",times);
		if (title != null) {
			ClientboundSetTitleTextPacket packetTitle = new ClientboundSetTitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(title))[0]);
			StupidReflection.callMethodWithObjectArgs(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").get(),"a",packetTitle);
		}
		if (subtitle != null) {
			ClientboundSetSubtitleTextPacket packetSubtitle = new ClientboundSetSubtitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(subtitle))[0]);
			StupidReflection.callMethodWithObjectArgs(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").get(),"a",packetSubtitle);
		}
	}
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.world.item.Item nmsItem = CraftMagicNumbers.getItem(material);
        if (nmsItem == null) return null;
        return nmsItem.toString();
    }
	@Override
	public void openBook(Player p, TextComponent... texts) {
		
	
		
		
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
		StupidReflection.callMethodWithObjectArgs(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").get(), "a", (Packet<?>)new PacketPlayOutOpenBook(EnumHand.a));
		
		p.getInventory().setItem(slot, old);
		
	}

	@Override
	public net.md_5.bungee.api.chat.TextComponent textFactoryConvertLocation(Player p,String name, Location loc, boolean isFinished) {
		if (loc == null)
			return new TextComponent("");

		ItemStack is = new ItemStack(Material.PAINTING);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);

		String displayMsg = I18n.locMsg(p,"QuestJourney.NPCLocDisplay", loc.getWorld().getName(),
				Integer.toString(loc.getBlockX()), Integer.toString(loc.getBlockY()),
				Integer.toString(loc.getBlockZ()));

		im.setLore(QuestUtil.createList(displayMsg));

		is.setItemMeta(im);
		TextComponent text = new TextComponent(isFinished ? QuestChatManager.finishedObjectFormat(name) : name);
		NBTTagCompound tag = new NBTTagCompound();
		StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(is),"b",tag);
		//ItemTag itemTag = ItemTag.ofNbt(CraftItemStack.asNMSCopy(is).getTag().asString());
		BaseComponent[] hoverEventComponents = new BaseComponent[] {
				new TextComponent(tag.toString()) // The only element of the hover
																					// events basecomponents is the item
																					// json
		};
		
	    //net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
	    
		//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(name,1,ItemTag.ofNbt(nmsItem.s().toString()))));
	    //text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,new Item(is.getData()., 1, ItemTag.ofNbt(nmsItem.s().toString()))));
	    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
		return text;
	}

	
	@Override
	public TextComponent textFactoryConvertItem(final ItemStack item, boolean finished) {
		
		String displayText = QuestUtil.translate(item);

		if (finished)
			displayText = QuestChatManager.finishedObjectFormat(displayText);
		else
			displayText = ChatColor.BLACK + displayText;

		TextComponent text = new TextComponent(displayText);
		if (item != null) {
			NBTTagCompound tag = new NBTTagCompound();
			//b same
			StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(item),"b",tag);
			
			
			//from s to u in 1.18.2
			Method s = null;
			try {
				s = CraftItemStack.asNMSCopy(item).getClass().getDeclaredMethod("s");
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
				return null;
			}
			s.setAccessible(true);
			try {
				if (s.invoke(CraftItemStack.asNMSCopy(item)) == null&& false) {
					//displayText = ChatColor.WHITE + ChatColor.stripColor(displayText);
					//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(displayText)));
					return text;
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String itemTag = tag.toString();
			itemTag.replace("\"text\":\""+QuestUtil.translate(item)+"\""+QuestUtil.translate(item)+"","\"translate\":\""+getItemTypeTranslationKey(item.getType())+"\"");
			
			//itemTag.replace("");
			BaseComponent[] hoverEventComponents = new BaseComponent[] {
					new TextComponent(itemTag) // The only element of the hover
																						// events basecomponents is the item
																						// json
			};


			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
			//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hoverEventComponents));
		}
		return text;
	}

	//useless function
	@Override
	public boolean hasTag(Player p, String s) {
		return false;
	}

	@Override
	public ItemStack addGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		//this.u is the tag
		//r becomes s in 1.18.2                       //s to u in 1.18.2
		Method s = null;
		NBTTagCompound sed  = null;
		try {
			s = net.minecraft.world.item.ItemStack.class.getDeclaredMethod("s");
			sed = (NBTTagCompound) s.invoke(nmscopy);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		s.setAccessible(false);

		Method nmsr = null;
		boolean rresult;
		try {
			nmsr = nmscopy.getClass().getDeclaredMethod("r");
			nmsr.setAccessible(true);
			rresult = (boolean) nmsr.invoke(nmscopy);
		}catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		NBTTagCompound stag = rresult ? sed : new NBTTagCompound();
		//setboolean = a(string x,boolean y)
		//same
		StupidReflection.callMethodWithObjectArgs(stag,"a","GUIitem",true);
		//c becomes a
		StupidReflection.callMethodWithObjectArgs(nmscopy,"c",stag);
		return CraftItemStack.asBukkitCopy(nmscopy);
	}

	@Override
	public boolean hasGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack  nmscopy = CraftItemStack.asNMSCopy(item);
		Method s = null;
		NBTTagCompound sed  = null;
		try {
			s = net.minecraft.world.item.ItemStack.class.getDeclaredMethod("s");
			sed = (NBTTagCompound) s.invoke(nmscopy);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		s.setAccessible(false);
		Method nmsr = null;
		boolean rresult;
		try {
			nmsr = nmscopy.getClass().getDeclaredMethod("r");
			nmsr.setAccessible(true);
			rresult = (boolean) nmsr.invoke(nmscopy);
		}catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		NBTTagCompound tag = rresult ? sed : new NBTTagCompound();
		//same
		
		return (boolean)StupidReflection.callMethodWithObjectArgs(tag,"e","GUIitem").get();
	}

	@Override
	public void playNPCEffect(Player p, Location location) {
		location.setY(location.getY() + 2);
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(Particles.Q, false,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 1, 1);
		//same
		StupidReflection.callMethodWithObjectArgs(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").get(),"a",packet);
	}
}
