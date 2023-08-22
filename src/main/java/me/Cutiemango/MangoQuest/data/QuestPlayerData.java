package me.Cutiemango.MangoQuest.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scoreboard.Scoreboard;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.Pair;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.commands.AdminCommand;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.conversation.QuestChoice.Choice;
import me.Cutiemango.MangoQuest.conversation.QuestConversation;
import me.Cutiemango.MangoQuest.conversation.StartTriggerConversation;
import me.Cutiemango.MangoQuest.event.QuestFinishEvent;
import me.Cutiemango.MangoQuest.event.QuestObjectProgressEvent;
import me.Cutiemango.MangoQuest.event.QuestQuitEvent;
import me.Cutiemango.MangoQuest.event.QuestTakeEvent;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.manager.RequirementManager;
import me.Cutiemango.MangoQuest.manager.TimeHandler;
import me.Cutiemango.MangoQuest.manager.database.DatabaseLoader;
import me.Cutiemango.MangoQuest.manager.database.DatabaseSaver;
import me.Cutiemango.MangoQuest.manager.mongodb.MongodbLoader;
import me.Cutiemango.MangoQuest.manager.mongodb.MongodbSaver;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.CustomQuestObject;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreakBlock;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreedMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBucketFill;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBucketFill.FillType;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectConsumeItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectCraftItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectDeliverItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnchantItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnterCommand;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectFishing;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectKillMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLaunchProjectile;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLaunchProjectile.ProjectileType;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLoginServer;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectMoveDistance;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlaceholderAPI;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlayerChat;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectReachLocation;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectRegeneration;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectShearSheep;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectSleep;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTalkToNPC;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTameMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectUseAnvil;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;

public class QuestPlayerData {

	// used for database
	private int PDID;
	private Location lastLocation;
	public static int ticks = 0;
	private Player owner;
	private QuestIO save;
	private ItemStack lastClicked = new ItemStack(Material.AIR);
	private Entity lastTamed;

	public ItemStack getLastClicked() {
		return lastClicked;
	}

	public void setLastClicked(ItemStack lastClicked) {
		this.lastClicked = lastClicked;
	}
	private Set<QuestProgress> papiQuests = new HashSet<>();	
	
	public Set<QuestProgress> getPapiQuests() {
		return papiQuests;
	}

	public void setPapiQuests(Set<QuestProgress> papiQuests) {
		this.papiQuests = papiQuests;
	}
	private Set<QuestProgress> currentQuests = new HashSet<>();
	private Set<QuestFinishData> finishedQuests = new HashSet<>();
	private Set<String> finishedConversations = new HashSet<>();
	private Map<Entity, Location> projectilesInAir = new HashMap<>();
	private List<Quest> redoableQuests = new ArrayList<>();
	private boolean isSwimming = false;

	private Map<NPC, List<Quest>> npcEffects = new HashMap<>();

	public Map<NPC, List<Quest>> getNpcEffects() {
		return npcEffects;
	}

	public void setNpcEffects(Map<NPC, List<Quest>> npcEffects) {
		this.npcEffects = npcEffects;
	}

	public void putNPCEffectsQuestList(NPC npc, List<Quest> questList) {
		this.npcEffects.put(npc, questList);
	}

	public boolean isSwimming() {
		return isSwimming;
	}

	public void setSwimming(boolean isSwimming) {
		this.isSwimming = isSwimming;
	}

	private HashMap<Integer, Integer> friendPointStorage = new HashMap<>();

	private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

	public QuestPlayerData(Player p) {
		owner = p;
	}

	public void loadExistingData(Set<QuestProgress> q, Set<QuestFinishData> fd, Set<String> convs,
			HashMap<Integer, Integer> map, int id) {
		currentQuests = q;
		finishedQuests = fd;
		finishedConversations = convs;
		friendPointStorage = map;
		PDID = id;
	}

	public CompletableFuture<Void> load(ConfigSettings.SaveType saveType) {
		return CompletableFuture.runAsync(() -> {
			switch (saveType) {
			case YML:
				loadFromYml();
				break;
			case SQL:
				DatabaseLoader.loadPlayer(this);
				break;
			case MONGODB:
				MongodbLoader.loadPlayer(this);
				break;
			}
		});
	}

	public CompletableFuture<Void> save() {
		return CompletableFuture.runAsync(() -> { // save actions can be done async
			try {
				switch (ConfigSettings.SAVE_TYPE) {
				case YML:
					saveToYml();
					break;
				case SQL:
					DatabaseSaver.savePlayerData(this);
					break;
				case MONGODB:
					MongodbSaver.savePlayerData(this);
					break;
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void loadFromYml() {
		save = new QuestIO(owner);
		save.set("LastKnownID", owner.getName());

		if (save.isSection("QuestProgress")) {
			List<QuestProgress> qps = new ArrayList<>();
			for (String index : save.getSection("QuestProgress")) {
				Quest q = QuestUtil.getQuest(index);
				if (q == null) {
					QuestChatManager.error(owner, I18n.locMsg(owner, "CommandInfo.TargetProgressNotFound", index));
					save.removeSection("QuestProgress." + index);
					continue;
				}

				if (q.getVersion().getTimeStamp() != save.getLong("QuestProgress." + q.getInternalID() + ".Version")) {
					QuestChatManager.error(owner, I18n.locMsg(owner, "CommandInfo.OutdatedQuestVersion", index));
					save.removeSection("QuestProgress." + index);
					continue;
				}

				int t = 0;
				int s = save.getInt("QuestProgress." + index + ".QuestStage");
				List<QuestObjectProgress> qplist = new ArrayList<>();
				for (SimpleQuestObject ob : q.getStage(s).getObjects()) {
					QuestObjectProgress qp = new QuestObjectProgress(ob,
							save.getInt("QuestProgress." + index + ".QuestObjectProgress." + t));
					if (ob instanceof DecimalObject) {
						qp.setProgressD(save.getDouble("QuestProgress." + index + ".QuestObjectProgress." + t));
						
					}
					if (save.getConfig()
							.contains("QuestProgress." + index + ".QuestObjectProgress._" + t + "_lastinvokedmilli")) {
						qp.setLastInvokedMilli(save.getLong(
								"QuestProgress." + index + ".QuestObjectProgress._" + t + "_lastinvokedmilli"));
					}
					qp.checkIfFinished();
					qplist.add(qp);
					t++;
				}
				QuestProgress qp = new QuestProgress(q, owner, s, qplist,
						save.getLong("QuestProgress." + index + ".TakeStamp"));
				if (qp.getQuest().isTimeLimited() || (qp.getQuest().hasRequirement()
						&& (qp.getQuest().getRequirements().containsKey(RequirementType.SERVER_TIME)
								|| qp.getQuest().getRequirements().containsKey(RequirementType.WORLD_TIME)))) {
					qps.add(qp);
				}
				currentQuests.add(qp);
			}
			if (!qps.isEmpty())
				QuestStorage.timedProgress.put(owner.getUniqueId().toString(), qps);
		}

		if (save.isSection("FinishedQuest")) {
			for (String s : save.getSection("FinishedQuest")) {
				if (QuestUtil.getQuest(s) == null) {
					DebugHandler.log(0, I18n.locMsg(null, "CmdLog.NoValidQuest",s,"{loading quests into cache}"));
					if(ConfigSettings.differentialquestsserver) {
						IncompatibleQuestFinishData qd = new IncompatibleQuestFinishData(owner,s,save.getInt("FinishedQuest." + s + ".FinishedTimes"),
							save.getLong("FinishedQuest." + s + ".LastFinishTime"),
							save.getBoolean("FinishedQuest." + s + ".RewardTaken"));
							finishedQuests.add(qd);
					}
				}else {
					QuestFinishData qd = new QuestFinishData(owner, QuestUtil.getQuest(s),
							save.getInt("FinishedQuest." + s + ".FinishedTimes"),
							save.getLong("FinishedQuest." + s + ".LastFinishTime"),
							save.getBoolean("FinishedQuest." + s + ".RewardTaken"));
					finishedQuests.add(qd);
				}
				
				
				
			
				

			}
		}

		if (save.isSection("FriendPoint"))
			for (String s : save.getSection("FriendPoint"))
				friendPointStorage.put(Integer.parseInt(s), save.getInt("FriendPoint." + s));

		if (save.getStringList("FinishedConversation") != null) {
			for (String s : save.getStringList("FinishedConversation")) {
				QuestConversation qc = ConversationManager.getConversation(s);
				if (qc != null)
					finishedConversations.add(qc.getInternalID());
			}
		}

		save.save();

		if (ConfigSettings.POP_LOGIN_MESSAGE)
			QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.PlayerLoadComplete"));
	}

	public void saveToCustomYml(YamlConfiguration save) {

		save.set("LastKnownID", owner.getName());
		for (QuestFinishData q : finishedQuests) {
			String id = q.getQuest().getInternalID();
			save.set("FinishedQuest." + id + ".FinishedTimes", q.getFinishedTimes());
			save.set("FinishedQuest." + id + ".LastFinishTime", q.getLastFinish());
			save.set("FinishedQuest." + id + ".RewardTaken", q.isRewardTaken());
		}

		save.set("QuestProgress", "");

		if (!currentQuests.isEmpty())
			for (QuestProgress qp : currentQuests)
				qp.save(save);

		for (int i : friendPointStorage.keySet())
			save.set("FriendPoint." + i, friendPointStorage.get(i));

		save.set("FinishedConversation", QuestUtil.convert(new HashSet<>(finishedConversations)));
		// save.save();
	}

	public void saveToYml() {
		
		save.set("LastKnownID", owner.getName());
		for (QuestFinishData q : finishedQuests) {
			//DebugHandler.log(2, "damn bro "+q.getQuest().getQuestName());
			String id = q instanceof IncompatibleQuestFinishData ? ((IncompatibleQuestFinishData)q).getIncompatQuest():q.getQuest().getInternalID();
			save.set("FinishedQuest." + id + ".FinishedTimes", q.getFinishedTimes());
			save.set("FinishedQuest." + id + ".LastFinishTime", q.getLastFinish());
			save.set("FinishedQuest." + id + ".RewardTaken", q.isRewardTaken());
		}
		//DebugHandler.log(2, "end game");
		save.set("QuestProgress", "");

		if (!currentQuests.isEmpty())
			for (QuestProgress qp : currentQuests)
				qp.save(save);

		for (int i : friendPointStorage.keySet())
			save.set("FriendPoint." + i, friendPointStorage.get(i));

		save.set("FinishedConversation", QuestUtil.convert(new HashSet<>(finishedConversations)));
		save.save();
	}

	public Player getPlayer() {
		return owner;
	}

	public int getPDID() {
		return PDID;
	}

	public HashMap<Integer, Integer> getFriendPointStorage() {
		return friendPointStorage;
	}

	public Set<String> getFinishedConversations() {
		return finishedConversations;
	}

	public boolean hasFinished(Quest q) {
		if (q == null)
			return false;
		for (QuestFinishData qd : finishedQuests) {
			if (qd.getQuest() == null && !(qd instanceof IncompatibleQuestFinishData)) {
				continue;
			}
			
			if(qd instanceof IncompatibleQuestFinishData) {
				if(((IncompatibleQuestFinishData) qd).getIncompatQuest().equals(q.getInternalID())) {
					return true;
				}
				continue;
			}else {
				if (qd.getQuest().getInternalID().equals(q.getInternalID()))
					return true;
			}
				
			
			
		}
		return false;
	}

	public boolean hasFinished(QuestConversation qc) {
		return finishedConversations.contains(qc.getInternalID());
	}

	public QuestProgress getProgress(Quest q) {
		for (QuestProgress qp : currentQuests) {
			if (q.getInternalID().equals(qp.getQuest().getInternalID()))
				return qp;
		}
		return null;
	}

	public Set<QuestProgress> getProgresses() {
		return currentQuests;
	}

	public int getNPCfp(int id) {
		if (!friendPointStorage.containsKey(id))
			friendPointStorage.put(id, 0);
		return friendPointStorage.get(id);
	}

	public void addNPCfp(int id, int value) {
		if (!friendPointStorage.containsKey(id))
			friendPointStorage.put(id, 0);
		friendPointStorage.put(id, friendPointStorage.get(id) + value);
		DebugHandler.log(3,
				"[Listener] Player " + owner.getName() + "'s friend point of NPC id=" + id + " raised by " + value);
	}

	public void setNPCfp(int id, int value) {
		friendPointStorage.put(id, value);
	}

	public boolean meetFriendPointReq(Choice choice) {
		for (Integer npc : choice.getFriendPointReq().keySet()) {
			if (!friendPointStorage.containsKey(npc))
				return false;
			if (!(friendPointStorage.get(npc) >= choice.getFriendPointReq().get(npc)))
				return false;
		}
		return true;
	}

	public void addFinishConversation(QuestConversation qc) {
		finishedConversations.add(qc.getInternalID());
	}

	public boolean checkStartConv(Quest q) {
		if (ConversationManager.getStartConversation(q) == null)
			return true;
		StartTriggerConversation conv = ConversationManager.getStartConversation(q);
		if (!hasFinished(conv)) {
			if (ConversationManager.isInConvProgress(owner, conv))
				ConversationManager.openConversation(owner, ConversationManager.getConvProgress(owner));
			else
				ConversationManager.startConversation(owner, conv);
			return false;
		}
		return true;
	}

	public boolean checkQuestSize(boolean msg) {
		if (currentQuests.size() + 1 > ConfigSettings.MAXIMUM_QUEST_AMOUNT) {
			if (msg)
				QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.QuestListFull"));
			return false;
		}
		return true;
	}

	public void takeQuest(Quest q, boolean checkConv) {
		if (!canTake(q, true))
			return;
		
		if (!q.isCommandQuest()) {
			if (!isNearNPC(q.getQuestNPC())) {
				QuestChatManager.error(owner, I18n.locMsg(owner, "CommandInfo.OutRanged"));
				return;
			}
		}
		if (!checkQuestSize(true))
			return;
		if (checkConv && !checkStartConv(q))
			return;
		forceTake(q, false);
	}
	
	public void forceTake(Quest q, boolean msg) {
		if (!checkQuestSize(true) || !checkStartConv(q))
			return;
		q.trigger(owner, TriggerType.TRIGGER_ON_TAKE, -1);
		QuestProgress progress = new QuestProgress(q, owner);

		currentQuests.add(progress);
		if(progress.getCurrentObjects().stream().anyMatch((qop)->{
			return qop.getObject() instanceof QuestObjectPlaceholderAPI;
		})) {
			this.papiQuests.add(progress);
		}
		if (progress.getQuest().isTimeLimited() || (progress.getQuest().hasRequirement()
				&& (progress.getQuest().getRequirements().containsKey(RequirementType.SERVER_TIME)
						|| progress.getQuest().getRequirements().containsKey(RequirementType.WORLD_TIME)))) {
			if (QuestStorage.timedProgress.containsKey(owner.getUniqueId().toString())) {
				List<QuestProgress> obtained = QuestStorage.timedProgress.get(owner.getUniqueId().toString());
				obtained.add(progress);
				QuestStorage.timedProgress.put(owner.getUniqueId().toString(), obtained);
			} else {
				List<QuestProgress> constructed = new ArrayList<>();
				constructed.add(progress);
				QuestStorage.timedProgress.put(owner.getUniqueId().toString(), constructed);
			}
		}
		if (q.getQuestNPC() != null) {
				// int index = 0;
			if(this.getNpcEffects().get(q.getQuestNPC()) != null) {
				this.getNpcEffects().get(q.getQuestNPC()).removeIf((quest) -> {
					return quest.getInternalID().equals(q.getInternalID());
				});
			}
			
		}
		if (msg)
			QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.ForceTakeQuest", q.getQuestName()));
		DebugHandler.log(3, "[Listener] Player " + owner.getName() + " accepted a new quest " + q.getQuestName());
		Bukkit.getPluginManager().callEvent(new QuestTakeEvent(owner, q));
	}

	public void forceNextStage(Quest q, boolean msg) {
		if (!isCurrentlyDoing(q))
			return;
		QuestProgress qp = getProgress(q);
		qp.nextStage();
		if (msg)
			QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.ForceNextStage", q.getQuestName()));
		DebugHandler.log(3,
				"[Listener] Player " + owner.getName() + "'s quest stage of quest " + q.getQuestName() + " shifted.");
	}

	public void forceFinishObj(Quest q, int id, boolean msg) {
		if (!isCurrentlyDoing(q))
			return;
		QuestProgress qp = getProgress(q);
		QuestObjectProgress qop = qp.getCurrentObjects().get(id - 1);
		if (qop != null) {
			qop.finish();
			this.checkFinished(qp, qop);
			if (msg)
				QuestChatManager.info(owner,
						I18n.locMsg(owner, "CommandInfo.ForceFinishObject", qop.getObject().toPlainText()));
			DebugHandler.log(3, "[Listener] Player " + owner.getName() + "'s quest object of quest " + q.getQuestName()
					+ " finished");
		}
	}

	public void forceFinish(Quest q, boolean msg) {
		if (!isCurrentlyDoing(q))
			return;
		QuestProgress qp = getProgress(q);
		qp.finish();
		if (msg)
			QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.ForceFinishQuest", q.getQuestName()));
		DebugHandler.log(3, "[Listener] Player " + owner.getName() + "'s quest " + q.getQuestName() + " finished");
		Bukkit.getPluginManager().callEvent(new QuestFinishEvent(owner, q));
	}

	public void forceQuit(Quest q, boolean msg) {
		if (!isCurrentlyDoing(q))
			return;
		QuestQuitEvent event = new QuestQuitEvent(owner, q);
		Bukkit.getPluginManager().callEvent(event);
		q.trigger(owner, TriggerType.TRIGGER_ON_QUIT, -1);
		for(QuestProgress qp:currentQuests) {
			if(QuestValidater.weakValidate(q, qp.getQuest())){
				papiQuests.removeIf((qp1)->{
					return QuestValidater.weakValidate(qp1.getQuest(),q);
				});
			}
		}
		removeProgress(q);
		if (QuestStorage.timedProgress.containsKey(owner.getUniqueId().toString())) {
			List<QuestProgress> obtained = QuestStorage.timedProgress.get(owner.getUniqueId().toString());
			obtained.removeIf(qp -> QuestValidater.weakValidate(qp.getQuest(), q));
			if (obtained.isEmpty()) {
				QuestStorage.timedProgress.remove(owner.getUniqueId().toString());
			} else {
				QuestStorage.timedProgress.put(owner.getUniqueId().toString(), obtained);
			}
		}
		if (q.getQuestNPC() != null) {
			List<Quest> questList = new ArrayList<>();
			if (this.npcEffects.containsKey(q.getQuestNPC())) {
				questList = this.npcEffects.get(q.getQuestNPC());
			}
			questList.add(q);
			this.npcEffects.put(q.getQuestNPC(), questList);
		}
		if (msg)
			QuestChatManager.error(owner, I18n.locMsg(owner, "CommandInfo.ForceQuitQuest", q.getQuestName()));
		DebugHandler.log(3, "[Listener] Player " + owner.getName() + " quitted quest " + q.getQuestName());
	}

	public void quitQuest(Quest q) {
		forceQuit(q, false);
	}

	public List<QuestProgress> getNPCtoTalkWith(NPC npc) {
		ArrayList<QuestProgress> all = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> qp.getCurrentObjects().stream().anyMatch(qop -> checkNPC(qop, npc)))
				.forEach(all::add);
		return all;
	}

	public boolean isNearNPC(NPC npc) {
		if (npc == null || npc.getStoredLocation() == null)
			return true;
		return npc.getStoredLocation().getWorld().getName().equals(owner.getWorld().getName())
				&& npc.getStoredLocation().distance(owner.getLocation()) < 20;
	}

	public boolean checkPlayerInWorld(Quest q) {
		return !q.hasWorldLimit() || owner.getWorld().getName().equals(q.getWorldLimit().getName());
	}

	public void objectSuccess(QuestProgress qp, QuestObjectProgress qop) {

		qop.setProgress(qop.getProgress() + 1);
		DebugHandler.log(5, "[Listener] Player %s's object succeeded: (%d/%d)", owner.getName(), qop.getProgress(),
				qop.getObject() instanceof NumerableObject ? ((NumerableObject) qop.getObject()).getAmount() : 1);
		checkFinished(qp, qop);
	}

	public void objectSuccess(QuestProgress qp, QuestObjectProgress qop, double amount) {

		qop.setProgressD(qop.getProgressD() + amount);
		DebugHandler.log(5, "[Listener] Player %s's object succeeded: (%f/%f)", owner.getName(), qop.getProgressD(),
				((DecimalObject) qop.getObject()).getAmount());
		checkFinished(qp, qop);
	}

	public boolean isProjectileInAir(Entity e) {
		if (projectilesInAir.containsKey(e)) {
			return true;
		}
		return false;
	}

	private boolean checkProjectile(QuestObjectProgress qop, Entity e) {
		if (!(qop.getObject() instanceof QuestObjectLaunchProjectile) || qop.isFinished()) {
			return false;
		}
		QuestObjectLaunchProjectile qolp = (QuestObjectLaunchProjectile) qop.getObject();
		if (Main.getInstance().mcCompat.getItemInMainHand(owner).getType().equals(Material.FISHING_ROD)) {
			if (qolp.getType() == ProjectileType.FISHING_ROD) {
				return true;
			} else {
				return false;
			}
		}

		if (e instanceof ThrownPotion) {
			if(ConfigSettings.MC_1_8) {
				return false;
			}
			if (Main.getInstance().mcCompat.getItemInMainHand(owner).getType() == Main.getInstance().mcCompat.getCompatMaterial("NOTWORKIN1.8","LINGERING_POTION")){
				if (qolp.getType() == ProjectileType.LINGERING_POTIONS) {
					if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
						if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
								&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
							return false;
						}
					}
					if (qolp.getDistance() != null) {
						if (!projectilesInAir.containsKey(e)) {
							projectilesInAir.put(e, e.getLocation());
							return false;
						} else {
							if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
								projectilesInAir.remove(e);
								return false;
							}
						}
					}
					return true;
				} else {
					return false;
				}
			} else if (Main.getInstance().mcCompat.getItemInMainHand(owner).getType() == Main.getInstance().mcCompat.getCompatMaterial("NOTWORKIN1.8", "SPLASH_POTION")) {
				if (qolp.getType() == ProjectileType.SPLASH_POTIONS) {
					if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
						if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
								&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
							return false;
						}
					}
					if (qolp.getDistance() != null) {
						if (!projectilesInAir.containsKey(e)) {
							projectilesInAir.put(e, e.getLocation());
							return false;
						} else {
							if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
								projectilesInAir.remove(e);
								return false;
							}
						}
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (e instanceof ThrownExpBottle) {
			if (qolp.getType() == ProjectileType.BOTTLE_OF_ENCHANTMENT) {
				if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
					if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
							&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
						return false;
					}
				}
				if (qolp.getDistance() != null) {
					if (!projectilesInAir.containsKey(e)) {
						projectilesInAir.put(e, e.getLocation());
						return false;
					} else {
						if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
							projectilesInAir.remove(e);
							return false;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		}
		switch (e.getType()) {
		case ARROW: {
			if (qolp.getType().equals(ProjectileType.ARROW)) {
				if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
					if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
							&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
						return false;
					}
				}
				if (qolp.getDistance() != null) {
					if (!projectilesInAir.containsKey(e)) {
						projectilesInAir.put(e, e.getLocation());
						return false;
					} else {
						if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
							projectilesInAir.remove(e);
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}
		case FIREBALL: {
			if (qolp.getType().equals(ProjectileType.FIREBALL)) {
				if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
					if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
							&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
						return false;
					}
				}
				if (qolp.getDistance() != null) {
					if (!projectilesInAir.containsKey(e)) {
						projectilesInAir.put(e, e.getLocation());
						return false;
					} else {
						if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
							projectilesInAir.remove(e);
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}
		case SNOWBALL: {
			if (qolp.getType().equals(ProjectileType.SNOWBALL)) {
				if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
					if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
							&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
						return false;
					}
				}
				if (qolp.getDistance() != null) {
					if (!projectilesInAir.containsKey(e)) {
						projectilesInAir.put(e, e.getLocation());
						return false;
					} else {
						if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
							projectilesInAir.remove(e);
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}


		}
		if(!ConfigSettings.LEGACY_MC) {
			if(e.getType() == Main.getInstance().mcCompat.getCompatEntityType("", "TRIDENT")) {
				if (qolp.getType().equals(ProjectileType.TRIDENT)) {
					if (qolp.getRequiredItem() != null && qolp.getDistance() == null) {
						if (!lastClicked.isSimilar(qolp.getRequiredItem()) && !ConfigSettings.USE_WEAK_ITEM_CHECK
								&& QuestValidater.weakItemCheck(qolp.getRequiredItem(), lastClicked, false)) {
							return false;
						}
					}
					if (qolp.getDistance() != null) {
						if (!projectilesInAir.containsKey(e)) {
							projectilesInAir.put(e, e.getLocation());
							return false;
						} else {
							if (Math.abs(projectilesInAir.get(e).distance(e.getLocation())) < qolp.getDistance()) {
								projectilesInAir.remove(e);
								return false;
							}
						}
					}
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public void launchProjectile(Entity e) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkProjectile(qop, e)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkCommand(QuestObjectProgress qop, String message) {
		if (!(qop.getObject() instanceof QuestObjectEnterCommand) || qop.isFinished()) {
			return false;
		}
		QuestObjectEnterCommand qopc = (QuestObjectEnterCommand) qop.getObject();
		if (qopc.isUseRegex()) {
			Pattern pattern = Pattern.compile(qopc.getFilter());
			Matcher matcher = pattern.matcher(message);
			if (matcher.matches()) {
				return true;
			}
		} else {
			if (message.equals(qopc.getFilter())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkFill(QuestObjectProgress qop, ItemStack stack, Material type) {
		if (!(qop.getObject() instanceof QuestObjectBucketFill) || qop.isFinished()) {
			return false;
		}
		QuestObjectBucketFill qobf = (QuestObjectBucketFill) qop.getObject();
		if (qobf.getRequiredItem() != null && stack.clone().isSimilar(qobf.getRequiredItem())
				&& (QuestValidater.weakItemCheck(qobf.getRequiredItem(), stack, true)
						|| !ConfigSettings.USE_WEAK_ITEM_CHECK)) {
			return false;
		}
		switch (type) {
		case WATER: {
			if (qobf.getType() == FillType.WATER) {
				return true;
			}
			return false;
		}
		case LAVA: {
			if (qobf.getType() == FillType.LAVA) {
				return true;
			}
			return false;
		}
		default: {
			return false;
		}
		}
	}

	private boolean checkSleep(QuestObjectProgress qop) {
		if (!(qop.getObject() instanceof QuestObjectSleep) || qop.isFinished()) {
			return false;
		}
		// if player leaves bed(wake up) in the morning
		if (owner.getWorld().getTime() == 23000) {
			return true;
		}
		return false;
	}

	public void sleep() {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkSleep(qop)).collect(Collectors.toList()).forEach(qop -> {
				AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
				ref.set(new Pair<>(qp, qop));
				list.add(ref);
			});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkTame(QuestObjectProgress qop, Entity e) {
		if (!(qop.getObject() instanceof QuestObjectTameMob) || qop.isFinished()) {
			return false;
		}
		QuestObjectTameMob qotm = (QuestObjectTameMob) qop.getObject();
		if (Main.getHooker().hasMythicMobEnabled() && Main.getHooker().getMythicMobsAPI().isEntityMythic(e)) {
			if (!qotm.isMythicObject()) {
				return false;
			}
			String type = Main.getHooker().getMythicMobsAPI().getMythicInternalName(e);
			return qotm.getMythicMob().equals(type);
		} else {
			if (!e.getType().equals(qotm.getType()))
				return false;
			if (qotm.hasCustomName())
				return e.getCustomName() != null && e.getCustomName().equals(qotm.getCustomName());
			return true;
		}
	}

	public void tameMob(LivingEntity e) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkTame(qop, e)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkBreed(QuestObjectProgress qop, LivingEntity Father, LivingEntity Mother) {
		if (!(qop.getObject() instanceof QuestObjectBreedMob) || qop.isFinished()) {
			return false;
		}
		if (Father.getType() != Mother.getType()) {
			return false;
		}
		// whatever,just using father because mother is of same type as father

		QuestObjectBreedMob qotm = (QuestObjectBreedMob) qop.getObject();
		if (Main.getHooker().hasMythicMobEnabled() && Main.getHooker().getMythicMobsAPI().isEntityMythic(Father)) {
			if (!qotm.isMythicObject()) {
				return false;
			}
			String type = Main.getHooker().getMythicMobsAPI().getMythicInternalName(Father);
			String type1 = Main.getHooker().getMythicMobsAPI().getMythicInternalName(Mother);
			return qotm.getMythicMob().equals(type) && qotm.getMythicMob().equals(type1);
		} else {
			if (!Father.getType().equals(qotm.getType()))
				return false;
			if (qotm.hasCustomName())
				return Father.getCustomName() != null && Father.getCustomName().equals(qotm.getCustomName())
						&& Mother.getCustomName() != null && Mother.getCustomName().equals(qotm.getCustomName());
			return true;
		}
	}

	public void breedMob(LivingEntity Father, LivingEntity Mother) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkBreed(qop, Father, Mother)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkEnchants(QuestObjectProgress qop, ItemStack item, Map<Enchantment, Integer> map) {
		if (!(qop.getObject() instanceof QuestObjectEnchantItem) || qop.isFinished()) {
			return false;
		}
		QuestObjectEnchantItem qoei = (QuestObjectEnchantItem) qop.getObject();
		if ((!qoei.isUseExactItem() && qoei.getItemType() == item.getType() && qoei.getRequiredItem() == null)
				|| (qoei.isUseExactItem() && qoei.getRequiredItem().isSimilar(item))
				|| (qoei.isUseExactItem() && QuestValidater.weakItemCheck(qoei.getRequiredItem(), item, true)
						&& ConfigSettings.USE_WEAK_ITEM_CHECK)) {
			// if(qoei.getRequiredItem()!=null) {
			// Bukkit.getLogger().info(qoei.getRequiredItem().getItemMeta().toString());
			// Bukkit.getLogger().info(item.getItemMeta().toString());
			// }
			if (qoei.getType() == null) {
				return true;
			}
			for (Entry<Enchantment, Integer> entry : map.entrySet()) {
				if (entry.getKey().equals(qoei.getType())) {
					int level = entry.getValue();
					switch (qoei.getCompare()) {
					case BIGGEREQUAL:
						return level >= qoei.getLevel();
					case SMALLEREQUAL:
						return level <= qoei.getLevel();
					case EQUAL:
						return level == qoei.getLevel();
					case BIGGER:
						return level > qoei.getLevel();
					case SMALLER:
						return level < qoei.getLevel();
					default:
						return false;
					}

				}
			}
		}
		return false;
	}

	public void enchantItem(ItemStack stack, Map<Enchantment, Integer> enchants) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkEnchants(qop, stack, enchants))
					.collect(Collectors.toList()).forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkAnvil(QuestObjectProgress qop, AnvilInventory ai, ItemStack holding) {
		if (!(qop.getObject() instanceof QuestObjectUseAnvil) || qop.isFinished()) {
			return false;
		}
		QuestObjectUseAnvil qoua = (QuestObjectUseAnvil) qop.getObject();
		//Bukkit.getLogger().info(ai.getContents().length + "");
		// ItemStack original = qoua.getResult();
		ItemStack newStack = qoua.getResult().clone();
		if (((Repairable) holding.getItemMeta()).hasRepairCost()) {
			Repairable repairable = (Repairable) newStack.getItemMeta();
			repairable.setRepairCost(((Repairable) holding.getItemMeta()).getRepairCost());
			newStack.setItemMeta((ItemMeta) repairable);
		}
		// qoua.setResult(newStack);
		// newStack.getItemMeta().toString().equals(holding.getItemMeta().toString())
		if ((newStack.isSimilar(holding) || ConfigSettings.USE_WEAK_ITEM_CHECK)
				&& (QuestValidater.weakItemCheck(newStack, holding, true) || !ConfigSettings.USE_WEAK_ITEM_CHECK)) {
			ItemStack original1 = null;
			if (qoua.getFirst() != null) {
				original1 = qoua.getFirst().clone();
				if (ai.getItem(0) != null && ((Repairable) ai.getItem(0).getItemMeta()).hasRepairCost()) {
					Repairable repairable1 = (Repairable) original1.getItemMeta();
					repairable1.setRepairCost(((Repairable) ai.getItem(0).getItemMeta()).getRepairCost());
					original1.setItemMeta((ItemMeta) repairable1);
				}

			}
			ItemStack original2 = null;
			if (qoua.getSecond() != null) {
				original2 = qoua.getSecond().clone();
				if (ai.getItem(1) != null && ((Repairable) ai.getItem(1).getItemMeta()).hasRepairCost()) {
					Repairable repairable2 = (Repairable) original2.getItemMeta();
					repairable2.setRepairCost(((Repairable) ai.getItem(1).getItemMeta()).getRepairCost());
					original2.setItemMeta((ItemMeta) repairable2);
				}
			}
			
			
			if (Main.getInstance().mcCompat.getDurability(newStack) < qoua
					.getDurabilityThreshold()) {
				//Bukkit.getLogger().info("rejected 2 durability ");
				return false;
			}
			// original1.getItemMeta().toString().equals(ai.getItem(0).getItemMeta().toString()
			if (qoua.getFirst() != null && ai.getItem(0) != null) {
				if (ConfigSettings.USE_WEAK_ITEM_CHECK) {
					if (!QuestValidater.weakItemCheck(original1, ai.getItem(0), true)) {
						return false;
					}
				} else {
					if (!ai.getItem(0).isSimilar(original1)) {
						// Bukkit.getLogger().info("rejected 0");
						return false;
					}
				}
				// Bukkit.getLogger().info(original1.getItemMeta().toString());
				// Bukkit.getLogger().info(ai.getItem(0).getItemMeta().toString());
				// Bukkit.getLogger().info("rejected 0");
				//Bukkit.getLogger().info(original1.isSimilar(ai.getItem(0)) + "");
				// owner.getInventory().addItem(ai.getItem(0),qoua.getFirst().clone());

			}
			// original2.getItemMeta().toString().equals(ai.getItem(1).getItemMeta().toString())
			if (qoua.getSecond() != null && ai.getItem(1) != null) {
				if (ConfigSettings.USE_WEAK_ITEM_CHECK) {
					if (!QuestValidater.weakItemCheck(original2, ai.getItem(1), true)) {
						return false;
					}
				} else {
					if (!original2.isSimilar(ai.getItem(1))) {
						return false;
					}
				}
				// Bukkit.getLogger().info("rejected 1");
				//Bukkit.getLogger().info(original2.getItemMeta().toString());
				//Bukkit.getLogger().info(ai.getItem(1).getItemMeta().toString());
				//Bukkit.getLogger().info(original2.isSimilar(ai.getItem(1)) + "");
				// owner.getInventory().addItem(ai.getItem(1),qoua.getSecond().clone());
			}
			return true;
		} else {
			//Bukkit.getLogger().info("rejected 2");
			// Bukkit.getLogger().info(qoua.getResult().getItemMeta().toString());
			// Bukkit.getLogger().info(holding.getItemMeta().toString());
			return false;
		}
		// return false;
	}

	public void useAnvil(AnvilInventory ai, ItemStack holding) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkAnvil(qop, ai, holding)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void fillBucket(ItemStack stack, Material type) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkFill(qop, stack, type)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void command(String message) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkCommand(qop, message)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkChat(QuestObjectProgress qop, String message) {
		if (!(qop.getObject() instanceof QuestObjectPlayerChat) || qop.isFinished()) {
			return false;
		}
		QuestObjectPlayerChat qopc = (QuestObjectPlayerChat) qop.getObject();
		message = message.trim();
		if (qopc.isUseRegex()) {
			Pattern pattern = Pattern.compile(qopc.getFilter());
			Matcher matcher = pattern.matcher(message);
			if (matcher.matches()) {
				return true;
			}
		} else {
			if (message.equals(qopc.getFilter())) {
				return true;
			}
		}
		return false;
	}

	public void chat(String message) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkChat(qop, message)).collect(Collectors.toList())
					.forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void login() {
		DebugHandler.log(5, "%s started login", this.getPlayer());
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> progresses = new ArrayList<>();

		for (QuestProgress qp : currentQuests) {
			int index = 0;
			Map<Integer, QuestObjectProgress> toUpdate = new HashMap<>();
			for (QuestObjectProgress qop : qp.getCurrentObjects()) {
				if (!checkLogin(qop)) {
					continue;
				}
				DebugHandler.log(5, "%s loginserver qop check", this.getPlayer());
				AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
				ref.set(new Pair<>(qp, qop));
				boolean intervalCheck = false;

				if (qop.getLastInvokedMilli() < 0) {
					qop.setLastInvokedMilli(System.currentTimeMillis());
					DebugHandler.log(5, "player %s lastinvoked = -1,setting new time", this.getPlayer());
					// intervalCheck = true;
				} else if (System.currentTimeMillis()
						- qop.getLastInvokedMilli() > ((QuestObjectLoginServer) qop.getObject()).getInterval() * 1000) {
					DebugHandler.log(5, "player %s Login Objective progress check success since bigger than interval",
							this.getPlayer());
					intervalCheck = true;
					qop.setLastInvokedMilli(System.currentTimeMillis());
				} else {
					long difference = System.currentTimeMillis() - qop.getLastInvokedMilli();
					long expected = ((QuestObjectLoginServer) qop.getObject()).getInterval() * 1000;
					QuestChatManager.info(owner, I18n.locMsg(owner, "QuestObject.LoginServer.OnCoolDown",
							TimeHandler.convertTime(expected - difference), qp.getQuest().getQuestName()));
					DebugHandler.log(5, "%s logged in with unsuccessful interval check", this.getPlayer());
				}
				toUpdate.put(index, qop);
				index++;
				if (intervalCheck) {
					progresses.add(ref);

				}
			}
			List<QuestObjectProgress> objList = qp.getCurrentObjects();
			for (Entry<Integer, QuestObjectProgress> entry : toUpdate.entrySet()) {
				objList.set(entry.getKey(), entry.getValue());
			}
			qp.setCurrentObjects(objList);
			this.currentQuests.add(qp);
		}
		
		if (progresses.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : progresses) {
			if (any.get() != null) {
				objectSuccess(any.get().getKey(), any.get().getValue());
			}
		}
	}

	private boolean checkMoveDistance(QuestObjectProgress qop, Player p, Double distance) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectMoveDistance)) {
			return false;
		}
		QuestObjectMoveDistance qomd = ((QuestObjectMoveDistance) qop.getObject());
		switch (qomd.getMethod()) {
		case HORSE: {
			if (p.isInsideVehicle() && p.getVehicle() instanceof Horse) {
				if (qomd.getCustomName() != null && !qomd.getCustomName().isEmpty()) {
					if (((LivingEntity) p.getVehicle()).getCustomName().equals(qomd.getCustomName())) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
			break;
		}
		case BOAT: {
			if (p.isInsideVehicle() && p.getVehicle() instanceof Boat) {
				return true;
			}

			break;
		}
		case PIG: {
			if (p.isInsideVehicle()) {
				if (p.isInsideVehicle() && p.getVehicle() instanceof Pig) {
					if (qomd.getCustomName() != null && !qomd.getCustomName().isEmpty()) {
						if (((LivingEntity) p.getVehicle()).getCustomName().equals(qomd.getCustomName())) {
							return true;
						} else {
							return false;
						}
					} else {
						return true;
					}
				}
			}
			break;
		}
		case MYTHIC_MOB: {
			if (!p.isInsideVehicle() || p.getVehicle() == null) {
				return false;
			}
			if (qomd.getMtmMob() != null && Main.getHooker().hasMythicMobEnabled()) {
				if (Main.getHooker().getMythicMobsAPI().isEntityMythic(p.getVehicle())) {
					return qomd.getMtmMob()
							.equals(Main.getHooker().getMythicMobsAPI().getMythicInternalName(p.getVehicle()));
				}
			} else {
				return false;
				// EntityType et = qomd.getGenericMobType();
				// if(p.isInsideVehicle()&&p.getVehicle().getType()==et) {
				// if(qomd.getCustomName()!=null&&!qomd.getCustomName().isEmpty()) {
				// if(((LivingEntity)p.getVehicle()).getCustomName().equals(qomd.getCustomName()))
				// {
				// return true;
				// }else {
				// return false;
				// }
				// }else {
				// return true;
				// }
				// }
				// if(p.isInsideVehicle()&& p.getVehicle().getType() == et)
			}
			break;
		}
		case MINECART: {
			if (p.isInsideVehicle() && p.getVehicle() instanceof Minecart) {
				return true;
			}
			break;
		}
		case SWIMMING: {
			if (!p.isInsideVehicle() && p.getLocation().getBlock().getType() == Material.WATER) {
				if (isSwimming) {
					return true;
				}
			}
			break;
		}

		case ELYTRA: {
			if(!ConfigSettings.MC_1_8) {
			if (!p.isInsideVehicle() && p.isGliding()) {
				return true;
			}
			}
			break;
		}
		case FOOT: {
			if (!p.isInsideVehicle() && !p.isFlying()) {
				return true;
			}
		}

		}
		return false;
	}

	public void moveDistance(Player p, Double distance) {
		//currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter((qp)-> !qp.isIncompatibleMode());
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkMoveDistance(qop, p, distance))
					.collect(Collectors.toList()).forEach(qop -> {
						AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
						ref.set(new Pair<>(qp, qop));
						list.add(ref);
					});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				// QuestObjectMoveDistance qop = (QuestObjectMoveDistance)
				// any.get().getValue().getObject();
				objectSuccess(pair.getKey(), pair.getValue(), distance);
			}
		}
	}

	public void regenerateHealth(double d) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkRegen(qop)).collect(Collectors.toList()).forEach(qop -> {
				AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
				ref.set(new Pair<>(qp, qop));
				list.add(ref);
			});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue(), d);
			}
		}
	}

	public void shearSheep() {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach((qp) -> {
			qp.getCurrentObjects().stream().filter(qop -> checkShear(qop)).collect(Collectors.toList()).forEach(qop -> {
				AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
				ref.set(new Pair<>(qp, qop));
				list.add(ref);
			});
		});
		if (list.isEmpty()) {
			return;
		}
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void breakBlock(Material m) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> qp.getCurrentObjects()
				.stream().filter(qop -> checkBlock(qop, m)).collect(Collectors.toList()).forEach(qop -> {
					AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
					ref.set(new Pair<>(qp, qop));
					list.add(ref);
				}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkLogin(QuestObjectProgress qop) {
		if (!(qop.getObject() instanceof QuestObjectLoginServer) || qop.isFinished()) {
			return false;
		}
		return true;
	}

	private boolean checkBlock(QuestObjectProgress qop, Material m) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectBreakBlock))
			return false;
		return ((QuestObjectBreakBlock) qop.getObject()).getType() == m;
	}

	private boolean checkShear(QuestObjectProgress qop) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectShearSheep)) {
			return false;
		}
		return true;
	}

	private boolean checkRegen(QuestObjectProgress qop) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectRegeneration)) {
			return false;
		}
		return true;
	}

	public void talkToNPC(NPC npc) {
		AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any = new AtomicReference<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> {
			return checkPlayerInWorld(qp.getQuest());
		}).forEach(qp -> {
			Optional<QuestObjectProgress> obj = qp.getCurrentObjects().stream().filter(qop -> {
				return checkNPC(qop, npc);
			}).findFirst();
			obj.ifPresent(qop -> any.set(new Pair<>(qp, qop)));
		});
		if (any.get() != null) {
			Pair<QuestProgress, QuestObjectProgress> pair = any.get();
			checkFinished(pair.getKey(), pair.getValue());
			DebugHandler.log(5, "[Listener] Player " + owner.getName() + " talked  npc %s. ", npc.getId() + "");
		}
	}

	public void finishTalkToNPCObjective(NPC npc, String conv) {
		AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any = new AtomicReference<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> {
			return checkPlayerInWorld(qp.getQuest());
		}).forEach(qp -> {
			Optional<QuestObjectProgress> obj = qp.getCurrentObjects().stream().filter(qop -> {
				return checkNPC(qop, npc);
			}).findFirst();
			obj.ifPresent(qop -> any.set(new Pair<>(qp, qop)));
		});
		if (any.get() != null) {
			Pair<QuestProgress, QuestObjectProgress> pair = any.get();
			if (((QuestObjectTalkToNPC) pair.getValue().getObject()).getConversation().getInternalID().equals(conv)) {
				DebugHandler.log(5, "[Listener] %s finished conv %s in crtain quest", owner.getName(), conv);
				pair.getValue().setProgress(1);
				pair.getValue().finish();
				QuestProgress qp = pair.getKey();
				SimpleQuestObject o = pair.getValue().getObject();
				QuestChatManager.info(owner,
						I18n.locMsg(owner, "QuestJourney.ProgressText", qp.getQuest().getQuestName())
								+ o.toDisplayText(owner) + " " + I18n.locMsg(owner, "CommandInfo.Finished"));
				DebugHandler.log(2, "[Listener] Player " + owner.getName() + "'s quest " + qp.getQuest().getQuestName()
						+ " finished an object of " + o.getConfigString());
				pair.getKey().checkIfNextStage();
			} else {
				DebugHandler.log(5, "[Listener] conv is %s",
						((QuestObjectTalkToNPC) pair.getValue().getObject()).getConversation().getInternalID());
			}
			// checkFinished(pair.getKey(), pair.getValue());
			// DebugHandler.log(5, "[Listener] Player " + owner.getName() + " talked npc %s.
			// ",npc.getId()+"");
		}
	}

	private boolean checkNPC(QuestObjectProgress qop, NPC npc) {
		// for(StackTraceElement s:Thread.currentThread().getStackTrace()) {
		// System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "("
		// + s.getFileName() + ":" + s.getLineNumber() + ")");
		// }
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectTalkToNPC))
			return false;
		// DebugHandler.log(5, "checking npc %s: target=%s",((QuestObjectTalkToNPC)
		// qop.getObject()).getTargetNPC().getId(),npc.getId());
		return ((QuestObjectTalkToNPC) qop.getObject()).getTargetNPC().getId() == npc.getId();
	}

	// Checks whether the player has submitted a correct item
	// Returns true if submitted at least 1 item.
	private boolean checkItem(QuestObjectProgress qop, NPC npc) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectDeliverItem))
			return false;
		QuestObjectDeliverItem o = (QuestObjectDeliverItem) qop.getObject();
		ItemStack itemToDeliver = Main.getInstance().mcCompat.getItemInMainHand(owner);
		int amountNeeded = o.getAmount() - qop.getProgress();
		DebugHandler.log(5, "[Listener] Checking item submission...");
		if (o.getTargetNPC().equals(npc)) {
			boolean bukkitSimilar = o.getItem().isSimilar(itemToDeliver);
			boolean weakItemCheck = QuestValidater.weakItemCheck(itemToDeliver, o.getItem(), true);
			DebugHandler.log(5, "[Listener] NPC check PASSED.");
			DebugHandler.log(5, "[Listener] Bukkit similarity = " + bukkitSimilar);
			DebugHandler.log(5, "[Listener] Weak itemCheck = " + weakItemCheck);
			if (bukkitSimilar || (ConfigSettings.USE_WEAK_ITEM_CHECK && weakItemCheck)) {
				DebugHandler.log(5, "[Listener] Item similarity check PASSED.");
				if (itemToDeliver.getAmount() > amountNeeded) {
					itemToDeliver.setAmount(itemToDeliver.getAmount() - amountNeeded);
					qop.setProgress(o.getAmount());
				} else {
					Main.getInstance().mcCompat.setItemInMainHand(owner, null);
					qop.setProgress(itemToDeliver.getAmount() == amountNeeded ? o.getAmount()
							: qop.getProgress() + itemToDeliver.getAmount());
				}
				return true;
			}
			DebugHandler.log(5, "[Listener] Failed due to the item submitted is not correct.");
			return false;
		}
		DebugHandler.log(5,
				"[Listener] NPC not correct. Required " + o.getTargetNPC().getId() + " but get " + npc.getId());
		return false;
	}

	public boolean deliverItem(NPC npc) {
		AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any = new AtomicReference<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> {
			Optional<QuestObjectProgress> obj = qp.getCurrentObjects().stream().filter(qop -> checkItem(qop, npc))
					.findFirst();
			obj.ifPresent(qop -> any.set(new Pair<>(qp, qop)));
		});
		if (any.get() != null) {
			Pair<QuestProgress, QuestObjectProgress> pair = any.get();
			checkFinished(pair.getKey(), pair.getValue());
			DebugHandler.log(5,
					"[Listener] Player " + owner.getName() + " handed in one or more quest-requiring item(s).");
			return true;
		} else {
			DebugHandler.log(5, "[Listener] Player " + owner.getName() + " did not hand in any quest-requiring items.");
			return false;
		}
	}

	private boolean checkTable(QuestObjectProgress qop, CraftingInventory inv) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectCraftItem)) {
			DebugHandler.log(5,"rejected because not an instance of craftItem");
			return false;
		}
		QuestObjectCraftItem qoci = (QuestObjectCraftItem) qop.getObject();
		for (int i = 1; i <= 9; i++) {
			if(qoci.getCraftingRequirement()[i-1]==null||qoci.getCraftingRequirement()[i-1].getType() == Material.AIR) {
				if(inv.getItem(i)!=null && inv.getItem(i).getType()!=Material.AIR){
					DebugHandler.log(5,"crafting slot "+i+" has sth while requirement is null");
					return false;
				}
				continue;
			}
			if(inv.getItem(i)==null||inv.getItem(i).getType() == Material.AIR) {
				DebugHandler.log(5,"crafting slot "+i+" is null or air");
				return false;
			}
			if (!(inv.getItem(i).isSimilar(qoci.getCraftingRequirement()[i-1]) || (ConfigSettings.USE_WEAK_ITEM_CHECK
					&& QuestValidater.weakItemCheck(inv.getItem(i), qoci.getCraftingRequirement()[i-1], false)))) {
				DebugHandler.log(5,"crafting slot"+i+" does not match craftReq");
				return false;
			}
		}
		if (qoci.getResult() != null && qoci.getResult().getType() != Material.AIR) {
			if (inv.getItem(0) == null||inv.getItem(0).getType() == Material.AIR) {
				DebugHandler.log(5,"crafting result is null");
				return false;
			}
			if (!(inv.getItem(0).isSimilar(qoci.getResult()) || (ConfigSettings.USE_WEAK_ITEM_CHECK
					&& QuestValidater.weakItemCheck(inv.getItem(0), qoci.getResult(), false)))) {
				DebugHandler.log(5,"crafting result does not match required resultant");
				return false;
			}
		}
		return true;
	}

	public boolean craftItem(CraftingInventory inv) {
		DebugHandler.log(5,"[Listener] Plyer" +owner.getName()+" have crafted an item (unchecked)");
		AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any = new AtomicReference<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> {
			Optional<QuestObjectProgress> obj = qp.getCurrentObjects().stream().filter(qop -> checkTable(qop, inv))
					.findFirst();
			obj.ifPresent(qop -> any.set(new Pair<>(qp, qop)));
		});
		if (any.get() != null) {
			Pair<QuestProgress, QuestObjectProgress> pair = any.get();
			objectSuccess(pair.getKey(), pair.getValue());
			DebugHandler.log(5, "[Listener] Player " + owner.getName() + " crafted the quest required item.");
			return true;
		} else {
			DebugHandler.log(5, "[Listener] Player " + owner.getName() + " did not craft any quest-required item.");
			return false;
		}
	}

	private boolean checkMob(QuestObjectProgress qop, Entity e) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectKillMob))
			return false;
		QuestObjectKillMob o = (QuestObjectKillMob) qop.getObject();
		if (!e.getType().equals(o.getType()))
			return false;
		if (o.hasCustomName())
			return e.getCustomName() != null && e.getCustomName().equals(o.getCustomName());
		return true;
	}

	public void killMob(Entity e) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> qp.getCurrentObjects()
				.stream().filter(qop -> checkMob(qop, e)).collect(Collectors.toList()).forEach(qop -> {
					AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
					ref.set(new Pair<>(qp, qop));
					list.add(ref);
				}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void catchFish() {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest()))
				.forEach(qp -> qp.getCurrentObjects().stream()
						.filter(qop -> qop.getObject() instanceof QuestObjectFishing && !qop.isFinished())
						.collect(Collectors.toList()).forEach(qop -> {
							AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
							ref.set(new Pair<>(qp, qop));
							list.add(ref);
						}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	public void killMythicMob(String mtmMob) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> qp.getCurrentObjects()
				.stream().filter(qop -> checkMythicMob(qop, mtmMob)).collect(Collectors.toList()).forEach(qop -> {
					AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
					ref.set(new Pair<>(qp, qop));
					list.add(ref);
				}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkMythicMob(QuestObjectProgress qop, String mtmMob) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectKillMob))
			return false;
		QuestObjectKillMob o = (QuestObjectKillMob) qop.getObject();
		if (!o.isMythicObject())
			return false;
		return o.getMythicMob().equals(mtmMob);
	}

	public void consumeItem(ItemStack is) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> qp.getCurrentObjects()
				.stream().filter(qop -> checkConsume(qop, is)).collect(Collectors.toList()).forEach(qop -> {
					AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
					ref.set(new Pair<>(qp, qop));
					list.add(ref);
				}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkConsume(QuestObjectProgress qop, ItemStack is) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectConsumeItem))
			return false;
		QuestObjectConsumeItem o = (QuestObjectConsumeItem) qop.getObject();
		return o.getItem().isSimilar(is)
				|| (ConfigSettings.USE_WEAK_ITEM_CHECK && QuestValidater.weakItemCheck(is, o.getItem(), false));
	}

	public void reachLocation(Location l) {
		ArrayList<AtomicReference<Pair<QuestProgress, QuestObjectProgress>>> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> checkPlayerInWorld(qp.getQuest())).forEach(qp -> qp.getCurrentObjects()
				.stream().filter(qop -> checkLocation(qop, l)).collect(Collectors.toList()).forEach(qop -> {
					AtomicReference<Pair<QuestProgress, QuestObjectProgress>> ref = new AtomicReference<>();
					ref.set(new Pair<>(qp, qop));
					list.add(ref);
				}));
		if (list.isEmpty())
			return;
		for (AtomicReference<Pair<QuestProgress, QuestObjectProgress>> any : list) {
			if (any.get() != null) {
				Pair<QuestProgress, QuestObjectProgress> pair = any.get();
				objectSuccess(pair.getKey(), pair.getValue());
			}
		}
	}

	private boolean checkLocation(QuestObjectProgress qop, Location l) {
		if (qop.isFinished() || !(qop.getObject() instanceof QuestObjectReachLocation))
			return false;
		QuestObjectReachLocation o = (QuestObjectReachLocation) qop.getObject();
		if (l.getWorld().getName().equals(o.getLocation().getWorld().getName()))
			if (l.getX() < (o.getLocation().getX() + o.getRadius())
					&& l.getX() > (o.getLocation().getX() - o.getRadius()))
				if (l.getY() < (o.getLocation().getY() + o.getRadius())
						&& l.getY() > (o.getLocation().getY() - o.getRadius()))
					return l.getZ() < (o.getLocation().getZ() + o.getRadius())
							&& l.getZ() > (o.getLocation().getZ() - o.getRadius());
		return false;
	}

	public void removeProgress(Quest q) {
		currentQuests.removeIf(qp -> QuestValidater.weakValidate(q, qp.getQuest()));

		// currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode()).filter(qp -> QuestValidater.weakValidate(q,
		// qp.getQuest())).findFirst().ifPresent(qp -> currentQuests.remove(qp));
	}

	public Set<QuestFinishData> getFinishQuests() {
		return finishedQuests;
	}

	public String getQuestDisplayFormat(Quest q) {
		if (canTake(q, false)) {
			if (hasFinished(q))
				return I18n.locMsg(owner, "QuestGUI.RedoableQuestSymbol").replaceAll("§0", "§f") + ChatColor.BOLD
						+ q.getQuestName();
			else
				return I18n.locMsg(owner, "QuestGUI.NewQuestSymbol").replaceAll("§0", "§f") + ChatColor.BOLD
						+ q.getQuestName();
		} else {
			for (QuestObjectProgress op : getProgress(q).getCurrentObjects()) {
				if (op.getObject() instanceof QuestObjectTalkToNPC && !op.isFinished())
					return I18n.locMsg(owner, "QuestGUI.QuestReturnSymbol").replaceAll("§0", "§f") + ChatColor.BOLD
							+ q.getQuestName();
			}
			return I18n.locMsg(owner, "QuestGUI.QuestDoingSymbol").replaceAll("§0", "§f") + ChatColor.BOLD
					+ q.getQuestName();
		}
	}

	public QuestFinishData getFinishData(Quest q) {
		if (!hasFinished(q))
			return null;
		for (QuestFinishData qd : finishedQuests) {
			if(!(qd instanceof IncompatibleQuestFinishData) &&qd.getQuest() == null) {
				DebugHandler.log(0, I18n.locMsg(null, "CmdLog.NoValidQuest","not available quest","{loading null quest finished data}"));
				continue;
			}
			
			if(qd instanceof IncompatibleQuestFinishData) {
				if(((IncompatibleQuestFinishData)qd).getIncompatQuest().equals(q.getInternalID())) {
					return qd;
				}else {
					continue;
				}
			}
			
			if (qd.getQuest().getInternalID().equals(q.getInternalID()))
				return qd;
		}
		return null;
	}

	public void addFinishedQuest(Quest q, boolean reward) {
		if (hasFinished(q)) {
			getFinishData(q).finish();
			return;
		}
		finishedQuests.add(new QuestFinishData(owner, q, 1, System.currentTimeMillis(), reward));
	}

	public boolean hasTakenReward(Quest q) {
		if (!hasFinished(q))
			return false;
		return getFinishData(q).isRewardTaken();
	}

	public void claimReward(Quest q) {
		getFinishData(q).setRewardTaken(true);
		save();
	}

	public void checkUnclaimedReward() {
		for (QuestFinishData data : finishedQuests) {
			if (!data.isRewardTaken()) {
				Quest q = data.getQuest();
				if (!q.isCommandQuest())
					QuestChatManager.info(owner,
							I18n.locMsg(owner, "QuestReward.RewardUnclaimed", q.getQuestNPC().getName()));
			}
		}
	}

	public boolean isCurrentlyDoing(Quest q) {
		for (QuestProgress qp : currentQuests) {
			if (QuestValidater.weakValidate(qp.getQuest(), q))
				return true;
		}
		return false;
	}

	public boolean canTake(Quest q, boolean sendMsg) {
		if (isCurrentlyDoing(q)) {
			if (sendMsg)
				QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.AlreadyTaken"));
			return false;
		}
		//override mode
		//&& sendMsg
		if(AdminCommand.overrideMode.contains(owner.getUniqueId().toString())) {
			return true;
		}
		if (q.usePermission() && !owner.hasPermission("MangoQuest.takeQuest." + q.getInternalID())) {
			if (sendMsg)
				QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.NoPermTakeQuest"));
			DebugHandler.log(3, q.getInternalID()+" failed perm");
			return false;
		}

		if (q.hasRequirement()) {
			Optional<String> failedRequirements = RequirementManager.meetRequirementWith(owner, q.getRequirements(),
					false);
			if (failedRequirements.isPresent()) {
				DebugHandler.log(3, q.getInternalID()+" failed requirements");
				if (sendMsg)
					QuestChatManager.info(owner, failedRequirements.get());
				return false;
			}
			DebugHandler.log(3, q.getInternalID()+" passed requirements");
		}

		if (hasFinished(q)) {
			long lastFinishTime = getFinishData(q).getLastFinish();
			switch (q.getRedoSetting()) {
			case ONCE_ONLY:
				if (sendMsg)
					QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.NotRedoable"));
				return false;
			case COOLDOWN:
				long d = getDelay(lastFinishTime, q.getRedoDelay());
				if (d > 0) {
					if (sendMsg)
						QuestChatManager.info(owner,
								I18n.locMsg(owner, "CommandInfo.QuestCooldown", TimeHandler.convertTime(d)));
					return false;
				}
				break;
			case DAILY:
				if (!TimeHandler.canTakeDaily(lastFinishTime, q.getResetHour())) {
					if (sendMsg)
						QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.QuestCooldown", TimeHandler
								.convertTime(TimeHandler.getDailyCooldown(lastFinishTime, q.getResetHour()))));
					return false;
				}
				break;
			case WEEKLY:
				if (!TimeHandler.canTakeWeekly(lastFinishTime, q.getResetDay(), q.getResetHour())) {
					if (sendMsg)
						QuestChatManager.info(owner,
								I18n.locMsg(owner, "CommandInfo.QuestCooldown", TimeHandler.convertTime(TimeHandler
										.getWeeklyCooldown(lastFinishTime, q.getResetDay(), q.getResetHour()))));
					return false;
				}
				break;
			}
			//DebugHandler.log(5, q.getInternalID()+" cooldown");
			if (!hasTakenReward(q)) {
				//DebugHandler.log(5, q.getInternalID()+" passed reward");
				DebugHandler.log(3, q.getInternalID()+" has not taken rewards");
				if (sendMsg)
					QuestChatManager.info(owner, I18n.locMsg(owner, "QuestReward.RewardNotTaken"));
				return false;
			}
		}
		return true;
	}

	public boolean isTakeableCooldown(Quest q) {

		Boolean sendMsg = false;
		if (getFinishData(q) == null) {
			return false;
		}
		long lastFinishTime = getFinishData(q).getLastFinish();

		switch (q.getRedoSetting()) {
		case ONCE_ONLY:
			if (sendMsg)
				QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.NotRedoable"));
			return false;
		case COOLDOWN:
			long d = getDelay(lastFinishTime, q.getRedoDelay());
			if (d > 0) {
				if (sendMsg)
					QuestChatManager.info(owner,
							I18n.locMsg(owner, "CommandInfo.QuestCooldown", TimeHandler.convertTime(d)));
				return false;
			}
			break;
		case DAILY:
			if (!TimeHandler.canTakeDaily(lastFinishTime, q.getResetHour())) {
				if (sendMsg)
					QuestChatManager.info(owner, I18n.locMsg(owner, "CommandInfo.QuestCooldown",
							TimeHandler.convertTime(TimeHandler.getDailyCooldown(lastFinishTime, q.getResetHour()))));
				return false;
			}
			break;
		case WEEKLY:
			if (!TimeHandler.canTakeWeekly(lastFinishTime, q.getResetDay(), q.getResetHour())) {
				if (sendMsg)
					QuestChatManager.info(owner,
							I18n.locMsg(owner, "CommandInfo.QuestCooldown", TimeHandler.convertTime(
									TimeHandler.getWeeklyCooldown(lastFinishTime, q.getResetDay(), q.getResetHour()))));
				return false;
			}
			break;
		}
		// if (!hasTakenReward(q)) {
		// if (sendMsg)
		// QuestChatManager.info(owner, I18n.locMsg(owner,
		// "QuestReward.RewardNotTaken"));
		// return false;
		// }
		return true;
	}

	// should be deprecated in the coming update...
	// not performance wise
	public void checkQuestFail() {
		ArrayList<Quest> list = new ArrayList<>();
		currentQuests.stream().filter((qp)-> !qp.isIncompatibleMode())
				.filter(qp -> qp.getQuest().isTimeLimited()
						&& System.currentTimeMillis() > qp.getQuest().getTimeLimit() + qp.getTakeTime())
				.forEach(qp -> list.add(qp.getQuest()));
		for (Quest q : list) {
			forceQuit(q, false);
			QuestChatManager.info(owner, I18n.locMsg(owner, "QuestJourney.QuestFailed", q.getQuestName()));
			DebugHandler.log(3, "[Listener] Player " + owner.getName() + " failed quest " + q.getQuestName()
					+ " because time is due.");
		}
		List<QuestProgress> questToFail = new ArrayList<>();
		for (QuestProgress idk : this.getProgresses()) {
			Quest q = idk.getQuest();

			if (RequirementManager.checkServerTimeFail(owner, q.getWorldLimit(), q.getRequirements(), false)
					.isPresent()) {
				questToFail.add(idk);
			}
		}
		for (QuestProgress idk : questToFail) {
			Quest q = idk.getQuest();
			QuestChatManager.info(owner, I18n.locMsg(owner, "QuestJourney.QuestFailed"));
			forceQuit(q, false);
		}
	}

	public long getDelay(long last, long quest) {
		return quest - (System.currentTimeMillis() - last);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void checkFinished(QuestProgress qp, QuestObjectProgress qop) {
		SimpleQuestObject o = qop.getObject();
		qop.checkIfFinished();
		if (qop.isFinished()) {
			QuestChatManager.info(owner, I18n.locMsg(owner, "QuestJourney.ProgressText", qp.getQuest().getQuestName())
					+ o.toDisplayText(owner) + " " + I18n.locMsg(owner, "CommandInfo.Finished"));
			DebugHandler.log(2, "[Listener] Player " + owner.getName() + "'s quest " + qp.getQuest().getQuestName()
					+ " finished an object of " + qop.getObject().getConfigString());
	
			qp.checkIfNextStage();
			Bukkit.getPluginManager().callEvent(new QuestObjectProgressEvent(this, qp.getQuest(), qop.getObject()));
		} else {
			if (o instanceof NumerableObject)
				QuestChatManager.info(owner,
						I18n.locMsg(owner, "QuestJourney.ProgressText", qp.getQuest().getQuestName())
								+ o.toDisplayText(owner) + " "
								+ I18n.locMsg(owner, "CommandInfo.Progress", Integer.toString(qop.getProgress()),
										Integer.toString(((NumerableObject) o).getAmount())));
			else if (o instanceof QuestObjectReachLocation) {
				if (qop.getProgress() >= 1) {
					qop.finish();
					checkFinished(qp, qop);
				}
			} else if (o instanceof DecimalObject) {
				// not gonna display the progress because it will be spammy
				// QuestChatManager.info(owner,
				// I18n.locMsg(owner, "QuestJourney.ProgressText", qp.getQuest().getQuestName())
				// + o.toDisplayText() + " "
				// + I18n.locMsg(owner, "CommandInfo.Progress",
				// Double.toString(qop.getProgressD()),
				// Double.toString(((DecimalObject) o).getAmount())));
			} else if (o instanceof CustomQuestObject) {
				QuestChatManager.info(owner,
						I18n.locMsg(owner, "QuestJourney.ProgressText", qp.getQuest().getQuestName())
								+ ((CustomQuestObject) o).getProgressText(qop));
			} else if (o instanceof QuestObjectTalkToNPC) {
				if (qop.getObject().hasConversation()) {
					qop.openConversation(owner);
					DebugHandler.log(2, "[Listener] Player " + owner.getName() + "'s quest "
							+ qp.getQuest().getQuestName() + " has unfinished conversation.");

				} else {
					DebugHandler.log(2, "[Listener] Player " + owner.getName() + "'s quest "
							+ qp.getQuest().getQuestName() + " has finished objective talktonpc");
					qop.setProgress(1);
					qop.finish();
					checkFinished(qp, qop);
				}
			}
		}
	}

	public Entity getLastTamed() {
		return lastTamed;
	}

	public void setLastTamed(Entity lastTamed) {
		this.lastTamed = lastTamed;
	}

	public void setLastLocation(Location to) {
		this.lastLocation = to.clone();
	}

	public Location getLastLocation() {
		return this.lastLocation;
	}

	public List<Quest> getRedoableQuests() {
		return redoableQuests;
	}

	public void setRedoableQuests(List<Quest> redoableQuests) {
		this.redoableQuests = redoableQuests;
	}

}
