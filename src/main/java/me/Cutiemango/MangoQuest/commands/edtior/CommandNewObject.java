package me.Cutiemango.MangoQuest.commands.edtior;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

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
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.QuestStage;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject.TriggerObjectType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreakBlock;

public class CommandNewObject
{

	public static void execute(Quest q, Player sender, String[] args) {
		if (!QuestEditorManager.checkEditorMode(sender, true))
			return;
		switch (args[2]) {
			case "req":
				addRequirements(q, sender, args);
				break;
			case "evt":
				addEvent(q, sender, args);
				break;
			case "stage":
				addStage(q, sender);
				break;
			case "object":
				addObject(q, sender, args);
				break;
			case "reward":
				addReward(q, sender, args);
				break;
		}
	}

	// /mq e addnew evt [triggertype] [stage] [index] [triggerobject]
	private static void addEvent(Quest q, Player sender, String[] args) {
		if (args.length < 6)
			return;
		TriggerType type = TriggerType.valueOf(args[3]);
		int stage = Integer.parseInt(args[4]);
		int index = Integer.parseInt(args[5]);
		if (args.length == 6) {
			QuestEditorManager.selectTriggerObjType(sender, type, stage, index);
			return;
		}
		TriggerObjectType obj = TriggerObjectType.valueOf(args[6]);
		if (args.length == 7) {
			String cmd = "mq e addnew evt " + type.toString() + " " + stage + " " + index + " " + obj.toString();
			if (obj == TriggerObjectType.SEND_TITLE_AND_SUBTITLE) {
				EditorListenerHandler.register(sender,
						new EditorListenerObject(ListeningType.STRING, cmd, Syntax.of("S%S", I18n.locMsg(sender,"Syntax.TitleAndSubtitle"), "%")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.TitleAndSubtitle"));
				return;
			} else if (obj == TriggerObjectType.TELEPORT) {
				EditorListenerHandler.register(sender,
						new EditorListenerObject(ListeningType.STRING, cmd, Syntax.of("S:D:D:D", I18n.locMsg(sender,"Syntax.Teleport"), ":")));
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.Teleport"));
				return;
			}else if(obj == TriggerObjectType.DISCORD_SRV) {
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.DiscordSRVMessage"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, cmd, null, (arg)->{
					if(Main.getInstance().configManager.getDiscordSRVMessages().getConfig().isConfigurationSection("messages."+arg)) {
						return true;
					}
					return false;
				}));
				return;
			}
			EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, cmd, null));
			QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterValue"));
		} else {
			String s = QuestUtil.convertArgsString(args, 7);
			if (s.equalsIgnoreCase("cancel")) {
				QuestEditorManager.editQuestTrigger(sender, type, stage);
				return;
			}
			List<TriggerObject> list = q.getTriggerMap().containsKey(type) ? q.getTriggerMap().get(type) : new ArrayList<>();
			list.add(index, new TriggerObject(obj, s, stage));
			q.getTriggerMap().put(type, list);
			QuestEditorManager.editQuestTrigger(sender, type, stage);
		}
	}

	// /mq e addnew req [type]
	@SuppressWarnings("unchecked")
	private static void addRequirements(Quest q, Player sender, String[] args) {
		RequirementType t = RequirementType.valueOf(args[3]);
		switch (t) {
			case QUEST:
				if (args.length == 5) {
					if (QuestUtil.getQuest(args[4]) != null) {
						((List<String>) q.getRequirements().get(t)).add(args[4]);
						QuestEditorManager.editQuestRequirement(sender);
					}
				} else if (args.length == 4)
					QuestEditorManager.selectQuest(sender, "/mq e addnew req QUEST");

				break;
			case FRIEND_POINT:
				QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.FriendPoint"));
				EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, "mq e edit req FRIEND_POINT",
						Syntax.of("N:D", I18n.locMsg(sender,"Syntax.FriendPoint"), ":")));
				break;
			case PERMISSION:
				if (args.length == 5) {
					((List<String>) q.getRequirements().get(t)).add(args[4]);
					QuestEditorManager.editQuestRequirement(sender);
				} else if (args.length == 4) {
					QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.Permission"));
					EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, "mq e addnew req PERMISSION", null));
				}
				break;
			case PLACEHOLDER_API:
				if(args.length == 4) {
					QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.PlaceholderAPI"));
					EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, "mq e addnew req PLACEHOLDER_API", null,(idk)->{
						if(idk.split(" ").length == 2) {
							return true;
						}
						return false;
					}));
					break;
				}else if(args.length == 6) {
					if(q.getRequirements().get(RequirementType.PLACEHOLDER_API)==null) {
						q.getRequirements().put(RequirementType.PLACEHOLDER_API, new HashMap<String,String>());
					}
					
					((Map<String,String>)q.getRequirements().get(t)).put(args[4], args[5]);
					QuestEditorManager.editQuestRequirement(sender);
					break;
				}
			case MCMMO_LEVEL:
				if(args.length == 4) {
					QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender, "EditorMessage.mcMMO"));
					EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING,"mq e addnew req MCMMO_LEVEL",null,(getarg)->{
						if(getarg.split(" ").length == 2) {
							String[] getargs = getarg.split(" ");
							if(ExperienceAPI.isValidSkillType(getargs[0])) {
								try {
									 Integer.parseInt(getargs[1]);
									return true;
								}catch(NumberFormatException e) {
									e.printStackTrace();
									return false;
								}
							}else {
								return false;
							}
						}
						return false;
					}));
					break;
				}else if(args.length == 6) {
					((Map<PrimarySkillType,Integer>)q.getRequirements().get(t)).put(mcMMO.p.getSkillTools().matchSkill(args[4].toUpperCase()), Integer.parseInt(args[5]));
					QuestEditorManager.editQuestRequirement(sender);
					break;
				}
		}
	}

	// /mq e addnew stage
	private static void addStage(Quest q, Player sender) {
		List<SimpleQuestObject> l = new ArrayList<>();
		l.add(new QuestObjectBreakBlock(Material.GRASS, 1));
		q.getStages().add(q.getStages().size(), new QuestStage(l));
		QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.StageCreated"));
		QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.StageCreated2"));
		QuestChatManager.info(sender, I18n.locMsg(sender,"EditorMessage.StageCreated3"));
		QuestEditorManager.editQuestStages(sender);
	}

	// /mq e addnew object [stage]
	private static void addObject(Quest q, Player sender, String[] args) {
		if (args.length == 4) {
			int stage;
			try {
				stage = Integer.parseInt(args[3]);
			}
			catch (NumberFormatException e) {
				QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
				return;
			}
			q.getStage(stage - 1).getObjects().add(q.getStage(stage - 1).getObjects().size(), new QuestObjectBreakBlock(Material.GRASS, 1));
			QuestEditorManager.selectObjectType(sender, stage, q.getStage(stage - 1).getObjects().size());
		}
	}

	// /mq e addnew reward item
	// /mq e addnew reward fp [npc] [value]
	// /mq e addnew reward command [value] ...
	private static void addReward(Quest q, Player sender, String[] args) {
		if (args.length == 4) {
			switch (args[3].toLowerCase()) {
				case "fp":
					EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, "mq e addnew reward fp",
							Syntax.of("N:D", I18n.locMsg(sender,"Syntax.FriendPoint"), ":")));
					QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.FriendPoint"));
					return;
				case "command":
					EditorListenerHandler.register(sender, new EditorListenerObject(ListeningType.STRING, "mq e addnew reward command", null));
					QuestBookGUIManager.openInfo(sender, I18n.locMsg(sender,"EditorMessage.EnterCommand"));
			}
		} else if (args.length >= 5) {
			switch (args[3]) {
				case "fp":
					String[] sp = args[4].split(":");
					try {
						q.getQuestReward().getFriendPointMap().put(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
					}
					catch (NumberFormatException e) {
						QuestChatManager.error(sender, I18n.locMsg(sender,"EditorMessage.WrongFormat"));
						QuestEditorManager.editQuest(sender);
						return;
					}
					QuestEditorManager.editQuest(sender);
					return;
				case "command":
					String s = QuestUtil.convertArgsString(args, 4);
					if (!s.equals(""))
						q.getQuestReward().addCommand(s);
					QuestEditorManager.editQuest(sender);
					return;
			}
		}
	}

}
