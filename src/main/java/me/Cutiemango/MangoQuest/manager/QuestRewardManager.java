package me.Cutiemango.MangoQuest.manager;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability;
import me.Cutiemango.MangoQuest.editor.QuestEditorManager;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.reward.QuestGUIItem;
import me.Cutiemango.MangoQuest.objects.reward.QuestReward;
import me.Cutiemango.MangoQuest.objects.reward.RewardCache;
import me.Cutiemango.MangoQuest.objects.reward.RewardChoice;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestRewardManager implements Listener
{
	public static final int MAXMIUM_CHOICES = 9;
	private static final HashMap<String, RewardCache> rewardCache = new HashMap<>();

	public static void openEditMainGUI(Player p) {
		if (!QuestEditorManager.checkEditorMode(p, false))
			return;
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		QuestReward reward = q.getQuestReward();
		Inventory inv = Bukkit.createInventory(null, 27,
				QuestChatManager.translateColor(I18n.locMsg(p,"QuestReward.RewardEditTitle") + ChatColor.stripColor(q.getQuestName())));
		int a = reward.getChoiceAmount();

		for (int i = 0; i < Math.min(a + 1, MAXMIUM_CHOICES); i++) {
			if (i == a)
				inv.setItem(getRewardSlot(a + 1, i), newRewardChoice(i));
			else
				inv.setItem(getRewardSlot(a + 1, i), itemButton(p, reward.getChoice(i), i));
		}

		inv.setItem(0, setNPC(reward.getRewardNPC()));
	
		inv.setItem(18, backToMenu());
		inv.setItem(26, editRewardAmount(reward.getRewardAmount()));
		//if(p.getName().equals("SakurajiKanade")) {
		//	p.getInventory().addItem(setNPC(reward.getRewardNPC()),editRewardAmount(reward.getRewardAmount()),backToMenu(),glassPane());
			
		//}

		for (int i = 0; i < 27; i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR))
				inv.setItem(i, glassPane());
		}
		p.openInventory(inv);
	}

	public static void openEditRewardGUI(Player p, int index) {
		if (!QuestEditorManager.checkEditorMode(p, false))
			return;
		String title = I18n.locMsg(p,"QuestReward.ChoiceEditTitle");
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		title += ChatColor.stripColor(QuestChatManager.translateColor(q.getQuestName()));
		if (title.length() >= 32)
			title = title.substring(0, 27) + "...";
		title += "@" + index;
		Inventory inv = Bukkit.createInventory(null, 27, title);

		if (q.getQuestReward().getChoices().size() <= index)
			q.getQuestReward().getChoices().add(index, new RewardChoice(new ArrayList<>()));

		for (ItemStack item : q.getQuestReward().getChoice(index).getItems()) {
			if (item == null || item.getType().equals(Material.AIR))
				continue;
			inv.addItem(item);
		}
		inv.setItem(26, removeRewardChoice());
		p.openInventory(inv);
	}

	public static void removeRewardChoice(Player p, int index) {
		if (!QuestEditorManager.checkEditorMode(p, false))
			return;
		QuestReward reward = QuestEditorManager.getCurrentEditingQuest(p).getQuestReward();
		if (index >= reward.getChoices().size())
			return;
		reward.getChoices().remove(index);
	}

	private static ItemStack editRewardAmount(int amount) {
		QuestGUIItem anvil = new QuestGUIItem(Material.ANVIL, 1);
		anvil.setName(I18n.locMsg(null,"QuestReward.EditRewardAmount"));
		anvil.setLore(QuestUtil
				.createList(I18n.locMsg(null,"QuestReward.CurrentRewardAmount", Integer.toString(amount), I18n.locMsg(null,"QuestReward.ClickToEdit"))));
		return anvil.get();
	}

	private static ItemStack newRewardChoice(int index) {
		QuestGUIItem chest = new QuestGUIItem(Material.ENDER_CHEST, 1);
		chest.setName(I18n.locMsg(null,"QuestReward.NewRewardChoice"));
		chest.setLore(QuestUtil.createList(I18n.locMsg(null,"QuestReward.NewRewardChoiceLore"), QuestChatManager.translateColor("&0" + index)));
		chest.glowEffect();
		return chest.get();
	}

	private static ItemStack glassPane() {
		QuestGUIItem glassPane = new QuestGUIItem(Main.getInstance().mcCompat.getColouredItem(Main.getInstance().mcCompat.getCompatMaterial("STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE"), 0, (byte)8));
		glassPane.setName("&0");
		return glassPane.get();
	}

	private static ItemStack itemButton(Player p, RewardChoice rc, int index) {
		ItemStack firstItem = new ItemStack(Material.BARRIER);
		QuestGUIItem button;
		if (rc.getItems().size() == 0)
			button = new QuestGUIItem(Material.BARRIER, 1);
		else {
			firstItem = rc.getItems().get(0);
			button = new QuestGUIItem(firstItem.getType(), firstItem.getAmount());
		}

		if (firstItem.hasItemMeta() && MinecraftCompatability.isUnbreakable(firstItem.getItemMeta()))
			button.setUnbreakable();

		List<String> lore = new ArrayList<>();
		for (ItemStack item : rc.getItems()) {
			if (item == null || item.getType() == Material.AIR)
				continue;
			lore.add(QuestChatManager.translateColor("&f- " + getItemName(p, item)));
		}
		lore.add(I18n.locMsg(null,"QuestReward.ClickToEdit"));
		lore.add(QuestChatManager.translateColor("&0" + index));
		button.setName("&f" + getItemName(p, firstItem));
		button.setLore(lore);
		return button.get();
	}

	private static ItemStack setNPC(NPC n) {
		//Material.PLAYER_HEAD
		QuestGUIItem npc = new QuestGUIItem(Main.getInstance().mcCompat.getCompatColouredItem("SKULL", "PLAYER_HEAD", 1, (byte)3));
		if (n != null && n.getName() != null)
			npc.setName(I18n.locMsg(null,"QuestReward.RewardNPC", n.getName()));
		else
			npc.setName(I18n.locMsg(null,"QuestReward.RewardNPC", I18n.locMsg(null,"Translation.UnknownNPC")));
		return npc.get();
	}

	private static ItemStack removeRewardChoice() {
		QuestGUIItem barrier = new QuestGUIItem(Material.BARRIER, 1);
		barrier.setName(I18n.locMsg(null,"QuestReward.RemoveChoice"));
		barrier.setLore(QuestUtil.createList(I18n.locMsg(null,"QuestReward.RemoveChoiceLore")));
		barrier.glowEffect();
		return barrier.get();
	}

	private static ItemStack backToMenu() {
		QuestGUIItem sign = new QuestGUIItem(Main.getInstance().mcCompat.getCompatMaterial("BOOK","WRITABLE_BOOK"), 1);
		sign.setName(I18n.locMsg(null,"QuestEditor.Return"));
		return sign.get();
	}

	public static void saveItemChoice(Player p, List<ItemStack> items, int index) {
		Quest q = QuestEditorManager.getCurrentEditingQuest(p);
		q.getQuestReward().setChoice(index, new RewardChoice(items));
		if (q.getQuestReward().getChoice(index).getItems().isEmpty()) {
			QuestChatManager.info(p, I18n.locMsg(p,"QuestReward.RemoveDueToEmpty"));
			removeRewardChoice(p, index);
		}
	}

	public static String getItemName(Player p,ItemStack item) {
		if (item == null)
			return I18n.locMsg(null,"Translation.UnknownItem");
		else if (item.getType() == Material.BARRIER)
			return I18n.locMsg(null,"QuestReward.DefaultRewardIcon");
		else
			return QuestChatManager.translateColor(QuestUtil.getItemName(p, item) + " &f" + ChatColor
					.stripColor(I18n.locMsg(null,"QuestEditor.Amount", Integer.toString(item.getAmount()))));
	}

	
	public static int getRewardSlot(int amount, int x) {
		int[] arySlot;
		if (amount % 2 == 0) {
			arySlot = new int[] { 3, 5, 1, 7, 2, 6, 0, 8 };
		} else {
			arySlot = new int[] { 4, 2, 6, 3, 5, 1, 7, 0, 8 };
		}
		return 9 + arySlot[x];
	}

	public static void giveRewardItem(Player p, ItemStack is) {
		if (p.getInventory().firstEmpty() == -1) {
			QuestChatManager.info(p, I18n.locMsg(p,"QuestReward.RewardDropped"));
			p.getWorld().dropItemNaturally(p.getLocation(), is);
		} else {
			QuestChatManager.info(p, I18n.locMsg(p,"QuestReward.GiveItemReward", QuestUtil.getItemName(p, is), Integer.toString(is.getAmount())));
			p.getInventory().addItem(is);
		}
	}

	public static RewardCache getRewardCache(Player p) {
		return rewardCache.get(p.getName());
	}

	public static boolean hasRewardCache(Player p) {
		return rewardCache.containsKey(p.getName());
	}

	public static void removeCache(Player p) {
		rewardCache.remove(p.getName());
	}

	public static void registerCache(Player p, Quest q) {
		RewardCache cache = new RewardCache(p, q);
		rewardCache.put(p.getName(), cache);
		cache.openGUI();
	}
}
