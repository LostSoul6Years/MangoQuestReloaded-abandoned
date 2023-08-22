package me.Cutiemango.MangoQuest.questobject;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.TextComponentFactory;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.conversation.QuestConversation;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public abstract class SimpleQuestObject
{
	public static HashMap<String, String> ALL_OBJECTS = new HashMap<>();

	public static void initObjectNames(Player p) {
		ALL_OBJECTS.put("BREAK_BLOCK", I18n.locMsg(p,"QuestObjectName.BreakBlock"));
		ALL_OBJECTS.put("CONSUME_ITEM", I18n.locMsg(p,"QuestObjectName.ConsumeItem"));
		ALL_OBJECTS.put("DELIVER_ITEM", I18n.locMsg(p,"QuestObjectName.DeliverItem"));
		ALL_OBJECTS.put("KILL_MOB", I18n.locMsg(p,"QuestObjectName.KillMob"));
		ALL_OBJECTS.put("REACH_LOCATION", I18n.locMsg(p,"QuestObjectName.ReachLocation"));
		ALL_OBJECTS.put("TALK_TO_NPC", I18n.locMsg(p,"QuestObjectName.TalkToNPC"));
		ALL_OBJECTS.put("FISHING", I18n.locMsg(p,"QuestObjectName.Fishing"));
		ALL_OBJECTS.put("LOGIN_SERVER", I18n.locMsg(p,"QuestObjectName.LoginServer"));
		ALL_OBJECTS.put("SHEAR_SHEEP",I18n.locMsg(p, "QuestObjectName.ShearSheep"));
		ALL_OBJECTS.put("REGENERATION", I18n.locMsg(p, "QuestObjectName.Regeneration"));
		ALL_OBJECTS.put("MOVE_DISTANCE", I18n.locMsg(p, "QuestObjectName.MoveDistance"));
		ALL_OBJECTS.put("PLAYER_CHAT",I18n.locMsg(p, "QuestObjectName.PlayerChat"));
		ALL_OBJECTS.put("ENTER_COMMAND",I18n.locMsg(p, "QuestObjectName.EnterCommand"));
		ALL_OBJECTS.put("LAUNCH_PROJECTILE", I18n.locMsg(p,"QuestObjectName.LaunchProjectile"));
		ALL_OBJECTS.put("BUCKET_FILL", I18n.locMsg(p, "QuestObjectName.Fill"));
		ALL_OBJECTS.put("ENCHANT_ITEM",I18n.locMsg(p, "QuestObjectName.EnchantItem"));
		ALL_OBJECTS.put("TAME_MOB",I18n.locMsg(p,"QuestObjectName.TameMob"));
		ALL_OBJECTS.put("SLEEP",I18n.locMsg(p,"QuestObjectName.Sleep"));
		ALL_OBJECTS.put("BREED_MOB", I18n.locMsg(p, "QuestObjectName.BreedMob"));
		ALL_OBJECTS.put("USE_ANVIL", I18n.locMsg(p, "QuestObjectName.UseAnvil"));
		ALL_OBJECTS.put("CRAFT_ITEM",I18n.locMsg(p, "QuestObjectName.CraftItem"));
		ALL_OBJECTS.put("PLACEHOLDER_API", I18n.locMsg(p, "QuestObjectName.PlaceholderAPI"));		
	}

	
	protected TextComponent toTextComponent(Player p,String s, boolean isFinished, Object... args) {
		TextComponent text = new TextComponent("");
		s = s.replace("[", "").replace("]", "");
		String left = s;
		String color = QuestChatManager.translateColor("&0");

		Material block = null;
		if (isFinished)
			color = QuestChatManager.translateColor("&8&m&o");
		for (int i = 0; i < args.length; i++) {
			String[] split = left.split("%" + i);
			if (split.length != 0) {
				if (split[0].equals(left)) {
					text.addExtra(color + left);
					return text;
				}
				if (split.length >= 2)
					left = split[1];
				else
					left = "";
				text.addExtra(color + split[0]);
			} else
				left = "";
			if (args[i] == null) {
				continue;
			}
			if (args[i] instanceof ItemStack)
				text.addExtra(TextComponentFactory.convertItemHoverEvent((ItemStack) args[i], isFinished));
			else if (args[i] instanceof Integer)
				text.addExtra(color + args[i]);
			else if (args[i] instanceof NPC) {
				NPC npc = (NPC) args[i];
				if (npc == null || npc.getEntity() == null)
					text.addExtra(I18n.locMsg(null,"Translation.UnknownNPC"));
				else
					
					text.addExtra(TextComponentFactory.convertLocHoverEvent(p,me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), npc.getEntity().getLocation(), isFinished));
			} else if (args[i] instanceof Material) {
		 		block = (Material) args[i];
	      	}else {
				// QuestObjectReachLocation
				if (args[i] instanceof String) {
					if (args.length - 1 > i && args[i + 1] instanceof Location) {
						text.addExtra(TextComponentFactory.convertLocHoverEvent(p,(String) args[i], (Location) args[i + 1], isFinished));
						left = left.replace("%" + (i + 1), "");
					} else {
						if (isFinished)
							text.addExtra(color + args[i]);
						else
							text.addExtra(color + QuestChatManager.translateColor((String) args[i]));
					}
				}
				// QuestObjectKillMob
				else if (args[i] instanceof EntityType) {
					String translation = QuestUtil.translate((EntityType)args[i]);
					if(translation.contains("transentity")) {
						TranslatableComponent entityTranslation = new TranslatableComponent("entity.minecraft."+((EntityType)args[i]).name().toLowerCase());
						//entityTranslation.setColor(ChatColor.BLACK);
						entityTranslation.addWith(new TranslatableComponent("entity.minecraft."+((EntityType)args[i]).name().toLowerCase()));
						text.addExtra(entityTranslation);
					}else {
						text.addExtra(QuestUtil.translate((EntityType) args[i]));
					}
					
				}
			}
		if (block != null) {
			String translation = QuestUtil.translate(block);
			if(!translation.contains("transmaterial")) {
				text.addExtra(color +translation );
			}else {
				TranslatableComponent materialTranslation = new TranslatableComponent(Main.getInstance().handler.getItemTypeTranslationKey(block));
				
				//materialTranslation.addWith("lolasdassdaa");
				//materialTranslation.addWith(Main.getInstance().handler.getItemTypeTranslationKey(block));
				//materialTranslation.setColor(ChatColor.BLACK);
				
				
				text.addExtra(materialTranslation);
				//DebugHandler.log(5, ComponentSerializer.toString(text));
				//for(Player player:Bukkit.getOnlinePlayers()) {
					///player.spigot().sendMessage(materialTranslation);
				//}
			}
			
		}
		}
		text.addExtra(color + left);
		return text;
	}


	protected TextComponent toTextComponent(boolean autoSpacing, Player p,String s, boolean isFinished, Object... args) {
		TextComponent text = new TextComponent("");
		s = s.replace("[", "").replace("]", "");
		String left = s;
		String color = QuestChatManager.translateColor("&0");

		Material block = null;
		if (isFinished)
			color = QuestChatManager.translateColor("&8&m&o");
		for (int i = 0; i < args.length; i++) {
			String[] split = left.split("%" + i);
			if (split.length != 0) {
				if (split[0].equals(left)) {
					text.addExtra(color + left);
					return text;
				}
				if (split.length >= 2)
					left = split[1];
				else
					left = "";
				text.addExtra(color + split[0]);
			} else
				left = "";
			if (args[i] == null) {
				continue;
			}
			if(autoSpacing) {
				text.addExtra(" ");
			}
			if (args[i] instanceof ItemStack) {
				DebugHandler.log(5,"Recognized Itemstack in textcomponent quest shown");
				text.addExtra(TextComponentFactory.convertItemHoverEvent((ItemStack) args[i], isFinished));
				}
			else if (args[i] instanceof Integer)
				text.addExtra(color + args[i]);
			else if (args[i] instanceof NPC) {
				NPC npc = (NPC) args[i];
				if (npc == null || npc.getEntity() == null)
					text.addExtra(I18n.locMsg(null,"Translation.UnknownNPC"));
				else
					text.addExtra(TextComponentFactory.convertLocHoverEvent(p,me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), npc.getEntity().getLocation(), isFinished));
			} else if (args[i] instanceof Material)
				block = (Material) args[i];
			else
				// QuestObjectReachLocation
				if (args[i] instanceof String) {
					if (args.length - 1 > i && args[i + 1] instanceof Location) {
						text.addExtra(TextComponentFactory.convertLocHoverEvent(p,(String) args[i], (Location) args[i + 1], isFinished));
						left = left.replace("%" + (i + 1), "");
					} else {
						if (isFinished)
							text.addExtra(color + args[i]);
						else
							text.addExtra(color + QuestChatManager.translateColor((String) args[i]));
					}
				}
				// QuestObjectKillMob
				else if (args[i] instanceof EntityType)
					text.addExtra(QuestUtil.translate((EntityType) args[i]));
		}		
		if (block != null)
			text.addExtra(color + QuestUtil.translate(block));
		text.addExtra(color + left);
		
		return text;
	}
	
	
	public abstract String toDisplayText(Player p);

	public abstract boolean load(QuestIO config, String path);

	public abstract void save(QuestIO config, String objpath);

	public abstract String getConfigString();

	public abstract String getObjectName();

	

	protected String activateConversation = null;

	public String toPlainText() {
		return ChatColor.stripColor(toDisplayText(null));
	}

	public QuestConversation getConversation() {
		return ConversationManager.getConversation(activateConversation);
	}

	public boolean hasConversation() {
		return ConversationManager.getConversation(activateConversation) != null;
	}

	public void setConversation(String s) {
		activateConversation = s;
	}

	public abstract TextComponent toTextComponent(Player p, boolean isFinished);

}
