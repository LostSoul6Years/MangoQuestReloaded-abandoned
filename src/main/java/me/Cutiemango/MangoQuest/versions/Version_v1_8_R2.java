package me.Cutiemango.MangoQuest.versions;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.IChatBaseComponent;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.Packet;
import net.minecraft.server.v1_8_R2.PacketDataSerializer;
import net.minecraft.server.v1_8_R2.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles;

public class Version_v1_8_R2 implements VersionHandler {
	private static final Pattern materialPattern = Pattern.compile("\"text\":\"transmat:(.+)\"");
	private static final Pattern entityPattern = Pattern.compile("\"text\":\"transentity:(.+)\"");
	public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		if (title == null)
			title = "";
		if (subtitle == null)
			subtitle = "";
		PacketPlayOutTitle ppot = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
				IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + QuestChatManager.translateColor(title) + "\"}"),
				fadeIn.intValue(), stay.intValue(), fadeOut.intValue());
		(((CraftPlayer) p).getHandle()).playerConnection.sendPacket((Packet) ppot);
		PacketPlayOutTitle subppot = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
				IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + QuestChatManager.translateColor(subtitle) + "\"}"),
				fadeIn.intValue(), stay.intValue(), fadeOut.intValue());
		(((CraftPlayer) p).getHandle()).playerConnection.sendPacket((Packet) subppot);
	}
	public String getItemTypeTranslationKey(Material material) {
        if (material == null) return null;
        net.minecraft.server.v1_8_R2.Item nmsItem = org.bukkit.craftbukkit.v1_8_R2.util.CraftMagicNumbers.getItem(material);
        if (nmsItem == null) return null;
        return nmsItem.getName();
    }
	public void openBook(Player p, TextComponent... texts) {
		ArrayList<BaseComponent[]> list = (ArrayList) new ArrayList<>();
		byte b;
		int i;
		TextComponent[] arrayOfTextComponent;
		for (i = (arrayOfTextComponent = texts).length, b = 0; b < i;) {
			TextComponent t = arrayOfTextComponent[b];
			list.add(new BaseComponent[] { (BaseComponent) t });
			b++;
		}
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		CraftMetaBook meta = addComponentPages((CraftMetaBook) book.getItemMeta(),
				texts);
		book.setItemMeta((ItemMeta) meta);
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			ByteBuf buf = Unpooled.buffer(256);
			buf.setByte(0, (byte) 0);
			buf.writerIndex(1);

			PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen",
					new PacketDataSerializer(buf));
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
			p.getInventory().setItem(slot, old);

		}, 2L);

		

	}
	private CraftMetaBook addComponentPages(CraftMetaBook meta, BaseComponent... texts) {
		CraftMetaBook target = meta.clone();
		if (target.pages.size() >= 50) {
			return target;
		}
		for(BaseComponent bc:texts) {
			if(bc == null) {
				bc = new TextComponent("");
			}
			target.pages.add(IChatBaseComponent.ChatSerializer.a((String) ComponentSerializer.toString((BaseComponent) bc)));
		}
		return target;
	}

	public TextComponent textFactoryConvertLocation(Player p, String name, Location loc, boolean isFinished) {
		TextComponent t = new TextComponent();
		ItemStack is = new ItemStack(Main.getInstance().mcCompat.getCompatMaterial("SIGN", "OAK_SIGN"));
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		if (loc != null)
			im.setLore(
					QuestUtil.createList(new String[] { I18n.locMsg(p, "QuestJourney.NPCLocDisplay",
							new String[] { loc.getWorld().getName(), Double.toString(Math.floor(loc.getX())),
									Double.toString(Math.floor(loc.getY())),
									Double.toString(Math.floor(loc.getZ())) }) }));
		is.setItemMeta(im);
		if (isFinished) {
			t = new TextComponent(QuestChatManager.finishedObjectFormat(name));
		} else {
			t = new TextComponent(name);
		}
		net.minecraft.server.v1_8_R2.ItemStack i = CraftItemStack.asNMSCopy(is);
		NBTTagCompound tag = i.save(new NBTTagCompound());
		String itemJson = tag.toString();
		BaseComponent[] hoverEventComponents = { (BaseComponent) new TextComponent(itemJson) };
		t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
		return t;
	}

	public TextComponent textFactoryConvertItem(Player p,ItemStack it, boolean f) {
		TextComponent itemname = new TextComponent();
		ItemStack is = it.clone();
		if (!is.getItemMeta().hasDisplayName()) {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + QuestUtil.translate(p,is.getType()));
			is.setItemMeta(im);
			if (f) {
				itemname = new TextComponent(QuestChatManager.finishedObjectFormat(QuestUtil.translate(p,is.getType())));
			} else {
				itemname = new TextComponent(ChatColor.BLACK + QuestUtil.translate(p,is.getType()));
			}
		} else if (f) {
			itemname = new TextComponent(QuestChatManager.finishedObjectFormat(QuestUtil.translate(p,is.getType())));
		} else {
			itemname = new TextComponent(is.getItemMeta().getDisplayName());
		}
		net.minecraft.server.v1_8_R2.ItemStack i = CraftItemStack.asNMSCopy(is);
		NBTTagCompound tag = i.save(new NBTTagCompound());
	    ItemStack item = it;
		String itemTag = tag.toString();
		itemTag.replace("\"text\":\""+QuestUtil.translate(p,item)+"\""+QuestUtil.translate(p,item)+"","\"translate\":\""+getItemTypeTranslationKey(item.getType())+"\"");
		
		//itemTag.replace("");
		BaseComponent[] hoverEventComponents = new BaseComponent[] {
				new TextComponent(itemTag) // The only element of the hover
																					// events basecomponents is the item
																					// json
		};
		itemname.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
		return itemname;
	}

	public boolean hasTag(Player p, String s) {
		return false;
	}

	public ItemStack addGUITag(ItemStack item) {
		net.minecraft.server.v1_8_R2.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound stag = nmscopy.hasTag() ? nmscopy.getTag() : new NBTTagCompound();
		stag.setBoolean("GUIitem", true);
		nmscopy.setTag(stag);
		return CraftItemStack.asBukkitCopy(nmscopy);
	}

	public boolean hasGUITag(ItemStack item) {
		net.minecraft.server.v1_8_R2.ItemStack nmscopy = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nmscopy.hasTag() ? nmscopy.getTag() : new NBTTagCompound();
		return tag.hasKey("GUIitem");
	}

	public void playNPCEffect(Player p, Location location) {
		location.setY(location.getY() + 2.0D);
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.NOTE, false,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(), 0.0F, 0.0F, 0.0F, 1.0F, 1,
				null);
		(((CraftPlayer) p).getHandle()).playerConnection.sendPacket((Packet) packet);
	}
}
