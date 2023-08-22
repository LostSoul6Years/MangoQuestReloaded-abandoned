package me.Cutiemango.MangoQuest.versions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.versions.utils.StupidReflection;
import me.Cutiemango.MangoQuest.versions.utils.StupidReflection.AccessLevel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.EnumHand;

public class Version_LTS implements VersionHandler{

	@Override
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		p.sendTitle(title, subtitle,fadeIn,stay,fadeOut);
		
	}

	@Override
	public void openBook(Player p, TextComponent... texts) {
		
		//Bukkit.broadcastMessage(ComponentSerializer.toString(texts));
				ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
				ItemMeta meta = book.getItemMeta();
				
				

				try {
					
					Field f = null;

					String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
					Class<?> metaClass = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftMetaBook");
					f =  metaClass.getDeclaredField("pages");
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
					
					metaClass.getDeclaredMethod("setAuthor", String.class).invoke(meta, "MangoQuest");
					metaClass.getDeclaredMethod("setTitle", String.class).invoke(meta, "MangoQuest");
					book.setItemMeta(meta);
					int slot = p.getInventory().getHeldItemSlot();
					ItemStack old = p.getInventory().getItem(slot);
					p.getInventory().setItem(slot, book);
					Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit."+version+".entity.CraftPlayer");
					StupidReflection.callMethodWithMethodDeclaration(StupidReflection.getFieldWithTypeAndAccess(craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(p)), PlayerConnection.class, AccessLevel.PUBLIC), AccessLevel.PUBLIC, Void.TYPE, new PacketPlayOutOpenBook(EnumHand.a));
					//StupidReflection.callMethodWithObjectArgs(((CraftPlayer)p).getHandle().c, "a", (Packet<?>)new PacketPlayOutOpenBook(EnumHand.a));
					p.getInventory().setItem(slot, old);
					//meta.setAuthor("MangoQuest");
					//meta.setTitle("MangoQuest");
					//book.setItemMeta(meta);
				}catch(ClassCastException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				//int slot = p.getInventory().getHeldItemSlot();
				//ItemStack old = p.getInventory().getItem(slot);
				//p.getInventory().setItem(slot, book);
				//StupidReflection.callMethodWithObjectArgs(((CraftPlayer)p).getHandle().c, "a", (Packet<?>)new PacketPlayOutOpenBook(EnumHand.a));
				//p.getInventory().setItem(slot, old);
				
		
		
	}

	@Override
	public TextComponent textFactoryConvertLocation(Player p, String name, Location loc, boolean isFinished) {
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
		//NBTTagCompound tag = (NBTTagCompound)StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(is),"v").get();
		
		NBTTagCompound tag;
		
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
			Class<?> craftStackClass = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftItemStack");
			
			
			
			tag = (NBTTagCompound) StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, is), AccessLevel.PUBLIC, NBTTagCompound.class).get();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
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

	@Override
	public TextComponent textFactoryConvertItem(ItemStack item, boolean finished) {

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
			//StupidReflection.callMethodWithObjectArgs(CraftItemStack.asNMSCopy(item),"c",tag);
			try {
				String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
				Class<?> craftStackClass = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftItemStack");
				
				
				
				StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item), AccessLevel.PUBLIC, Void.TYPE,tag);
				if (StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item), AccessLevel.PUBLIC,NBTTagCompound.class).isEmpty() && false) {
					//displayText = ChatColor.WHITE + ChatColor.stripColor(displayText);
					//text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(displayText)));
					
					return text;
				}
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}
			
			
			
			
			//DebugHandler.log(5,"created tag");
			
			//from s to u in 1.18.2
			
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

	@Override
	
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.world.item.Item nmsItem = null;
        try {
        	String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
			Class<?> magicClass = Class.forName("org.bukkit.craftbukkit."+version+".util.CraftMagicNumbers");
			
        	nmsItem = (net.minecraft.world.item.Item) magicClass.getDeclaredMethod("getItem", ItemStack.class).invoke(null, material);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        if (nmsItem == null) return null;
        return (String)StupidReflection.callMethodWithMethodDeclaration(nmsItem,AccessLevel.PUBLIC,String.class).get();
        
    }

	@Override
	public boolean hasTag(Player p, String s) {
		return false;
	}

	@Override
	public ItemStack addGUITag(ItemStack item) {
		
		net.minecraft.world.item.ItemStack nmscopy = null;
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
			Class<?> craftStackClass = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftItemStack");
			
			nmscopy = (net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			
			//this.u is the tag
			//r becomes s in 1.18.2                       //s to u in 1.18.2
			NBTTagCompound stag = (!StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item), AccessLevel.PUBLIC, NBTTagCompound.class).isEmpty())? ((NBTTagCompound) StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item), AccessLevel.PUBLIC, NBTTagCompound.class).get()): new NBTTagCompound();
			//setboolean = a(string x,boolean y)
			//same
			
			//StupidReflection.callMethodWithObjectArgs(stag,"a","GUIitem",true);
			StupidReflection.callMethodWithMethodDeclaration(stag,AccessLevel.PUBLIC, Void.TYPE,"GUIitem",true);
			
			//DebugHandler.log(2, "pleasework tag just after added:"+(StupidReflection.callMethodWithObjectArgs(stag,"c","GUIitem").isPresent()));
			//c becomes a
			//net.minecraft.world.item.ItemStack nmscopy1 = net.minecraft.world.item.ItemStack.a(stag);
			//StupidReflection.callMethodWithObjectArgs(nmscopy,"c",stag);
			StupidReflection.callMethodWithMethodDeclaration(nmscopy,AccessLevel.PUBLIC,Void.TYPE,stag);
			
			//return CraftItemStack.asBukkitCopy(nmscopy);
			return (ItemStack) craftStackClass.getDeclaredMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class).invoke(null, nmscopy);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public boolean hasGUITag(ItemStack is) {
		net.minecraft.world.item.ItemStack nmscopy = null;
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
			Class<?> craftStackClass = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftItemStack");
			
			nmscopy = (net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, is);
			
			NBTTagCompound tag = (!StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, is), AccessLevel.PUBLIC, NBTTagCompound.class).isEmpty())? ((NBTTagCompound) StupidReflection.callMethodWithMethodDeclaration((net.minecraft.world.item.ItemStack)craftStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, is), AccessLevel.PUBLIC, NBTTagCompound.class).get()): new NBTTagCompound();
			//DebugHandler.log(2, "pleasework tag:"+(StupidReflection.callMethodWithObjectArgs(tag,"c","GUIitem").isPresent()));
			//same
			//return StupidReflection.callMethodWithObjectArgs(tag,"c","GUIitem").isPresent();
			//DebugHandler.log(3,""+!StupidReflection.callMethodWithMethodDeclaration(tag, AccessLevel.PUBLIC, net.minecraft.nbt.NBTBase.class, "GUIitem").isEmpty());
			
			return !StupidReflection.callMethodWithMethodDeclaration(tag, AccessLevel.PUBLIC, net.minecraft.nbt.NBTBase.class, "GUIitem").isEmpty();
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void playNPCEffect(Player p, Location location) {
		location.setY(location.getY() + 2);
		try {
			//CraftParticle
			String version = Bukkit.getServer().getClass().getPackage().getName(); version = version.substring(version.lastIndexOf('.') + 1);
			Class<?> craftStackClass = Class.forName("org.bukkit.craftbukkit."+version+".CraftParticle");
			
			
			PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(((ParticleParam)craftStackClass.getDeclaredMethod("toNMS", Particle.class).invoke(null, Particle.NOTE)), false,
					(float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 1, 1);
			//same
			//DebugHandler.log(5, "sent particle packet");
			
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit."+version+".entity.CraftPlayer");
			
			
			StupidReflection.callMethodWithMethodDeclaration(StupidReflection.getFieldWithTypeAndAccess(craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(p)), PlayerConnection.class, AccessLevel.PUBLIC), AccessLevel.PUBLIC, Void.TYPE, packet);
			
			//StupidReflection.callMethodWithObjectArgs(((CraftPlayer)p).getHandle().c,"a",packet);
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	
		
	}

}
