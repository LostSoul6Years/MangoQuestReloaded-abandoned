package me.Cutiemango.MangoQuest.commands.edtior;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sucy.skill.SkillAPI;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.Syntax;
import me.Cutiemango.MangoQuest.editor.EditorListenerHandler;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject.ListeningType;
import me.Cutiemango.MangoQuest.editor.QuestEditorManager;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestRewardManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.objects.reward.QuestReward;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject.TriggerObjectType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreakBlock;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreedMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBucketFill;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectConsumeItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectCraftItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectDeliverItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnchantItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnterCommand;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectFishing;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectKillMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLaunchProjectile;
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
import net.citizensnpcs.api.CitizensAPI;

public class CommandEditQuest {

	// Command: /mq e edit args[2] args[3]
	public static void execute(Quest q, Player sender, String[] args) {
		if (!QuestEditorManager.checkEditorMode(sender, true))
			return;
		if (args.length == 4 && args[3].equals("cancel")) {
			QuestEditorManager.editQuest(sender);
			return;
		}
		switch (args[2]) {
		case "limit":
		case "timelimit":
		case "vis":
		case "quit":
		case "redo":
		case "redodelay":
		case "resethour":
		case "resetday":
		case "world":
		case "perm":
			CommandEditSetting.execute(q, sender, args);
			break;
		case "name":
			editName(q, sender, args);
			break;
		case "outline":
			editOutline(q, sender, args);
			break;
		case "npc":
			editNPC(q, sender, args);
			break;
		case "req":
			editRequirements(q, sender, args);
			break;
		case "evt":
			editTrigger(q, sender, args);
			break;
		case "stage":
			editStage(sender, args);
			break;
		case "object":
			editObject(q, sender, args);
			break;
		case "reward":
			editReward(q, sender, args);
			break;
		}
	}

	private static void editName(Quest q, Player sender, String[] args) {
		if (args.length == 3) {

			EditorListenerHandler.register(sender,
					new EditorListenerObject(ListeningType.STRING, "mq e edit name", null));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterValue"));
		} else if (args.length >= 4) {
			q.setQuestName(args[3]);
			QuestEditorManager.editQuest(sender);
		}
	}

	private static void editOutline(Quest q, Player sender, String[] args) {
		if (args.length == 3) {
			EditorListenerHandler.register(sender,
					new EditorListenerObject(ListeningType.STRING, "mq e edit outline", null));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.Outline"));
		} else if (args.length >= 4) {
			int line;
			try {
				line = Integer.parseInt(args[3]) - 1;
			} catch (NumberFormatException e) {
				QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
				QuestEditorManager.editQuest(sender);
				return;
			}
			if (line < 0) {
				QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
				QuestEditorManager.editQuest(sender);
				return;
			}
			String s = QuestUtil.convertArgsString(args, 4);
			if (q.getQuestOutline().size() - 1 < line)
				q.getQuestOutline().add(line, s);
			else
				q.getQuestOutline().set(line, s);
			QuestEditorManager.editQuest(sender);
		}
	}

	// unfinished
	// /mq e edit req [type] ([index]) [obj]
	@SuppressWarnings("unchecked")
	private static void editRequirements(Quest q, Player sender, String[] args) {
		if (args.length == 3) {
			QuestEditorManager.editQuestRequirement(sender);
			return;
		}
		RequirementType t = RequirementType.valueOf(args[3]);

		if (args.length >= 6) {
			if (args[5].equalsIgnoreCase("cancel")) {
				QuestEditorManager.editQuestRequirement(sender);
				return;
			}
			// unfinished

			// special 6 args or more input for JJCDeveloper's newly added 3 requirements
			if (t == RequirementType.SERVER_TIME || t == RequirementType.WORLD_TIME) {
				switch (t) {
				case SERVER_TIME: {
					String startTimeRaw = args[4];
					String endTimeRaw = args[5];
					if (toValidTime(startTimeRaw) != null || toValidTime(endTimeRaw) != null) {
						q.getRequirements().put(RequirementType.SERVER_TIME,
								Arrays.asList(toValidTime(startTimeRaw), toValidTime(endTimeRaw)));
					} else {
						QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
						QuestEditorManager.editQuest(sender);
						return;
					}
					break;
				}
				case WORLD_TIME: {
					String startTimeRaw = args[4];
					String endTimeRaw = args[5];
					try {
						q.getRequirements().put(RequirementType.WORLD_TIME,
								Arrays.asList(Long.parseLong(startTimeRaw), Long.parseLong(endTimeRaw)));
					} catch (NumberFormatException e) {
						QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
						QuestEditorManager.editQuest(sender);
						return;
					}
					break;
				}

				}
			} else if (t == RequirementType.QUEST) {
				if (QuestUtil.getQuest(args[5]) != null) {
					if (((List<String>) q.getRequirements().get(t)).contains(args[5])) {
						QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.ObjectExist"));
						((List<String>) q.getRequirements().get(t)).remove(Integer.parseInt(args[4]));
					}
				} else
					QuestChatManager.error(sender, I18n.locMsg(sender,"CommandInfo.QuestNotFound"));
			}
			QuestEditorManager.editQuestRequirement(sender);
		} else if (args.length == 5) {
			switch (t) {
			case LEVEL:
			case SKILLAPI_LEVEL:
			case QRPG_LEVEL:
				q.getRequirements().put(t, Integer.parseInt(args[4]));
				break;
			case MONEY:
				q.getRequirements().put(t, Double.parseDouble(args[4]));
				break;
			case PERMISSION:
			case ITEM:
				break;
			case QUEST:
				QuestEditorManager.selectQuest(sender,
						"/mq e edit req " + t.toString() + " " + Integer.parseInt(args[4]));
				return;
			case FRIEND_POINT:
				String[] sp = args[4].split(":");
				try {
					((HashMap<Integer, Integer>) q.getRequirements().get(t)).put(Integer.parseInt(sp[0]),
							Integer.parseInt(sp[1]));
				} catch (NumberFormatException e) {
					QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
					QuestEditorManager.editQuest(sender);
					return;
				}
				QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.FriendPointRegistered", sp[0], sp[1]));
				break;
			case ALLOW_DESCENDANT:
				q.getRequirements().put(t, Boolean.parseBoolean(args[4]));
				break;
			case QRPG_CLASS:
				if (args[4].equalsIgnoreCase("none") || Main.getHooker().getQuantumRPG().getModuleCache()
						.getClassManager().getClassById(args[4]) != null)
					q.getRequirements().put(t, args[4]);
				else
					QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.RPGClassNotValid", args[4]));
				break;
			case SKILLAPI_CLASS:
				if (SkillAPI.isClassRegistered(args[4]))
					q.getRequirements().put(t, args[4]);
				else
					QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.RPGClassNotValid", args[4]));
				break;
			default:
				break;
			}
			QuestEditorManager.editQuestRequirement(sender);
		} else {
			switch (t) {
			case LEVEL:
			case MONEY:
			case SKILLAPI_CLASS:
			case SKILLAPI_LEVEL:
			case QRPG_CLASS:
			case QRPG_LEVEL:
				EditorListenerHandler.register(sender,
						new EditorListenerObject(ListeningType.STRING, "mq e edit req " + t.toString(), null));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterValue"));
				break;
			case ALLOW_DESCENDANT:
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,
						"mq e edit req " + t.toString(), Syntax.of("B", I18n.locMsg(sender,"Syntax.Boolean"))));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterValue"));
				break;
			case FRIEND_POINT:
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.FriendPoint"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,
						"mq e edit req FRIEND_POINT", Syntax.of("N:D", I18n.locMsg(sender,"Syntax.FriendPoint"), ":")));
				break;
			case ITEM:
				EditorListenerHandler.registerGUI(sender, "requirement");
				break;
			// unfinished
			case SERVER_TIME:
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.ServerTime"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,"mq e edit req SERVER_TIME", null,(idk)->{
					if(idk.split(" ").length == 2||idk.contains("cancel")) {
						return true;
					}
					return false;
				}));
				break;
			case WORLD_TIME:
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.WorldTime"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,"mq e edit req WORLD_TIME", null,(idk)->{
					if(idk.split(" ").length == 2||idk.contains("cancel")) {
						return true;
					}
					return false;
				}));
				break;
			case PLACEHOLDER_API:
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.PlaceholderAPI"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,"mq e edit req PLACEHOLDER_API", null,(idk)->{
					if(idk.split(" ").length == 2||idk.contains("cancel")) {
						return true;
					}
					
					return false;
				}));
				break;
			default:
				break;
			}
		}
	}

	private static void editNPC(Quest q, Player sender, String[] args) {
		if (args.length == 3) {
			EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.NPC_LEFT_CLICK,
					"mq e edit npc", Syntax.of("N", I18n.locMsg(sender,"Syntax.NPCID"), "")));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.ClickNPC"));
		} else if (args.length == 4) {
			if (args[3].equalsIgnoreCase("-1")) {
				q.setQuestNPC(null);
				QuestEditorManager.editQuest(sender);
				QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.NPCRemoved"));
				return;
			}
			q.setQuestNPC(Main.getHooker().getNPC(args[3]));
			QuestEditorManager.editQuest(sender);
		}
	}

	// Command: /mq e edit evt [triggertype] [stage] [index] [objtype] [obj]
	private static void editTrigger(Quest q, Player sender, String[] args) {
		if (args.length == 3) {
			QuestEditorManager.selectTriggerType(sender, "edit");
			return;
		} else if (args.length == 4) {
			TriggerType type = TriggerType.valueOf(args[3]);
			if (type == TriggerType.TRIGGER_STAGE_FINISH || type == TriggerType.TRIGGER_STAGE_START)
				QuestEditorManager.selectTriggerStage(sender, "edit", TriggerType.valueOf(args[3]));
			else
				QuestEditorManager.editQuestTrigger(sender, TriggerType.valueOf(args[3]), -1);
			return;
		} else if (args.length == 5) {
			QuestEditorManager.editQuestTrigger(sender, TriggerType.valueOf(args[3]), Integer.parseInt(args[4]));
			return;
		}
		TriggerType type = TriggerType.valueOf(args[3]);
		int stage = Integer.parseInt(args[4]);
		int index = Integer.parseInt(args[5]);
		TriggerObjectType objtype = TriggerObjectType.valueOf(args[6]);
		if (args.length >= 8) {
			String s = QuestUtil.convertArgsString(args, 7);
			if (s.equalsIgnoreCase("cancel")) {
				QuestEditorManager.editQuestTrigger(sender, type, stage);
				return;
			}
			List<TriggerObject> list = q.getTriggerMap().containsKey(type) ? q.getTriggerMap().get(type)
					: new ArrayList<>();
			if (index == list.size())
				list.add(new TriggerObject(objtype, s, stage));
			else
				list.set(index, new TriggerObject(objtype, s, stage));
			q.getTriggerMap().put(type, list);
			QuestEditorManager.editQuestTrigger(sender, type, stage);
		} else {
			String cmd = "mq e edit evt " + type.toString() + " " + stage + " " + index + " " + objtype.toString();
			if (objtype == TriggerObjectType.SEND_TITLE_AND_SUBTITLE) {
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, cmd,
						Syntax.of("S%S", I18n.locMsg(sender,"Syntax.TitleAndSubtitle"), "%")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.TitleAndSubtitle"));
				return;
			} else if (objtype == TriggerObjectType.TELEPORT) {
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, cmd,
						Syntax.of("S:D:D:D", I18n.locMsg(sender,"Syntax.Teleport"), ":")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.Teleport"));
				return;
			}
			EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, cmd, null));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterValue"));
		}
	}

	private static void editStage(Player sender, String[] args) {
		if (args.length == 3) {
			QuestEditorManager.editQuestStages(sender);
		} else if (args.length == 4) {
			int stage = 1;
			try {
				stage = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
			}
			QuestEditorManager.editQuestObjects(sender, stage);
		}
	}

	// /mq e edit object [stage] [objcount] [obj] [content]...
	private static void editObject(Quest q, Player sender, String[] args) {
		if (args.length <= 4) {
			QuestEditorManager.editQuestStages(sender);
			return;
		}
		int stage;
		int obj;
		try {
			stage = Integer.parseInt(args[3]);
			obj = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
			return;
		}
		switch (args.length) {
		case 5:
			QuestEditorManager.editQuestObject(sender, stage, obj);
			return;
		case 6:
			if (args[5].equalsIgnoreCase("type")) {
				QuestEditorManager.selectObjectType(sender, stage, obj);
				return;
			}
			SimpleQuestObject qobj = q.getStage(stage - 1).getObject(obj - 1);
			if (!(qobj instanceof EditorObject))
				return;
			EditorListenerObject eobj = ((EditorObject) qobj).createCommandOutput(sender,
					"mq e edit object " + stage + " " + obj + " " + args[5], args[5]);
			if (eobj == null)
				return;
			EditorListenerHandler.register(sender, eobj);
			return;
		case 7:
			SimpleQuestObject o = q.getStage(stage - 1).getObject(obj - 1);
			if (args[6].equalsIgnoreCase("cancel"))
				break;
			else if (args[5].equalsIgnoreCase("type")) {
				SimpleQuestObject ob;
				switch (args[6].toUpperCase()) {
				case "ENTER_COMMAND":
					ob = new QuestObjectEnterCommand();
					break;
				case "SLEEP":
					ob = new QuestObjectSleep();
					break;
				case "BREED_MOB":
					ob = new QuestObjectBreedMob();
					break;
				case "BUCKET_FILL":
					ob = new QuestObjectBucketFill();
					break;
				case "ENCHANT_ITEM":
					ob = new QuestObjectEnchantItem();
					break;
				case "TAME_MOB":
					ob = new QuestObjectTameMob();
					break;
				case "USE_ANVIL":
					ob = new QuestObjectUseAnvil();
					break;
				case "PLAYER_CHAT":
					ob = new QuestObjectPlayerChat();
					break;
				case "LAUNCH_PROJECTILE":
					ob = new QuestObjectLaunchProjectile();
					break;
				case "LOGIN_SERVER":
					ob = new QuestObjectLoginServer();
					break;
				case "SHEAR_SHEEP":
					ob = new QuestObjectShearSheep();
					break;
				case "REGENERATION":
					ob = new QuestObjectRegeneration();
					break;
				case "MOVE_DISTANCE":
					ob = new QuestObjectMoveDistance();
					break;
				case "BREAK_BLOCK":
					ob = new QuestObjectBreakBlock(Material.STONE, 1);
					break;
				case "CONSUME_ITEM":
					ob = new QuestObjectConsumeItem(new ItemStack(Material.BREAD), 1);
					break;
				case "DELIVER_ITEM":
					ob = new QuestObjectDeliverItem(CitizensAPI.getNPCRegistry().getById(0),
							new ItemStack(Material.APPLE), 1);
					break;
				case "KILL_MOB":
					ob = new QuestObjectKillMob(EntityType.ZOMBIE, 1, null);
					break;
				case "REACH_LOCATION":
					ob = new QuestObjectReachLocation(new Location(Bukkit.getWorld("world"), 0, 0, 0), 0,
							I18n.locMsg(sender,"EditorMessage.DefaultLocation"));
					break;
				case "TALK_TO_NPC":
					ob = new QuestObjectTalkToNPC(Main.getHooker().getNPC(0));
					break;
				case "FISHING":
					ob = new QuestObjectFishing(1);
					break;
				case "CRAFT_ITEM":
					ob = new QuestObjectCraftItem();
					break;
				case "PLACEHOLDER_API":
					ob = new QuestObjectPlaceholderAPI();
					break;
				default:
					return;
				}
				q.getStage(stage - 1).getObjects().set(obj - 1, ob);
				QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.ChangeObject"));
				break;
			}
			if (o instanceof EditorObject && !((EditorObject) o).receiveCommandInput(sender, args[5], args[6])) {
				QuestChatManager.error(sender, I18n.locMsg(sender,"CommandInfo.InvalidArgument", args[6]));
				break;
			}
		}
		QuestEditorManager.editQuestObject(sender, stage, obj);
	}

	// /mq e edit reward [type] [value]
	private static void editReward(Quest q, Player sender, String[] args) {
		QuestReward reward = q.getQuestReward();
		if (args.length == 4) {
			switch (args[3].toLowerCase()) {
			case "money":
				EditorListenerHandler.register(sender,
						new EditorListenerObject(ListeningType.STRING, "mq e edit reward " + args[3], null));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.MoneyAmount"));
				break;
			case "choiceamount":
			case "exp":
				// skillapiexp
			case "saexp":
				// qrpgexp
			case "qrpgexp":
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,
						"mq e edit reward " + args[3], Syntax.of("I", I18n.locMsg(sender,"Syntax.Number"), "")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.ExpAmount"));
				break;
			case "npc":
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.NPC_LEFT_CLICK,
						"mq e edit reward " + args[3], Syntax.of("N", I18n.locMsg(sender,"Syntax.NPCID"), "")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.ClickNPC"));
				return;
			case "item":
				QuestRewardManager.openEditMainGUI(sender);
				return;
			case "fp":
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.FriendPoint"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,
						"mq e edit reward fp", Syntax.of("N:D", I18n.locMsg(sender,"Syntax.FriendPoint"), ":")));
				return;
			default:
				QuestEditorManager.editQuest(sender);
			}
		} else if (args.length == 5) {
			switch (args[3].toLowerCase()) {
			case "money":
				double money;
				try {
					money = Double.parseDouble(args[4]);
				} catch (NumberFormatException e) {
					QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
					break;
				}
				reward.setMoney(money);
				break;
			case "exp":
				reward.setExp(Integer.parseInt(args[4]));
				break;
			case "saexp":
				reward.setSkillAPIExp(Integer.parseInt(args[4]));
				break;
			case "qrpgexp":
				reward.setQRPGExp(Integer.parseInt(args[4]));
				break;
			case "npc":
				if (!QuestValidater.validateNPC(args[4]))
					return;
				reward.setRewardNPC(Main.getHooker().getNPC(args[4]));
				QuestRewardManager.openEditMainGUI(sender);
				return;
			case "choiceamount":
				int i = Integer.parseInt(args[4]);
				if (i > q.getQuestReward().getChoiceAmount())
					QuestChatManager.error(sender, I18n.locMsg(sender,"QuestReward.TooManyChoices"));
				reward.setRewardAmount(i);
				QuestRewardManager.openEditMainGUI(sender);
				return;
			case "fp":
				String[] sp = args[4].split(":");
				try {
					reward.getFriendPointMap().put(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
				} catch (NumberFormatException e) {
					QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
					QuestEditorManager.editQuest(sender);
					return;
				}
				break;
			case "command":
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterCommand"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,
						"mq e edit reward command " + Integer.parseInt(args[4]), null));
				return;
			}
			QuestEditorManager.editQuest(sender);
		} else if (args.length >= 6) {
			if (args[3].equalsIgnoreCase("command")) {
				String cmd = QuestUtil.convertArgsString(args, 5);
				int index = Integer.parseInt(args[4]);
				if (!cmd.equals(""))
					reward.getCommands().set(index, cmd);
			}
			QuestEditorManager.editQuest(sender);
		}
	}

	private static Long toValidTime(String i) {
		if (i.indexOf('T') != -1) {
			String yearDay = i.substring(0, i.indexOf('T'));
			String Time = i.substring(i.indexOf('T') + 1);
			String[] yearDays = yearDay.split("-");
			boolean error = false;
			if (yearDays[0].length() != 4 || yearDays[1].length() != 2 || yearDays[2].length() != 2) {
				error = true;
			}
			String[] Times = Time.split(":");
			if (Times[0].length() != 2 || Times[1].length() != 2 || Times[2].length() != 2) {

				error = true;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setLenient(false);
			String finalised = i.replaceAll("T", " ");
			if (!error) {
				try {
					return sdf.parse(finalised).getTime();
				} catch (ParseException e1) {
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
	}

}
