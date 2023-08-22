package me.Cutiemango.MangoQuest;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import me.Cutiemango.MangoQuest.ConfigSettings.SaveType;
import me.Cutiemango.MangoQuest.Metrics.SimplePie;
import me.Cutiemango.MangoQuest.Metrics.SingleLineChart;
import me.Cutiemango.MangoQuest.commands.AdminCommand;
import me.Cutiemango.MangoQuest.commands.CommandReceiver;
import me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.listeners.CompatMainListener;
import me.Cutiemango.MangoQuest.listeners.MainListener;
import me.Cutiemango.MangoQuest.manager.CustomObjectManager;
import me.Cutiemango.MangoQuest.manager.PluginHooker;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestNPCManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.manager.RequirementManager;
import me.Cutiemango.MangoQuest.manager.ScoreboardManager;
import me.Cutiemango.MangoQuest.manager.config.QuestConfigManager;
import me.Cutiemango.MangoQuest.manager.database.DatabaseManager;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlaceholderAPI;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlaceholderAPI.Mode;
import me.Cutiemango.MangoQuest.versions.VersionHandler;
import me.Cutiemango.MangoQuest.versions.Version_LTS;
import me.Cutiemango.MangoQuest.versions.Version_v1_10_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_11_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_12_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_13_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_13_R2;
import me.Cutiemango.MangoQuest.versions.Version_v1_14_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_15_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_16_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_16_R2;
import me.Cutiemango.MangoQuest.versions.Version_v1_16_R3;
import me.Cutiemango.MangoQuest.versions.Version_v1_17_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_18_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_18_R2;
import me.Cutiemango.MangoQuest.versions.Version_v1_19_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_19_R2;
import me.Cutiemango.MangoQuest.versions.Version_v1_19_R3;
import me.Cutiemango.MangoQuest.versions.Version_v1_20_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_8_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_8_R2;
import me.Cutiemango.MangoQuest.versions.Version_v1_8_R3;
import me.Cutiemango.MangoQuest.versions.Version_v1_9_R1;
import me.Cutiemango.MangoQuest.versions.Version_v1_9_R2;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Main extends JavaPlugin {
	private static Main instance;
	public MinecraftCompatability mcCompat;
	public PluginHooker pluginHooker;
	public VersionHandler handler;
	public boolean hexColorSupport = false;
	public QuestConfigManager configManager;
	private int counterTaskID = -1;
	public static AtomicBoolean lockDown = new AtomicBoolean(false);
	public volatile static Set<String> disableScoreboard = new HashSet<>();

	public static Set<String> getDisableScoreboard() {
		return disableScoreboard;
	}

	@Override
	public void onEnable() {
	
		instance = this;

		getCommand("mq").setExecutor(new CommandReceiver());
		getCommand("mqa").setExecutor(new AdminCommand());
		
        //if (false) {
         //   animationManager = new AnimationManager();
       // }

		boolean not = false;
		boolean test = false;
		boolean lts = false;
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		switch (version) {
		case "v1_8_R1":
			handler = new Version_v1_8_R1();
			ConfigSettings.LEGACY_MC = true;
			ConfigSettings.MC_1_8 = true;
			break;
		case "v1_8_R2":
			handler = new Version_v1_8_R2();
			ConfigSettings.LEGACY_MC = true;
			ConfigSettings.MC_1_8 = true;
			break;
		case "v1_8_R3":
			handler = new Version_v1_8_R3();
			ConfigSettings.LEGACY_MC = true;
			ConfigSettings.MC_1_8 = true;
			break;
		case "v1_9_R1":
			handler = new Version_v1_9_R1();
			ConfigSettings.LEGACY_MC = true;
			break;
		case "v1_9_R2":
			handler = new Version_v1_9_R2();
			ConfigSettings.LEGACY_MC = true;
			break;
		case "v1_10_R1":
			handler = new Version_v1_10_R1();
			ConfigSettings.LEGACY_MC = true;
			break;
		case "v1_11_R1":
			handler = new Version_v1_11_R1();
			ConfigSettings.LEGACY_MC = true;
			break;
		case "v1_12_R1":
			handler = new Version_v1_12_R1();
			ConfigSettings.LEGACY_MC = true;
			break;
		case "v1_13_R1":
			handler = new Version_v1_13_R1();
			break;
		case "v1_13_R2":
			handler = new Version_v1_13_R2();
			break;
		case "v1_14_R1":
			handler = new Version_v1_14_R1();
			break;
		case "v1_15_R1":
			handler = new Version_v1_15_R1();
			break;
		case "v1_16_R1":
			handler = new Version_v1_16_R1();
			hexColorSupport = true;
			break;
		case "v1_16_R2":
			handler = new Version_v1_16_R2();
			hexColorSupport = true;
			break;
		case "v1_16_R3":
			handler = new Version_v1_16_R3();
			hexColorSupport = true;
			// QuestChatManager.logCmd(Level.WARNING,
			// I18n.locMsg(null,"Cmdlog.TestingVersion"));
			break;
		case "v1_17_R1":
			handler = new Version_v1_17_R1();
			hexColorSupport = true;
			// QuestChatManager.logCmd(Level.WARNING,
			// I18n.locMsg(null,"Cmdlog.TestingVersion"));
			break;
		case "v1_18_R1":
			handler = new Version_v1_18_R1();
			hexColorSupport = true;
			test = true;
			break;
		case "v1_18_R2":
			hexColorSupport = true;
			handler = new Version_v1_18_R2();
			break;
		case "v1_19_R1":
			hexColorSupport = true;
			handler = new Version_v1_19_R1();
			//test = true;
			break;
		case "v1_19_R2":
			hexColorSupport = true;
			handler = new Version_v1_19_R2();
			//test = true;
			break;
		case "v1_19_R3":
			hexColorSupport = true;
			handler = new Version_v1_19_R3();
			//test = true;
			break;
		case "v1_20_R1":
			hexColorSupport = true;
			handler = new Version_v1_20_R1();
			
			//test = true;
			break;
		default:
			if(!getConfig().contains("enablelts")) {
				getConfig().set("enablelts", true);
			}else {
				ConfigSettings.ENABLE_LTS_SUPPORT = getConfig().getBoolean("enablelts");
			}
			
			not = !ConfigSettings.ENABLE_LTS_SUPPORT;
			lts = ConfigSettings.ENABLE_LTS_SUPPORT;
			
			if(!not) {
				handler = new Version_LTS();
			}
			break;
		}
		this.mcCompat = new MinecraftCompatability();
		configManager = new QuestConfigManager();
		if(not) {
			QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null, "Cmdlog.VersionNotSupported1"));
			QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null, "Cmdlog.VersionNotSupported2"));
		}
		if(test) {
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "Cmdlog.TestingVersion"));
		}
		if(lts) {
			QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null, "Cmdlog.UsingLongTermSupportVersion"));
		}
		
		DebugHandler.log(2, "Current Thread: "+Thread.currentThread().getId());
		
		pluginHooker = new PluginHooker(this);
		pluginHooker.hookPlugins();
		SimpleQuestObject.initObjectNames(null);

		
		if (ConfigSettings.SAVE_TYPE == ConfigSettings.SaveType.SQL) {
			DatabaseManager.initPlayerDB();
		}

		getServer().getPluginManager().registerEvents(new MainListener(), this);
		

		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null, "Cmdlog.LoadedNMSVersion", version));
		if(!ConfigSettings.LEGACY_MC) {
			getServer().getPluginManager().registerEvents(new CompatMainListener(), this);
		}
		
		Main.lockDown.set(true);
		
		new BukkitRunnable() {

			@Override
			public void run() {
				try
				{
					DebugHandler.log(2, "Current Thread in init task: "+Thread.currentThread().getId());
					//pluginHooker.hookPlugins();
					CustomObjectManager.loadCustomObjects();
					QuestConfigManager.getLoader().loadAll();
					Main.lockDown.set(false);
					loadPlayers();
					startCounter();
					DebugHandler.log(1, "Plugin Loaded!");
					
					
				}catch(Exception e) {
					e.printStackTrace();
				}
				
					
			}
		}.runTaskLater(this, 10L);

		

		// Use new metrics!! Yay!!
		// new Metrics(this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		
		Metrics m = new Metrics(this,15213);
		addCustomCharts(m);

	}

	@Override
	public void onDisable() {
		stopCounter();
		savePlayers();
		this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		DebugHandler.log(1, I18n.locMsg(null, "Cmdlog.Disabled"));
	}
	private void addCustomCharts(Metrics metrics) {
	    metrics.addCustomChart(new SingleLineChart("Online Players", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            // (This is useless as there is already a player chart by default.)
	            return Bukkit.getOnlinePlayers().size();
	        }
	    }));
	    metrics.addCustomChart(new SingleLineChart("Accumulated Players", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            // (This is useless as there is already a player chart by default.)
	            return Bukkit.getOfflinePlayers().length+Bukkit.getOnlinePlayers().size();       
	        }
	    }));
	    metrics.addCustomChart(new SimplePie("NMS Versions", () -> {
	        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	    }));
	}
	public void reload() {
		savePlayers();
		QuestStorage.clear();
		QuestNPCManager.clear();
		Main.lockDown.set(true);
		configManager = new QuestConfigManager();
		pluginHooker = new PluginHooker(this);
		pluginHooker.hookPlugins();
		configManager.loadFile();

		SimpleQuestObject.initObjectNames(null);
		CustomObjectManager.loadCustomObjects();
		QuestConfigManager.getLoader().loadAll();

		Main.lockDown.set(false);
		
		loadPlayers();
	}

	public static Main getInstance() {
		return instance;
	}

	public static PluginHooker getHooker() {
		return instance.pluginHooker;
	}

	public void loadPlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			QuestPlayerData qd = new QuestPlayerData(p);
			qd.load(ConfigSettings.SAVE_TYPE).thenRun(() -> {
				try {
					QuestStorage.playerData.put(p.getName(), qd);
					QuestPlayerData qd1 = QuestUtil.compileEffectTasks(qd);
					QuestStorage.playerData.put(p.getName(), qd1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		}
	}

	public void savePlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (QuestUtil.getData(p) != null) {
				QuestUtil.getData(p).save();
				QuestChatManager.info(p, I18n.locMsg(p, "CommandInfo.PlayerDataSaving"));
			}
		}
	}

	public void startCounter() {
		counterTaskID = new BukkitRunnable() {
			int counter = 0;
			int clearCounter = 0;

			@Override
			public void run() {
				try {

					// sql clear should be placed outside
					if (ConfigSettings.SAVE_TYPE == SaveType.SQL
							&& clearCounter++ > ConfigSettings.SQL_CLEAR_INTERVAL_IN_TICKS) {
						// clear sql drivers (force garbage collector)
						Collections.list(DriverManager.getDrivers()).forEach(driver -> {
							try {
								DriverManager.deregisterDriver(driver);
								DriverManager.registerDriver(driver);
							} catch (SQLException e) {
								QuestChatManager.logCmd(Level.SEVERE, "An error occured while deregistering sql drivers!");
								e.printStackTrace();
							}
						});
						clearCounter = 0;
					}

					HashMap<String, List<QuestProgress>> toUpdate = new HashMap<>();
					for (Entry<String, List<QuestProgress>> entry : QuestStorage.timedProgress.entrySet()) {
						QuestPlayerData qpd = QuestUtil.getData(Bukkit.getPlayer(UUID.fromString(entry.getKey())));
						List<QuestProgress> progresses = entry.getValue();
						List<QuestProgress> newProgresses = new ArrayList<>();
						boolean needModification = false;
						for (QuestProgress qp : progresses) {
							if (qp.getQuest().isTimeLimited() || (qp.getQuest().hasRequirement()
									&& (qp.getQuest().getRequirements().containsKey(RequirementType.SERVER_TIME)
											|| qp.getQuest().getRequirements().containsKey(RequirementType.WORLD_TIME)))) {
								Optional<String> idk = RequirementManager.checkServerTimeFail(qp.getOwner(),
										qp.getQuest().getWorldLimit(), qp.getQuest().getRequirements(), false);
								if (qp.getQuest().isTimeLimited()
										&& System.currentTimeMillis() > qp.getQuest().getTimeLimit() + qp.getTakeTime()) {
									QuestChatManager.info(qp.getOwner(), I18n.locMsg(qp.getOwner(),
											"QuestJourney.QuestFailed", qp.getQuest().getQuestName()));
									qpd.forceQuit(qp.getQuest(), false);
									needModification = true;
									if (qp.getQuest().getQuestNPC() != null) {
										Map<NPC, List<Quest>> copy = qpd.getNpcEffects();
										List<Quest> questList = new ArrayList<>();
										if (copy.containsKey(qp.getQuest().getQuestNPC())) {
											questList = copy.get(qp.getQuest().getQuestNPC());
										}
										questList.add(qp.getQuest());
										copy.put(qp.getQuest().getQuestNPC(), questList);
										qpd.setNpcEffects(copy);
									}
									continue;

								} else if (idk.isPresent()) {
									QuestChatManager.info(qp.getOwner(), I18n.locMsg(qp.getOwner(),
											"QuestJourney.QuestFailed", qp.getQuest().getQuestName()));
									needModification = true;
									qpd.forceQuit(qp.getQuest(), false);
									if (qp.getQuest().getQuestNPC() != null) {
										Map<NPC, List<Quest>> copy = qpd.getNpcEffects();
										List<Quest> questList = new ArrayList<>();
										if (copy.containsKey(qp.getQuest().getQuestNPC())) {
											questList = copy.get(qp.getQuest().getQuestNPC());
										}
										questList.add(qp.getQuest());
										copy.put(qp.getQuest().getQuestNPC(), questList);
										qpd.setNpcEffects(copy);
									}
									continue;
								}
								newProgresses.add(qp);
							}
						}
						if (needModification)
							toUpdate.put(entry.getKey(), newProgresses);
					}
					for (Entry<String, List<QuestProgress>> entry : toUpdate.entrySet()) {
						QuestStorage.timedProgress.put(entry.getKey(), entry.getValue());
					}
					if (counter++ > ConfigSettings.PLAYER_DATA_SAVE_INTERVAL) {
						CompletableFuture.runAsync(() -> {
							for (QuestPlayerData qpd : new ArrayList<>(QuestStorage.playerData.values())) {
								qpd.save();
							}
						});
						counter = 0;
					}

					for (Player p : Bukkit.getOnlinePlayers()) {
						QuestPlayerData pd = QuestUtil.getData(p);

						if (pd == null)
							continue;

						// debug for developer
						// if(p.getName().equals("SakurajiKanade")) {
						// p.sendMessage("progresses size:"+pd.getProgresses().size());
						// p.sendMessage("Finished Quests size:"+pd.getFinishQuests());
						// p.sendMessage("finished conversations
						// size:"+pd.getFinishedConversations().size());
						// p.sendMessage("friend point storage
						// size:"+pd.getFriendPointStorage().size());
						// }
						// if (counter++ > ConfigSettings.PLAYER_DATA_SAVE_INTERVAL) {
						// pd.save();
						// counter = 0;
						// }

						// pd.checkQuestFail();

						if (Main.getHooker().hasPlaceholderAPIEnabled()) {
							Set<Quest> errorQuests = new HashSet<>();
							for (QuestProgress qp : pd.getPapiQuests()) {

								if (!pd.checkPlayerInWorld(qp.getQuest())) {
									continue;
								}
								for (QuestObjectProgress qop : qp.getCurrentObjects()) {
									if (qop.getObject() instanceof QuestObjectPlaceholderAPI) {
										QuestObjectPlaceholderAPI qopapi = (QuestObjectPlaceholderAPI) qop.getObject();
										String placeholder = qopapi.getPlaceholder();
										int expectedInteger = qopapi.getExpectedInteger();
										double expectedDecimal = qopapi.getExpectedDecimal();
										String expectedString = qopapi.getExpectedString();
										QuestObjectPlaceholderAPI.Mode mode = qopapi.getMode();
										QuestUtil.Comparison compare = qopapi.getCompare();
										if (mode == Mode.String) {
											if (expectedString.equals(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
													"%" + placeholder + "%"))) {
												pd.objectSuccess(qp, qop);
											}
										} else {
											try {
												if (mode == Mode.Integer) {
													switch (compare) {
													case EQUAL: {
														if (expectedInteger == Integer
																.parseInt(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%"))) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case BIGGEREQUAL: {
														if (Integer.parseInt(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																"%" + placeholder + "%")) >= expectedInteger) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case SMALLEREQUAL: {
														if (Integer.parseInt(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																"%" + placeholder + "%")) <= expectedInteger) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case BIGGER: {
														if (Integer.parseInt(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																"%" + placeholder + "%")) > expectedInteger) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case SMALLER: {
														if (Integer.parseInt(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																"%" + placeholder + "%")) < expectedInteger) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													}
												} else if (mode == Mode.Decimal) {
													switch (compare) {
													case EQUAL: {
														if (expectedDecimal == Double
																.parseDouble(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%"))) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case BIGGEREQUAL: {
														if (Double
																.parseDouble(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%")) >= expectedDecimal) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case SMALLEREQUAL: {
														if (Double
																.parseDouble(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%")) <= expectedDecimal) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case BIGGER: {
														if (Double
																.parseDouble(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%")) > expectedDecimal) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													case SMALLER: {
														if (Double
																.parseDouble(PlaceholderAPI.setPlaceholders(pd.getPlayer(),
																		"%" + placeholder + "%")) < expectedDecimal) {
															pd.objectSuccess(qp, qop);
														}
														break;
													}
													}
												}
											} catch (IllegalArgumentException e) {
												QuestChatManager.logCmd(Level.SEVERE,
														I18n.locMsg(p, "CommandInfo.UnmatchedPlaceholderType",
																qp.getQuest().getInternalID(), placeholder,
																I18n.locMsg(p, mode.getDisplayPath())));
												errorQuests.add(qp.getQuest());
												break;
											}
										}
									}
								}
							}
							for (Quest q : errorQuests) {
								pd.getPapiQuests().removeIf((qp) -> {
									return QuestValidater.weakValidate(q, qp.getQuest());
								});
							}

						}
						if (ConfigSettings.USE_PARTICLE_EFFECT) {
						/*
							List<Quest> newRedoable = new ArrayList<>(pd.getRedoableQuests());
						for (Quest q : pd.getRedoableQuests()) {
							if (pd.canTake(q,false)) {
								// developer debug
								// if(pd.getPlayer().getName().equals("SakurajiKanade")) {
								// pd.getPlayer().sendMessage("Redoable becomes takable:"+q.getInternalID());
								// }
								if (q.getQuestNPC() == null) {
									DebugHandler.log(5, "Rejected quest npc effects because "+q.getQuestName()+" npc is null");
									continue;
								}
								List<Quest> questList = new ArrayList<>();
								if (pd.getNpcEffects().containsKey(q.getQuestNPC())) {
									questList = pd.getNpcEffects().get(q.getQuestNPC());
								}
								questList.add(q);
								Map<NPC, List<Quest>> effects = pd.getNpcEffects();
								effects.put(q.getQuestNPC(), questList);
								pd.setNpcEffects(effects);
								newRedoable.removeIf((toRemove) -> {
									return toRemove.getInternalID().equals(q.getInternalID());
								});
							}
						}
						pd.setRedoableQuests(newRedoable);*/
						
							// !pd.getNPCtoTalkWith(npc).isEmpty()
							for (NPC npc : pd.getNpcEffects().keySet()) {
								//for(Quest q:pd.getNpcEffects().get(npc)){
								//	DebugHandler.log(5, "npc="+npc.getId()+" quest="+q.getInternalID());
								//}
								
								if (pd.getNpcEffects().get(npc) == null || pd.getNpcEffects().get(npc).isEmpty()) {
									//DebugHandler.log(0, "is empty for reasons");
									continue;
								}
								
								if(npc.getStoredLocation() == null) {
									npc = CitizensAPI.getNPCRegistry().getById(npc.getId());
									
								}
								
								if (pd.isNearNPC(npc)) {
									// npc has been spawned before
									
									if (npc.getStoredLocation() != null) {
										Main.getInstance().handler.playNPCEffect(pd.getPlayer(), npc.getStoredLocation());
									}
									
								}
							}

							// try {
							// QuestNPCManager.effectTask(pd);
							// } catch (Exception e) {
							// System.out.println(e);
							// e.printStackTrace();
							// this.cancel();
							// }

						}
					      if (ConfigSettings.ENABLE_SCOREBOARD) {
							Bukkit.getScheduler().runTask(Main.instance, () -> {
								try {
									
									if(!Main.getDisableScoreboard().contains(pd.getPlayer().getUniqueId().toString())) {
										Scoreboard score = ScoreboardManager.update(pd);
										pd.getPlayer().setScoreboard(score);
									}
								} catch (Exception e) {
									QuestChatManager.logCmd(Level.SEVERE,
											I18n.locMsg(pd.getPlayer(), "Cmdlog.ScoreboardException"));
									System.out.println(e);
									e.printStackTrace();
									this.cancel();
								}
							});
						}
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}.runTaskTimer(this, 0L, 20L).getTaskId();
	}

	public void stopCounter() {
		if (counterTaskID != -1)
			Bukkit.getScheduler().cancelTask(counterTaskID);
	}
//backup code
	
}
