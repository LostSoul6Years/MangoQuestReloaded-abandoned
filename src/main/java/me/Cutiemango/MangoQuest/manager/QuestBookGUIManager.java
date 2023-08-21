package me.Cutiemango.MangoQuest.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.book.FlexibleBook;
import me.Cutiemango.MangoQuest.book.InteractiveText;
import me.Cutiemango.MangoQuest.book.QuestBookPage;
import me.Cutiemango.MangoQuest.commands.AdminCommand;
import me.Cutiemango.MangoQuest.conversation.FriendConversation;
import me.Cutiemango.MangoQuest.conversation.QuestChoice.Choice;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.GUIOption;
import me.Cutiemango.MangoQuest.objects.QuestStage;
import me.Cutiemango.MangoQuest.objects.reward.RewardChoice;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class QuestBookGUIManager {

	public static void openGUIWithProgress(Player p, QuestProgress qp) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p, "QuestEditor.QuestName", qp.getQuest().getQuestName()));
		book.changeLine();

		// NPC
		if (!qp.getQuest().isCommandQuest()) {
			NPC npc = qp.getQuest().getQuestNPC();
			book.add(I18n.locMsg(p, "QuestEditor.QuestNPC"));
			book.add(new InteractiveText(p,"").showNPCInfo(npc)).changeLine();
			book.changeLine();
		}

		// Objects
		book.add(I18n.locMsg(p, "QuestEditor.QuestObjects")).changeLine();
		for (int i = 0; i < qp.getQuest().getStages().size(); i++) {
			QuestStage stage = qp.getQuest().getStage(i);
			for (int k = 0; k < stage.getObjects().size(); k++) {
				SimpleQuestObject obj = stage.getObjects().get(k);

				if (qp.getCurrentStage() == i && !qp.getCurrentObjects().get(k).isFinished()) {
					QuestObjectProgress qop = qp.getCurrentObjects().get(k);
					DebugHandler.log(5, "i dont even know bro");
					DebugHandler.log(5, ComponentSerializer.toString(obj.toTextComponent(p,false)));
					book.add(obj.toTextComponent(p,false));
					if (obj instanceof NumerableObject)
						book.add(" &8(" + qop.getProgress() + "/" + ((NumerableObject) obj).getAmount() + ")");
					book.changeLine();
				} else if (qp.getCurrentStage() < i) {
					//for (int j = 0; j < qp.getQuest().getStage(i).getObjects().size(); j++) {
						book.add(I18n.locMsg(p, "QuestJourney.NotRevealed")).changeLine();
					//}
				}
				else
					book.add(obj.toTextComponent(p,true)).changeLine();
			}
		}
		if (QuestUtil.getData(p).canTake(qp.getQuest(), false))
			book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.TakeButton"))
					.showText(I18n.locMsg(p, "QuestGUI.Hover.TakeQuest", qp.getQuest().getQuestName()))
					.clickCommand("/mq q take " + qp.getQuest().getInternalID()));
		book.changeLine();

		// OutLine
		book.createNewPage();
		book.add(I18n.locMsg(p, "QuestEditor.Outline")).changeLine();
		for (String out : qp.getQuest().getQuestOutline())
			book.add(out).changeLine();

		// Reward
		book.createNewPage();
		book.add(I18n.locMsg(p, "QuestEditor.Reward")).changeLine();

		if (qp.getQuest().getQuestReward().hasItem()) {
			if (qp.getQuest().getQuestReward().hasMultipleChoices()) {
				book.add(I18n.locMsg(p, "QuestReward.SelectReward",
						Integer.toString(qp.getQuest().getQuestReward().getChoiceAmount()),
						Integer.toString(qp.getQuest().getQuestReward().getRewardAmount()))).changeLine();
				for (RewardChoice choice : qp.getQuest().getQuestReward().getChoices()) {
					book.add("- ");
					List<ItemStack> items = choice.getItems();
					for (int j = 0; j < items.size(); j++)
						book.add(new InteractiveText(p,items.get(j))).add(j == items.size() - 1 ? "" : ", ");
					book.changeLine();
				}
			} else {
				for (ItemStack is : qp.getQuest().getQuestReward().getDefaultChoice().getItems()) {
					if (is != null) {
						book.add(new InteractiveText(p,is));
						book.add(" ");
						book.add(I18n.locMsg(p, "QuestReward.RewardAmount", Integer.toString(is.getAmount())))
								.changeLine();
					}
				}
			}
		}

		if (qp.getQuest().getQuestReward().hasMoney())
			book.add(I18n.locMsg(p, "QuestEditor.RewardMoney",
					Double.toString(QuestUtil.cut(qp.getQuest().getQuestReward().getMoney())))).changeLine();

		if (qp.getQuest().getQuestReward().hasExp())
			book.add(I18n.locMsg(p, "QuestEditor.RewardExp", Integer.toString(qp.getQuest().getQuestReward().getExp())))
					.changeLine();
		
		openBook(p, book.toSendableBook());
	}

	public static void openBook(Player p, QuestBookPage... qp) {
		List<TextComponent> list = new ArrayList<>();
		for (QuestBookPage page : qp)
			list.add(page.getOriginalPage());

		openBook(p, list.toArray(new TextComponent[0]));
	}

	public static void openChoice(Player p, TextComponent q, List<Choice> c) {
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p, "Conversation.ChoiceTitle")).changeLine();
		book.add(q).changeLine();
		for (int i = 0; i < c.size(); i++) {
			if (!QuestUtil.getData(p).meetFriendPointReq(c.get(i)))
				continue;
			book.add(new InteractiveText(p,(i + 1) + ". " + c.get(i).getContent()).clickCommand("/mq conv choose " + i))
					.changeLine();
		}
		openBook(p, book.toSendableBook());
	}

	public static void openJourneyMenu(Player p) {
		FlexibleBook book = new FlexibleBook();
		book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.QuestList")));
		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.add(new InteractiveText(p,"     " + I18n.locMsg(p, "QuestJourney.QuestProgress"))
				.clickCommand("/mq quest list progress"));
		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.add(new InteractiveText(p,"     " + I18n.locMsg(p, "QuestJourney.QuestToTake"))
				.clickCommand("/mq quest list doable"));
		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.changeLine();
		book.add(new InteractiveText(p,"     " + I18n.locMsg(p, "QuestJourney.QuestFinished"))
				.clickCommand("/mq quest list finished"));
		book.changeLine();
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void openProgressJourney(Player p) {
		QuestPlayerData qd = QuestUtil.getData(p);
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p, "QuestJourney.QuestProgress")).changeLine();
		for (QuestProgress qp : qd.getProgresses()) {
			if (!qp.getQuest().getSettings().displayOnProgress())
				continue;
			book.changeLine();
			if (qp.getQuest().isQuitable())
				book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.QuitButton"))
						.clickCommand("/mq quest quit " + qp.getQuest().getInternalID()));
			book.add(new InteractiveText(p,"").showQuest(qp.getQuest()));
			book.add(":");
			book.changeLine();
			if (qp.getQuest().isTimeLimited()) {
				long timeleft = (qp.getTakeTime() + qp.getQuest().getTimeLimit()) - System.currentTimeMillis();
				book.add(
						new InteractiveText(p,I18n.locMsg(p, "QuestJourney.TimeLeft", TimeHandler.convertTime(timeleft))))
						.changeLine();
			}
			for (QuestObjectProgress qop : qp.getCurrentObjects()) {
				book.add("- ");
				if (qop.isFinished())
					book.add(qop.getObject().toTextComponent(p,true)).changeLine();
				else {
					book.add(qop.getObject().toTextComponent(p,false));
					if (qop.getObject() instanceof NumerableObject)
						book.add(" &8(" + qop.getProgress() + "/" + ((NumerableObject) qop.getObject()).getAmount()
								+ ")");
					book.changeLine();
				}
			}
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p, "QuestEditor.Return")).clickCommand("/mq q list")).changeLine();
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void openDoableJourney(Player p) {
		QuestPlayerData qd = QuestUtil.getData(p);
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p, "QuestJourney.QuestToTake")).changeLine();
		for (Quest q : QuestStorage.localQuests.values()) {
			if (q.getSettings().displayOnTake() && qd.canTake(q, false)) {
				book.add("- ");
				book.add(new InteractiveText(p,"").showQuest(q));
				if (q.isCommandQuest())
					book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.TakeButton"))
							.clickCommand("/mq quest take " + q.getInternalID()));
				book.changeLine();
			}
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p, "QuestEditor.Return")).clickCommand("/mq q list")).changeLine();
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void openFinishedJourney(Player p) {
		QuestPlayerData qd = QuestUtil.getData(p);
		FlexibleBook book = new FlexibleBook();
		book.add(I18n.locMsg(p, "QuestJourney.QuestFinished")).changeLine();
		for (QuestFinishData qfd : qd.getFinishQuests()) {
			if (!qfd.getQuest().getSettings().displayOnFinish())
				continue;
			if (!qfd.isRewardTaken() && qfd.getQuest().isCommandQuest()) {
				book.add(I18n.locMsg(p, "QuestGUI.NewQuestSymbol"));
				book.add(new InteractiveText(p,"").showQuest(qfd.getQuest()));
				book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.RewardButton"))
						.clickCommand("/mq q reward select " + qfd.getQuest().getInternalID())
						.showText(I18n.locMsg(p, "QuestGUI.Hover.ClaimReward")));
				book.changeLine();
			} else {
				book.add("- ");
				book.add(new InteractiveText(p,"").showQuest(qfd.getQuest()));
				book.add(": ");
				book.add(I18n.locMsg(p, "QuestJourney.FinishedTimes", Integer.toString(qfd.getFinishedTimes())))
						.changeLine();
			}
		}
		book.changeLine();
		book.add(new InteractiveText(p,I18n.locMsg(p, "QuestEditor.Return")).clickCommand("/mq q list")).changeLine();
		QuestBookGUIManager.openBook(p, book.toSendableBook());
	}

	public static void openInfo(Player p, String msg) {
		QuestBookPage p1 = new QuestBookPage();
		p1.add(msg).changeLine();
		p1.add(I18n.locMsg(p, "EditorMessage.EnterCancel")).changeLine();
		openBook(p, p1);
	}

	public static void openBook(Player p, TextComponent... texts) {
		Main.getInstance().handler.openBook(p, texts);
	}

	public static void openQuitGUI(Player p, Quest q) {
		QuestBookPage page = new QuestBookPage();
		page.add(I18n.locMsg(p, "QuestQuitMsg.Title")).changeLine();
		page.add(I18n.locMsg(p, "QuestQuitMsg.WarnAccept", q.getQuestName())).changeLine();
		page.add(I18n.locMsg(p, "QuestQuitMsg.WarnAccept2")).changeLine();
		page.changeLine();
		page.add(new InteractiveText(p,I18n.locMsg(p, "QuestQuitMsg.QuitQuest") + q.getQuitAcceptMsg(p))
				.clickCommand("/mq q cquit " + q.getInternalID())).changeLine();
		page.changeLine();
		page.add(new InteractiveText(p,I18n.locMsg(p, "QuestQuitMsg.Cancel") + q.getQuitCancelMsg(p))
				.clickCommand("/mq q list")).changeLine();

		openBook(p, page);
	}

	public static void openNPCInfo(Player p, NPC npc, boolean trade) {
		QuestPlayerData qd = QuestUtil.getData(p);
		FlexibleBook book = new FlexibleBook();
		List<String> holder = new ArrayList<>();

		// Message
		book.add(I18n.locMsg(p, "QuestJourney.NPCFriendMessage", me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc),
				QuestNPCManager.getNPCMessage(p,npc.getId(), qd.getNPCfp(npc.getId())))).changeLine();
		book.changeLine();

		// Interaction List
		book.add(I18n.locMsg(p, "QuestGUI.InteractionList")).changeLine();

		Map<NPCInfoType, List<Function<Boolean, InteractiveText>>> actionMap = new EnumMap<>(NPCInfoType.class);
		for (NPCInfoType infoType : NPCInfoType.values()) {
			actionMap.put(infoType, new ArrayList<>());
		}
		// List<GUIOption> options = new ArrayList<>();
		// Show GUIOptions
		for (GUIOption option : QuestNPCManager.getNPCData(npc.getId()).getOptions()) {
			if (!option.meetRequirementWith(p))
				continue;
			actionMap.get(NPCInfoType.GUIOptions).add(new Function<Boolean, InteractiveText>() {
				@Override
				public InteractiveText apply(Boolean b) {
					InteractiveText info = option.toInteractiveText(npc);
					if(!b) {
						return info;
					}
					book.add(info).changeLine();
					return info;
				}
			});

			// book.add(option.toInteractiveText(npc)).changeLine();

		}

		// Trade Options
		if (trade) {
			actionMap.get(NPCInfoType.Trade).add(new Function<Boolean, InteractiveText>() {

				@Override
				public InteractiveText apply(Boolean b) {
					InteractiveText info = new InteractiveText(p,I18n.locMsg(p, "QuestGUI.Trade"))
							.clickCommand("/mq quest trade " + npc.getId());
					if(!b) {
						return info;
					}
					book.add(info).changeLine();
					return info;
				}

			});
		}

		// List<QuestProgress> progresses = new ArrayList<>();

		for (QuestProgress q : qd.getNPCtoTalkWith(npc)) {
			actionMap.get(NPCInfoType.Progresses).add(new Function<Boolean, InteractiveText>() {

				@Override
				public InteractiveText apply(Boolean b) {
					InteractiveText questName = new InteractiveText(p,"").showQuest(q.getQuest());
					if(!b) {
						return questName;
					}
					book.add(I18n.locMsg(p, "QuestGUI.QuestReturnSymbol"));
					book.add(questName);
					book.add(new InteractiveText(p,I18n.locMsg(p, "QuestGUI.Conversation"))
							.clickCommand("/mq conv npc " + npc.getId())
							.showText(I18n.locMsg(p, "QuestGUI.Hover.ClickToChat")));
					if (q.getQuest().isQuitable())
						if (qd.isCurrentlyDoing(q.getQuest()) && !q.getQuest().isCommandQuest()
								&& q.getQuest().getQuestNPC().equals(npc)) {
							book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.QuitButton"))
									.clickCommand("/mq quest quit " + q.getQuest().getInternalID()).showText(
											I18n.locMsg(p, "QuestGUI.Hover.QuitWarning", q.getQuest().getQuestName())));
							holder.add(q.getQuest().getInternalID());
						}
					book.changeLine();
					return questName;
				}

			});
		}
		//List<Quest> redoableQuests = new ArrayList<Quest>();
		//List<Quest> newQuests = new ArrayList<>();
		//List<Quest> currentQuests = new ArrayList<>();
		//List<Quest> unavailableQuests = new ArrayList<Quest>();

		if (QuestNPCManager.hasData(npc.getId())) {
			for (Quest q : QuestNPCManager.getNPCData(npc.getId()).getRewardQuests()) {
				if (qd.hasFinished(q) && q.getQuestReward().hasMultipleChoices() && !qd.getFinishData(q).isRewardTaken()
						&& q.getQuestReward().getRewardNPC().getId() == npc.getId()) {
					actionMap.get(NPCInfoType.rewardQuests).add(new Function<Boolean, InteractiveText>() {

						@Override
						public InteractiveText apply(Boolean b) {
							InteractiveText questInfo = new InteractiveText(p,"").showQuest(q);
							if(!b) {
								return questInfo;
							}
							book.add(I18n.locMsg(p, "QuestGUI.NewQuestSymbol"));
							book.add(questInfo);
							book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.RewardButton"))
									.clickCommand("/mq q reward select " + q.getInternalID())
									.showText(I18n.locMsg(p, "QuestGUI.Hover.ClaimReward"))).changeLine();
							return questInfo;
						}

					});
				}
			}
			// unfinished

			for (Quest q : QuestNPCManager.getNPCData(npc.getId()).getGivenQuests()) {
				if (holder.contains(q.getInternalID()) || (!q.isRedoable() && qd.hasFinished(q)&&!AdminCommand.overrideMode.contains(p.getUniqueId().toString())))
					continue;
				if (qd.canTake(q, false)) {
					if (qd.hasFinished(q)) {
						// book.add(I18n.locMsg(p,"QuestGUI.RedoableQuestSymbol"));
						actionMap.get(NPCInfoType.redoableQuests).add(new Function<Boolean,InteractiveText>(){

							@Override
							public InteractiveText apply(Boolean b) {
								InteractiveText questInfo = new InteractiveText(p,"").showQuest(q);
								if(!b) {
									return questInfo;
								}
								book.add(I18n.locMsg(p, "QuestGUI.RedoableQuestSymbol"));
								book.add(questInfo);
								book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.TakeButton"))
										.clickCommand("/mq quest take " + q.getInternalID())
										.showText(I18n.locMsg(p, "QuestGUI.Hover.TakeQuest", q.getQuestName())));
								book.changeLine();
								return questInfo;
							}
							
						});
						//redoableQuests.add(q);
					} else {
						actionMap.get(NPCInfoType.newQuests).add(new Function<Boolean,InteractiveText>(){

							@Override
							public InteractiveText apply(Boolean b) {
								InteractiveText questInfo = new InteractiveText(p,"").showQuest(q);
								if(!b) {
									return questInfo;
								}
										book.add(I18n.locMsg(p, "QuestGUI.NewQuestSymbol"));
										book.add(questInfo);
										book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.TakeButton"))
												.clickCommand("/mq quest take " + q.getInternalID())
												.showText(I18n.locMsg(p, "QuestGUI.Hover.TakeQuest", q.getQuestName())));
										book.changeLine();
										return questInfo;
							}
							
						});
						
						//newQuests.add(q);
						// book.add(I18n.locMsg(p,"QuestGUI.NewQuestSymbol"));
					}
					// book.add(new InteractiveText(p,"").showQuest(q));
					// book.add(new
					// InteractiveText(I18n.locMsg(p,"QuestJourney.TakeButton")).clickCommand("/mq
					// quest take " + q.getInternalID())
					// .showText(I18n.locMsg(p,"QuestGUI.Hover.TakeQuest", q.getQuestName())));
					// book.changeLine();
				} else if (qd.isCurrentlyDoing(q)) {
					actionMap.get(NPCInfoType.currentQuests).add(new Function<Boolean,InteractiveText>(){

						@Override
						public InteractiveText apply(Boolean b) {
							InteractiveText qa = new InteractiveText(p,"").showQuest(q);
							if(!b) {
								return qa;
							}
							book.add(I18n.locMsg(p, "QuestGUI.QuestDoingSymbol"));
							book.add(qa);
							if (q.isQuitable())
								book.add(new InteractiveText(p,I18n.locMsg(p, "QuestJourney.QuitButton"))
										.clickCommand("/mq quest quit " + q.getInternalID())
										.showText(I18n.locMsg(p, "QuestGUI.Hover.QuitWarning", q.getQuestName())));
							book.changeLine();
							return qa;
						}
						
					});
					//currentQuests.add(q);
					// book.add(I18n.locMsg(p,"QuestGUI.QuestDoingSymbol"));
					// book.add(new InteractiveText(p,"").showQuest(q));
					// if (q.isQuitable())
					// book.add(new
					// InteractiveText(I18n.locMsg(p,"QuestJourney.QuitButton")).clickCommand("/mq
					// quest quit " + q.getInternalID())
					// .showText(I18n.locMsg(p,"QuestGUI.Hover.QuitWarning", q.getQuestName())));
					// book.changeLine();
				} else {
					// book.add("&0- ");
					// book.add(new InteractiveText(p,"").showRequirement(qd, q));
					// book.changeLine();
					actionMap.get(NPCInfoType.unavailableQuests).add(new Function<Boolean,InteractiveText>(){

						@Override
						public InteractiveText apply(Boolean b) {
							InteractiveText uninfo = new InteractiveText(p,"").showRequirement(qd, q);
							if(!b) {
								return uninfo;
							}
							book.add("&0- ");
							book.add(uninfo);
							book.changeLine();
							return uninfo;
						}
						
					});
					
					//unavailableQuests.add(q);

				}
			}
			
			
			

		}
		//List<FriendConversation> finished = new ArrayList<>();
		for (FriendConversation qc : QuestUtil.getConversations(npc, qd.getNPCfp(npc.getId()))) {
			if (qd.hasFinished(qc)) {
				//finished.add(qc);
				actionMap.get(NPCInfoType.oldFriendConversation).add(new Function<Boolean,InteractiveText>(){

					@Override
					public InteractiveText apply(Boolean b) {
						InteractiveText oldConv = new InteractiveText(p,I18n.locMsg(p, "QuestGUI.OldFriendConversation", qc.getName()))
								.clickCommand("/mq conv opennew " + qc.getInternalID());
						if(!b) {
							return oldConv;
						}
						book.add(oldConv);
						book.changeLine();
						return oldConv;
					}
					
				});
				//book.add(new InteractiveText(p,I18n.locMsg(p, "QuestGUI.OldFriendConversation", qc.getName()))
						//.clickCommand("/mq conv opennew " + qc.getInternalID()));
				//book.changeLine();
			}else {
				actionMap.get(NPCInfoType.newFriendConversation).add(new Function<Boolean,InteractiveText>(){

					@Override
					public InteractiveText apply(Boolean b) {
						InteractiveText newInfo = new InteractiveText(p,I18n.locMsg(p, "QuestGUI.NewFriendConversation", qc.getName()))
								.clickCommand("/mq conv opennew " + qc.getInternalID());
						if(!b) {
							return newInfo;
						}
						book.add(newInfo);
						book.changeLine();
						return newInfo;
					}
					
				});
				//book.add(new InteractiveText(p,I18n.locMsg(p, "QuestGUI.NewFriendConversation", qc.getName()))
						//.clickCommand("/mq conv opennew " + qc.getInternalID()));
				//book.changeLine();
			}
			
		}

		
		
		
		List<Function<Boolean,InteractiveText>> normalExecutionOrder = new ArrayList<>();
		List<Function<Boolean,InteractiveText>> unavailableExecutionOrder = new ArrayList<>();
		for(Entry<NPCInfoType, List<Function<Boolean, InteractiveText>>> func:actionMap.entrySet()) {
			if(func.getKey().equals(NPCInfoType.unavailableQuests)||func.getKey().equals(NPCInfoType.oldFriendConversation)) {
				for(Function<Boolean,InteractiveText> f:func.getValue()) {
					unavailableExecutionOrder.add(f);
				}
			}else {
				for(Function<Boolean,InteractiveText> f:func.getValue()) {
					normalExecutionOrder.add(f);
				}
			}
		}
		//sort by name
		normalExecutionOrder.sort(new Comparator<Function<Boolean,InteractiveText>>() {

			@Override
			public int compare(Function<Boolean, InteractiveText> f1, Function<Boolean, InteractiveText> f2) {
				return ChatColor.stripColor(f1.apply(false).get().toPlainText()).compareTo(ChatColor.stripColor(f2.apply(false).get().toPlainText()));
			}
			
		});
		unavailableExecutionOrder.sort(new Comparator<Function<Boolean,InteractiveText>>() {

			@Override
			public int compare(Function<Boolean, InteractiveText> f1, Function<Boolean, InteractiveText> f2) {
				return ChatColor.stripColor(f1.apply(false).get().toPlainText()).compareTo(ChatColor.stripColor(f2.apply(false).get().toPlainText()));
			}
			
		});
		normalExecutionOrder.forEach((f)->{
			f.apply(true);
		});
		unavailableExecutionOrder.forEach((f)->{
			f.apply(true);
		});
		openBook(p, book.toSendableBook());
	}

	private enum NPCInfoType {
		GUIOptions, Trade, Progresses, rewardQuests, newQuests, redoableQuests, currentQuests, unavailableQuests,
		oldFriendConversation,newFriendConversation;
	}
}
