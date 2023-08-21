package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.editor.EditorListenerHandler;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectCraftItem extends NumerableObject implements EditorObject{
	private ItemStack[] craftingReq = {null,null,null,null,null,null,null,null,null};
	private ItemStack result = new ItemStack(Material.APPLE);
	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		switch(type) {
		case "resultant":{
			ItemStack item = Main.getInstance().mcCompat.getItemInMainHand(sender);
			this.result = item.clone();
			return true;
		}
		case "recipe":{
			Inventory inv = EditorListenerHandler.tempCraftingTables.get(obj);
			if(inv == null) {
				return false;
			}
			for(int i = 1;i <=9;i++) {
				ItemStack recipe = inv.getItem(i);
				if(recipe == null||recipe.getType().equals(Material.AIR)) {
					craftingReq[i-1]=null;
					continue;
				}
				craftingReq[i-1] = recipe.clone();
			}
			EditorListenerHandler.tempCraftingTables.remove(obj);
			return true;
		}
		}
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		EditorListenerObject eobj;
		switch(type) {
		case "resultant":{
			eobj = new EditorListenerObject(ListeningType.ITEM,command,null);
			QuestBookGUIManager.openInfo(sender, "EditorMessage.RightClick");
			return eobj;
		}
		case "recipe":{
			eobj = new EditorListenerObject(ListeningType.CRAFTING_INVENTORY,command,null);
			//QuestBookGUIManager.openInfo(sender,I18n.locMsg(sender, "EditorMessage.OpenCraftingInventory"));
			Inventory inv = Bukkit.createInventory(sender, InventoryType.WORKBENCH);
			for(int i = 1;i <=9;i++) {
				ItemStack craft;
				if((craft = craftingReq[i-1]) == null) {
					continue;
				}
				inv.setItem(i, craft);
			}
			sender.openInventory(inv);
			return eobj;
		}
		}
		return super.createCommandOutput(sender, command, type);
	}

	@Override
	public void formatEditorPage(Player p, FlexibleBook page, int stage, int obj) {
		page.add(I18n.locMsg(null, "QuestEditor.Resultant"));
		page.add(new InteractiveText(p,result));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " resultant"));
		page.changeLine();
		
		page.add(I18n.locMsg(null, "QuestEditor.Recipe"));
		page.add(new InteractiveText(p,I18n.locMsg(p,"QuestEditor.Edit")).clickCommand("/mq e edit object " + stage + " " + obj + " recipe"));
		page.changeLine();
		super.formatEditorPage(p, page, stage, obj);
	}

	@Override
public String toDisplayText(Player p) {		
		return I18n.locMsg(null, "QuestObject.CraftItem",QuestUtil.translateCraftItem(QuestUtil.translateMultiple(craftingReq), result,amount));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean load(QuestIO config, String path) {
		List<ItemStack> craftingReqsRaw = (List<ItemStack>) config.getConfig().getList(path+"craftingReq");
		int index = 0;
		for(ItemStack i:craftingReqsRaw) {
			craftingReq[index] = i;
			index++;
		}
		result = config.getItemStack(path+"result");
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String objpath) {
		config.set(objpath+"craftingReq", craftingReq);
		config.set(objpath+"result", result);
		super.save(config, objpath);
	}

	@Override
	public String getConfigString() {
		return "CRAFT_ITEM";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null, "QuestObjectName.CraftItem");
	}

	@Override
	public TextComponent toTextComponent(Player p, boolean isFinished) {		
		return super.toTextComponent(true, p, ChatColor.stripColor(I18n.locMsg(null,"QuestObject.CraftItem")), isFinished, QuestUtil.translateCraftItemComponent(craftingReq, result, amount));
	}

	public ItemStack getResult() {
		return result;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public ItemStack[] getCraftingRequirement() {
		return craftingReq;
	}

	public void setCraftingRequirement(ItemStack[] craftingReq) {
		this.craftingReq = craftingReq;
	}

	
}
