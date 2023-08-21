package me.Cutiemango.MangoQuest.editor;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditorListenerHandler
{
	public static HashMap<String, EditorListenerObject> currentListening = new HashMap<>();
	public static  Map<String,Inventory> tempCraftingTables = new HashMap<>();
	public static void onChat(Player p, String msg, AsyncPlayerChatEvent event) {
		if (currentListening.containsKey(p.getName())) {
			EditorListenerObject obj = currentListening.get(p.getName());
			if (msg.contains("cancel"))
				QuestChatManager.info(p, I18n.locMsg(p,"EditorMessage.CancelEntry"));
			else
				QuestChatManager.info(p, I18n.locMsg(null,"EditorMessage.YourEntry", msg));
			obj.execute(p, msg);
			event.setCancelled(true);
		}
	}
	
	@Deprecated
	public static void onPlayerCraft(Player p,CraftingInventory inv) {
		if(currentListening.containsKey(p.getName())) {
			EditorListenerObject obj = currentListening.get(p.getName());
			if(obj.getType() != ListeningType.CRAFTING_INVENTORY) {
				return;
			}
			String claimID = UUID.randomUUID().toString();
			tempCraftingTables.put(claimID, inv);
			obj.execute(p, claimID);
		}
	}

	public static void onPlayerInteract(Player p, Action act, ItemStack is, Cancellable event) {
		if (!preCondition(p))
			return;
		EditorListenerObject obj = currentListening.get(p.getName());
		if (act.equals(Action.RIGHT_CLICK_AIR) || act.equals(Action.RIGHT_CLICK_BLOCK)) {
			if (is != null && !is.getType().equals(Material.AIR)) {
				if (obj.getType().equals(ListeningType.ITEM)) {
					event.setCancelled(true);
					obj.execute(p, "item");
				}
			}
		}
	}

	public static void onBlockBreak(Player p, Block b, Cancellable event) {
		if (!preCondition(p))
			return;
		EditorListenerObject obj = currentListening.get(p.getName());
		if (obj.getType().equals(ListeningType.BLOCK)) {
			obj.execute(p, b.getType().toString());
			DebugHandler.log(5, "[Listener] Object of listening type " + obj.getType().toString() + " triggered. Block=" + b.toString());
			event.setCancelled(true);
		}
	}

	public static void onEntityDamage(Player p, Entity e, Cancellable event) {
		if (!preCondition(p))
			return;
		EditorListenerObject obj = currentListening.get(p.getName());
		event.setCancelled(true);
		if (obj.getType().equals(ListeningType.MTMMOB_LEFT_CLICK)) {
			DebugHandler.log(5, "[Listener] Object of listening type " + obj.getType().toString() + " triggered.");
			if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs") && Main.getHooker().getMythicMobsAPI().isEntityMythic(e)) {
				obj.execute(p, Main.getHooker().getMythicMobsAPI().getMythicInternalName(e));
				DebugHandler.log(5, "hehe"+Main.getHooker().getMythicMobsAPI().getMythicInternalName(e));
				
			}
		} else if (obj.getType().equals(ListeningType.MOB_LEFT_CLICK)) {
			DebugHandler.log(5, "[Listener] Object of listening type " + obj.getType().toString() + " triggered.");
			obj.execute(p, e.getType().toString());
		} else if (obj.getType().equals(ListeningType.STRING)) {
			DebugHandler.log(5, "[Listener] Object of listening type " + obj.getType().toString() + " triggered.");
			if (e.getCustomName() != null)
				obj.execute(p, e.getCustomName());
			else
				obj.execute(p, QuestUtil.translate(e.getType()));
		}
	}

	public static void onNPCLeftClick(Player p, NPC npc, Cancellable event) {
		if (!preCondition(p)) {
			DebugHandler.log(5, "[Listener] Player " + p.getName() + " does not have the precondition to edit.");
			return;
		}
		EditorListenerObject obj = currentListening.get(p.getName());
		if (obj.getType().equals(ListeningType.NPC_LEFT_CLICK)) {
			obj.execute(p, Integer.toString(npc.getId()));
			DebugHandler.log(2, "NPC ID SELECTED: "+npc.getId());
			QuestChatManager.info(p, I18n.locMsg(p,"EditorMessage.NPCSelcted", me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc)));
		} else {
			DebugHandler.log(5, "[Listener] Object triggered, but the type was " + obj.getType().toString());
		}
		event.setCancelled(true);
	}

	public static void onInventoryClose(Player p, Inventory inv, InventoryView view) {
		if (!preCondition(p))
			return;
		EditorListenerObject obj = currentListening.get(p.getName());
		if (!obj.getType().equals(ListeningType.OPEN_INVENTORY)&&!obj.getType().equals(ListeningType.CRAFTING_INVENTORY))
			return;
		DebugHandler.log(5, "[Listener] Object of listening type " + obj.getType().toString() + " triggered.");
		if(obj.getType().equals(ListeningType.CRAFTING_INVENTORY)) {
				
				if(inv.getType()!=InventoryType.WORKBENCH&&inv.getType()!=InventoryType.CRAFTING) {
					return;
				}
				String claimID = UUID.randomUUID().toString();
				//remember to remove the temptable once access is finished
				tempCraftingTables.put(claimID, inv);
				obj.execute(p, claimID);
				unregister(p);
				return;
		}
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		List<ItemStack> list = new ArrayList<>();
		for (ItemStack is : inv.getContents()) {
			if (is != null && !is.getType().equals(Material.AIR))
				list.add(is);
		}

		if (view.getTitle().contains("Requirement")) {
			q.getRequirements().put(RequirementType.ITEM, list);
			QuestEditorManager.editQuestRequirement(p);
		}
		unregister(p);
		QuestChatManager.info(p, I18n.locMsg(p,"EditorMessage.ItemSaved"));
	}
	
	

	public static void register(Player p, EditorListenerObject obj) {
		currentListening.put(p.getName(), obj);
	}

	public static void unregister(Player p) {
		if (preCondition(p)) {
			QuestChatManager.info(p, I18n.locMsg(p,"CommandInfo.ClearedListening"));
			currentListening.remove(p.getName());
		}
	}

	public static boolean isListening(Player p) {
		return currentListening.containsKey(p.getName());
	}

	public static boolean preCondition(Player p) {
		return isListening(p) && ((QuestEditorManager.checkEditorMode(p, false) || ConversationEditorManager.checkEditorMode(p, false)));
	}

	@SuppressWarnings("unchecked")
	public static void registerGUI(Player p, String obj) {
		if (QuestEditorManager.checkEditorMode(p, false)) {
			if (obj.equalsIgnoreCase("requirement")) {
				QuestEditorManager.generateEditItemGUI(p, "Requirement",
						(List<ItemStack>) QuestEditorManager.getCurrentEditingQuest(p).getRequirements().get(RequirementType.ITEM));
				currentListening.put(p.getName(), new EditorListenerObject(ListeningType.OPEN_INVENTORY, obj, null));
			}
		}
	}

}
