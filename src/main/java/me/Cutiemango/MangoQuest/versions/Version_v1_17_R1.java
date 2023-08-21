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
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
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

public class Version_v1_17_R1 implements VersionHandler {
	private static final Pattern materialPattern = Pattern.compile("\"text\":\"transmat:(.+)\"");
	private static final Pattern entityPattern = Pattern.compile("\"text\":\"transentity:(.+)\"");
	private static NBTTagCompound dummyKey = new NBTTagCompound();
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.world.item.Item nmsItem = CraftMagicNumbers.getItem(material);
        if (nmsItem == null) return null;
        return nmsItem.toString();
    }
	@Override
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		CraftPlayer cp = (CraftPlayer) p;
		ClientboundSetTitlesAnimationPacket times = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
		Method m = null;
		try {
			m = StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").getClass().getDeclaredMethod("sendPacket", Packet.class);
			m.setAccessible(true);
			m.invoke(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b"), times);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//((Object) StupidReflection.getField(((CraftPlayer)p).getHandle(),"b")).sendPacket(times);
		if (title != null) {
			ClientboundSetTitleTextPacket packetTitle = new ClientboundSetTitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(title))[0]);
			Method m1;
			try {
				m1 = StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").getClass().getDeclaredMethod("sendPacket", Packet.class);
				m1.setAccessible(true);
				m1.invoke(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b"), packetTitle);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//(cp.getHandle()).b.sendPacket(packetTitle);
		}
		if (subtitle != null) {
			ClientboundSetSubtitleTextPacket packetSubtitle = new ClientboundSetSubtitleTextPacket(
					CraftChatMessage.fromString(QuestChatManager.translateColor(subtitle))[0]);
			Method m1;
			try {
				m1 = StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").getClass().getDeclaredMethod("sendPacket", Packet.class);
				m1.setAccessible(true);
				m1.invoke(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b"), packetSubtitle);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//(cp.getHandle()).b.sendPacket(packetSubtitle);
		}
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
		Method m1;
		try {
			m1 = StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").getClass().getDeclaredMethod("sendPacket", Packet.class);
			m1.setAccessible(true);
			m1.invoke(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b"), new PacketPlayOutOpenBook(EnumHand.a));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//((CraftPlayer) p).getHandle().b.sendPacket(new PacketPlayOutOpenBook(EnumHand.a));
		p.getInventory().setItem(slot, old);
	}

	@Override
	public TextComponent textFactoryConvertLocation(Player p,String name, Location loc, boolean isFinished) {
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

		//ItemTag itemTag = ItemTag.ofNbt(CraftItemStack.asNMSCopy(is).getTag().asString());
		NBTTagCompound tag = new NBTTagCompound();
		
		Method m;
		String nametag = null;
		try {
			m = CraftItemStack.asNMSCopy(is).getClass().getDeclaredMethod("save",NBTTagCompound.class);
			m.setAccessible(true);
			tag = (NBTTagCompound)m.invoke(CraftItemStack.asNMSCopy(is), tag);
			Method m1=tag.getClass().getDeclaredMethod("toString");
			m1.setAccessible(true);
			nametag= (String) m1.invoke(tag);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		 
		BaseComponent[] hoverEventComponents = new BaseComponent[] {
				new TextComponent(nametag) };
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
			
			Method m;
			String nametag = null;
			try {
				m = CraftItemStack.asNMSCopy(item).getClass().getDeclaredMethod("save",NBTTagCompound.class);
				m.setAccessible(true);
			    tag = (NBTTagCompound)m.invoke(CraftItemStack.asNMSCopy(item), tag);
				Method m1=tag.getClass().getDeclaredMethod("toString");
				m1.setAccessible(true);
				nametag= (String) m1.invoke(tag);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tag == null)
				return text;
			//ItemTag itemTag = ItemTag.ofNbt(tag.asString());
			String itemTag = nametag;
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
	public boolean hasTag(Player p, String s) {
		//useless function
		return true;
	}

	@Override
	public ItemStack addGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = null;
		try {
			Method hasTag = nmscopy.getClass().getDeclaredMethod("hasTag");
			hasTag.setAccessible(true);
			Method getTag = nmscopy.getClass().getDeclaredMethod("getTag");
			getTag.setAccessible(true);
			//Method hasKey = nmscopy.getClass().getDeclaredMethod("hasKey");
			//hasKey.setAccessible(true);
			Method setTag = nmscopy.getClass().getDeclaredMethod("setTag",NBTTagCompound.class);
			setTag.setAccessible(true);
			
			Method setBoolean = dummyKey.getClass().getDeclaredMethod("setBoolean",String.class,boolean.class);
			setBoolean.setAccessible(true);
		    tag = ((boolean)hasTag.invoke(nmscopy)) ? (NBTTagCompound)getTag.invoke(nmscopy) : new NBTTagCompound();
		    setBoolean.invoke(tag, "GUIitem",true);
		    setTag.invoke(nmscopy,tag);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CraftItemStack.asBukkitCopy(nmscopy);
	}

	@Override
	public boolean hasGUITag(ItemStack item) {
		net.minecraft.world.item.ItemStack  nmscopy = CraftItemStack.asNMSCopy(item);
		Method hasTag;
		boolean idk = false;
		
		try {
			hasTag = nmscopy.getClass().getDeclaredMethod("hasTag");
			hasTag.setAccessible(true);
			Method getTag = nmscopy.getClass().getDeclaredMethod("getTag");
			getTag.setAccessible(true);
			//for(Method fuck:dummyKey.getClass().getMethods()) {
				//Bukkit.getLogger().info("NBTTagCompound method:"+fuck.toGenericString());
			//}
			Method hasKey = dummyKey.getClass().getDeclaredMethod("hasKey",String.class);
			hasKey.setAccessible(true);
			
			NBTTagCompound tag = ((boolean)hasTag.invoke(nmscopy)) ? (NBTTagCompound)getTag.invoke(nmscopy) : new NBTTagCompound();
			idk = (boolean) hasKey.invoke(tag,"GUIitem");
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return idk;
	}

	@Override
	public void playNPCEffect(Player p, Location location) {
		location.setY(location.getY() + 2);
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(Particles.R, false,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 1, 1);
		Method m1;
		try {
			m1 = StupidReflection.getField(((CraftPlayer)p).getHandle(),"b").getClass().getDeclaredMethod("sendPacket", Packet.class);
			m1.setAccessible(true);
			m1.invoke(StupidReflection.getField(((CraftPlayer)p).getHandle(),"b"), packet);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
