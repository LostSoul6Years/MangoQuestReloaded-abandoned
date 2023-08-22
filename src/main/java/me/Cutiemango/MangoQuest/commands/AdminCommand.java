package me.Cutiemango.MangoQuest.commands;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.sqlite.SQLiteConfig;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.ConfigSettings.SaveType;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.manager.MigrationManager;
import me.Cutiemango.MangoQuest.manager.QuestBookGUIManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.manager.config.QuestConfigManager;
import me.Cutiemango.MangoQuest.manager.database.DatabaseManager;
import me.Cutiemango.MangoQuest.model.Quest;
import net.citizensnpcs.api.npc.NPC;

public class AdminCommand implements CommandExecutor {
	public static Set<String> overrideMode = new HashSet<>();
	// Command: /mqa [args]
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("MangoQuest.AdminCommand")) {
			QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.NoPermission"));
			return false;
		}
		if (args.length == 1) {
			Player admin = null;
			if (sender instanceof Player) {
				admin = (Player) sender;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				Main.getInstance().reload();
				if (admin != null) {
					QuestChatManager.info(sender, "&a" + I18n.locMsg(admin, "CommandInfo.ReloadSuccessful"));
				} else {
					QuestChatManager.info(sender, "&a" + I18n.locMsg(null, "CommandInfo.ReloadSuccessful"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("placeholders")) {
				if (admin != null) {
					sendPlaceHolder(admin);
					
				} else {
					
					sendPlaceHolder(sender);
				}
				return false;
			}else if(args[0].equalsIgnoreCase("override")) {
				if(admin != null) {
					if(overrideMode.contains(admin.getUniqueId().toString())) {
						overrideMode.remove(admin.getUniqueId().toString());
						QuestChatManager.info(sender,I18n.locMsg(admin, "CommandInfo.DisabledOverride"));
					}else {
						overrideMode.add(admin.getUniqueId().toString());
						QuestChatManager.info(sender,I18n.locMsg(admin, "CommandInfo.EnabledOverride"));
					}
					QuestStorage.playerData.put(admin.getName(), QuestUtil.recompileEffectTasks(QuestUtil.getData(admin)));
					return true;
				}
				
			} else {
				sendAdminHelp(sender);
				return false;
			}
		} else if (args.length > 1) {
			if (args[0].equalsIgnoreCase("debuglevel")) {
				if (args.length != 2) {
					// TODO: wrong arguments
					return false;
				}
				try {

					Integer level = Integer.parseInt(args[1]);
					if (level < 0 || level > 5) {
						QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.InvalidArgument"));
						return false;
					}
					DebugHandler.DEBUG_LEVEL = level;
					Main.getInstance().configManager.getConfig().getConfig().set("debugLevel", level);
					Main.getInstance().configManager.getConfig().save();
					QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.DebugLevelChanged", level + ""));
				} catch (NumberFormatException invalidArgs) {
					QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.InvalidArgument"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("backup")) {
				if (args.length != 2 && args.length != 3) {
					QuestChatManager.error(sender, "CommandInfo.InvalidArgument");
					return false;
				}
				boolean override = false;
				String name = args[1];
				// String typeGeneric = args[2];
				// String type;
				File backups = Main.getInstance().configManager.getBackups();
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("true")) {
						override = true;
					}
				}
				// if (typeGeneric.equalsIgnoreCase("yml")) {
				// type = "yml";
				// } else if (typeGeneric.equalsIgnoreCase("sql")) {
				// type = "db";
				// } else {
				// QuestChatManager.error(sender, "CommandInfo.BackupTypeUnsupported");
				// return false;
				// }
				// if (new File(backups.getPath() + "/" + name + "." + type).isFile()) {
				// TODO: tell the admin the file already exists (obsolete,will just override
				// file instead)
				// return false;
				// }
				QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.BackingUp"));
				if (ConfigSettings.SAVE_TYPE == SaveType.YML) {
					File ymlFolder = new File(backups.getPath() + "/" + name );
					if(ymlFolder.isDirectory()&&!override) {
						QuestChatManager.error(sender, I18n.locMsg(null,"CommandInfo.BackupExists",ymlFolder.getName()));
						//TODO: tell admin to run 3 args command
						return false;
					}
					CompletableFuture.supplyAsync(() -> {
						
						ymlFolder.mkdir();
						
						
						
						File dataFolder = new File(Main.getInstance().getDataFolder() + "/data/");
						if(!dataFolder.isDirectory()) {
							//CommandInfo.BackupFailed
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupSourceNotExists"));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupSourceNotExists"));
							return false;
						}
						File backupss = Main.getInstance().configManager.getBackups();
						if(!backupss.isDirectory()) {
							backupss.mkdir();
						}
						for(File data:dataFolder.listFiles()) {
							if(data.getName().endsWith(".yml")) {								
								try {
									FileUtils.copyFile(data, new File(ymlFolder,data.getName()));
								} catch (IOException e) {
									//CommandInfo.BackupError
									if (sender != null) {
										QuestChatManager.info(sender,
												I18n.locMsg(null, "CommandInfo.BackupError", ymlFolder.getName()));
									}
									QuestChatManager.info(Bukkit.getConsoleSender(),
											I18n.locMsg(null, "CommandInfo.BackupError", ymlFolder.getName()));
									e.printStackTrace();
								}
							}
						}
						return true;
						// tell the admin the dirty work is done
					}).thenAccept((bool) -> {
						if(bool) {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupFinish", ymlFolder.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupFinish", ymlFolder.getName()));
						}else {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupFailed", ymlFolder.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupFailed", ymlFolder.getName()));
						}
					});
					;
				} else if (ConfigSettings.SAVE_TYPE == SaveType.SQL) {
					File sqlFile = new File(backups.getPath() + "/" + name + ".db");
					if(sqlFile.isFile()&&!override) {
						QuestChatManager.error(sender, I18n.locMsg(null,"CommandInfo.BackupExists",sqlFile.getName()));
						return false;
					}
					CompletableFuture.supplyAsync(() -> {

						try {
							sqlFile.createNewFile();
						} catch (IOException e) {
							// TODO: tell the admin cannot save to file
							e.printStackTrace();
							return false;
						}
						SQLiteConfig config = new SQLiteConfig();
						config.setEncoding(SQLiteConfig.Encoding.UTF8);
						try (Connection conn1 = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getPath())) {
							Connection conn = DatabaseManager.getConnection();
							PreparedStatement clearpdq = conn1.prepareStatement("DROP TABLE IF EXISTS mq_playerdata");
							clearpdq.execute();
							clearpdq.close();
							PreparedStatement clearqp = conn1.prepareStatement("DROP TABLE IF EXISTS mq_questprogress");
							clearqp.execute();
							clearqp.close();
							PreparedStatement clearfq = conn1.prepareStatement("DROP TABLE IF EXISTS mq_finishedquest");
							clearfq.execute();
							clearfq.close();
							PreparedStatement clearfp = conn1.prepareStatement("DROP TABLE IF EXISTS mq_friendpoint");
							clearfp.execute();
							clearfp.close();
							PreparedStatement clearfc = conn1.prepareStatement("DROP TABLE IF EXISTS mq_finishedconv");
							clearfc.execute();
							clearfc.close();
							DatabaseManager.initPlayerDBOpen();
							PreparedStatement pdq = conn.prepareStatement("SELECT * FROM mq_playerdata");
							ResultSet pdqr = pdq.executeQuery();
							while (pdqr.next()) {
								//Bukkit.getLogger().info("INSERTED RECORD");
								PreparedStatement statement = conn1.prepareStatement(
										"INSERT INTO mq_playerdata (PDID,LastKnownID,UUID) VALUES (?,?,?)");
								statement.setInt(1, pdqr.getInt("PDID"));
								statement.setString(2, pdqr.getString("LastKnownID"));
								statement.setString(3, pdqr.getString("UUID"));
								statement.execute();
								statement.close();
							}
							pdqr.close();
							pdq.close();
							PreparedStatement pqpq = conn.prepareStatement("SELECT * FROM mq_questprogress");
							ResultSet pqpr = pqpq.executeQuery();
							while (pqpr.next()) {
								PreparedStatement statement = conn1.prepareStatement(
										"INSERT INTO mq_questprogress (QPDID,PDID,QuestID,QuestStage,Version,TakeStamp,QuestObjectProgress) VALUES (?,?,?,?,?,?,?)");
								statement.setInt(1, pqpr.getInt("QPDID"));
								statement.setInt(2, pqpr.getInt("PDID"));
								statement.setString(3, pqpr.getString("QuestID"));
								statement.setInt(4, pqpr.getInt("QuestStage"));
								statement.setInt(5, pqpr.getInt("Version"));
								statement.setInt(6, pqpr.getInt("TakeStamp"));
								statement.setString(7, pqpr.getString("QuestObjectProgress"));
								statement.execute();
								statement.close();
							}
							pqpr.close();
							pqpq.close();

							PreparedStatement pfqq = conn.prepareStatement("SELECT * FROM mq_finishedquest");
							ResultSet pfqr = pfqq.executeQuery();
							while (pfqr.next()) {
								PreparedStatement statement = conn1.prepareStatement(
										"INSERT INTO mq_finishedquest (FQID,PDID,QuestID,FinishedTimes,LastFinishTime,RewardTaken) VALUES (?,?,?,?,?,?)");
								statement.setInt(1, pfqr.getInt("FQID"));
								statement.setInt(2, pfqr.getInt("PDID"));
								statement.setString(3, pfqr.getString("QuestID"));
								statement.setInt(4, pfqr.getInt("FinishedTimes"));
								statement.setInt(5, pfqr.getInt("LastFinishTime"));
								statement.setInt(6, pfqr.getInt("RewardTaken"));
								statement.execute();
								statement.close();
							}
							pfqr.close();
							pfqq.close();

							PreparedStatement fpq = conn.prepareStatement("SELECT * FROM mq_friendpoint");
							ResultSet fpqr = fpq.executeQuery();
							while (fpqr.next()) {
								PreparedStatement statement = conn1.prepareStatement(
										"INSERT INTO mq_friendpoint (FPID,PDID,NPC,FriendPoint) VALUES (?,?,?,?)");
								statement.setInt(1, fpqr.getInt("FPID"));
								statement.setInt(2, fpqr.getInt("PDID"));
								statement.setInt(3, fpqr.getInt("NPC"));
								statement.setInt(4, fpqr.getInt("FriendPoint"));
								statement.execute();
								statement.close();
							}
							fpqr.close();
							fpq.close();

							PreparedStatement fcq = conn.prepareStatement("SELECT * FROM mq_finishedconv");
							ResultSet fcqr = fcq.executeQuery();
							while (fcqr.next()) {
								PreparedStatement statement = conn1.prepareStatement(
										"INSERT INTO mq_finishedconv (FCID,PDID,ConvID) VALUES (?,?,?)");
								statement.setInt(1, fcqr.getInt("FCID"));
								statement.setInt(2, fcqr.getInt("PDID"));
								statement.setInt(3, fcqr.getInt("ConvID"));
								statement.execute();
								statement.close();
							}
						} catch (SQLException e) {
							// CommandInfo.GeneralErrorWhileLoadingBackup
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupError"));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupError"));
							e.printStackTrace();
							return false;
						}
						return true;
					}).thenAccept((bool) -> {
						if(bool) {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupFinish", sqlFile.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupFinish", sqlFile.getName()));
						}else {
							//CommandInfo.BackupFailed
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupFailed", sqlFile.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupFailed", sqlFile.getName()));
						}
					});
					;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("loadbackup")) {
				if (Main.lockDown.get()) {
					QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.OngoingBackupLoad"));
					return false;
				}
				if (args.length != 2) {
					QuestChatManager.error(sender,I18n.locMsg(null, "CommandInfo.InvalidArgument") );
					return false;
				}
				// Player p = (Player) sender;

				String name = args[1];
				File backups = Main.getInstance().configManager.getBackups();
				File ymlFile = new File(backups.getPath() + "/" + name );
				File dbFile = new File(backups.getPath() + "/" + name + ".db");
				QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.BackupLoadStarted"));
				Main.lockDown.set(true);
				CompletableFuture.supplyAsync(() -> {
					
					try {
						
						if (ConfigSettings.SAVE_TYPE == SaveType.SQL) {
							
							if (ymlFile.isDirectory() && !dbFile.isFile()) {
								if (sender != null) {
									QuestChatManager.info(sender,
											I18n.locMsg(null, "CommandInfo.BackupConversion"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.BackupConversion"));
								try {
									SQLiteConfig config = new SQLiteConfig();
									config.setEncoding(SQLiteConfig.Encoding.UTF8);
									Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath(),config.toProperties());
									Map<String, YamlConfiguration> ymls = new HashMap<>();
									for (File f : ymlFile.listFiles()) {
										if (f.isFile()&&f.getName().endsWith(".yml")) {
											YamlConfiguration temp = new YamlConfiguration();
											try {
												temp.load(f);
												try {
													UUID.fromString(QuestUtil.getFileNameWithoutExtension(f));
												} catch (IllegalArgumentException e) {
													e.printStackTrace();
													if (sender != null) {
														QuestChatManager.info(sender, I18n.locMsg(null,
																"CommandInfo.YmlFileWrongFormat", f.getName()));
													}
													QuestChatManager.info(Bukkit.getConsoleSender(), I18n.locMsg(null,
															"CommandInfo.YmlFileWrongFormat", f.getName()));
													return false;
												}
												ymls.put(QuestUtil.getFileNameWithoutExtension(f), temp);
											} catch (IOException | InvalidConfigurationException e) {
												e.printStackTrace();
												if (sender != null) {
													QuestChatManager.info(sender, I18n.locMsg(null,
															"CommandInfo.ErrorWhileLoadingBackup", ymlFile.getName()));
												}
												QuestChatManager.info(Bukkit.getConsoleSender(), I18n.locMsg(null,
														"CommandInfo.ErrorWhileLoadingBackup", ymlFile.getName()));
												return false;
											}
										}
									}
									try {
										if (!MigrationManager.convertYmltoSQL(ymls, conn).get()) {
											if (sender != null) {
												QuestChatManager.info(sender,
														I18n.locMsg(null, "CommandInfo.MigrationError"));
											}
											QuestChatManager.info(Bukkit.getConsoleSender(),
													I18n.locMsg(null, "CommandInfo.MigrationError"));
											return false;
										}
									} catch (InterruptedException | ExecutionException e) {
										if (sender != null) {
											QuestChatManager.info(sender,
													I18n.locMsg(null, "CommandInfo.MigrationInterrupted"));
										}
										QuestChatManager.info(Bukkit.getConsoleSender(),
												I18n.locMsg(null, "CommandInfo.MigrationInterrupted"));
										e.printStackTrace();
										return false;
									}

								} catch (SQLException e) {
									e.printStackTrace();
									if (sender != null) {
										QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.MigrationError"));
									}
									QuestChatManager.info(Bukkit.getConsoleSender(),
											I18n.locMsg(null, "CommandInfo.MigrationError"));
									return false;
								}

							} else if (!ymlFile.isDirectory() && !dbFile.isFile()) {
								// CommandInfo.BackupDoesNotExist
								if (sender != null) {
									QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.BackupDoesNotExist"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.BackupDoesNotExist"));
								return false;
							}
							try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath())) {
								Connection conn1 = DatabaseManager.getConnection();
								PreparedStatement clearpdq = conn1.prepareStatement("DROP TABLE IF EXISTS mq_playerdata");
								clearpdq.execute();
								clearpdq.close();
								PreparedStatement clearqp = conn1.prepareStatement("DROP TABLE IF EXISTS mq_questprogress");
								clearqp.execute();
								clearqp.close();
								PreparedStatement clearfq = conn1.prepareStatement("DROP TABLE IF EXISTS mq_finishedquest");
								clearfq.execute();
								clearfq.close();
								PreparedStatement clearfp = conn1.prepareStatement("DROP TABLE IF EXISTS mq_friendpoint");
								clearfp.execute();
								clearfp.close();
								PreparedStatement clearfc = conn1.prepareStatement("DROP TABLE IF EXISTS mq_finishedconv");
								clearfc.execute();
								clearfc.close();
								DatabaseManager.initPlayerDBOpen();
								PreparedStatement pdq = conn.prepareStatement("SELECT * FROM mq_playerdata");
								ResultSet pdqr = pdq.executeQuery();
								while (pdqr.next()) {
									//Bukkit.getLogger().info("INSERTED RECORD");
									PreparedStatement statement = conn1.prepareStatement(
											"INSERT INTO mq_playerdata (PDID,LastKnownID,UUID) VALUES (?,?,?)");
									statement.setInt(1, pdqr.getInt("PDID"));
									statement.setString(2, pdqr.getString("LastKnownID"));
									statement.setString(3, pdqr.getString("UUID"));
									statement.execute();
									statement.close();
								}
								pdqr.close();
								pdq.close();
								PreparedStatement pqpq = conn.prepareStatement("SELECT * FROM mq_questprogress");
								ResultSet pqpr = pqpq.executeQuery();
								while (pqpr.next()) {
									PreparedStatement statement = conn1.prepareStatement(
											"INSERT INTO mq_questprogress (QPDID,PDID,QuestID,QuestStage,Version,TakeStamp,QuestObjectProgress) VALUES (?,?,?,?,?,?,?)");
									statement.setInt(1, pqpr.getInt("QPDID"));
									statement.setInt(2, pqpr.getInt("PDID"));
									statement.setString(3, pqpr.getString("QuestID"));
									statement.setInt(4, pqpr.getInt("QuestStage"));
									statement.setInt(5, pqpr.getInt("Version"));
									statement.setInt(6, pqpr.getInt("TakeStamp"));
									statement.setString(7, pqpr.getString("QuestObjectProgress"));
									statement.execute();
									statement.close();
								}
								pqpr.close();
								pqpq.close();

								PreparedStatement pfqq = conn.prepareStatement("SELECT * FROM mq_finishedquest");
								ResultSet pfqr = pfqq.executeQuery();
								while (pfqr.next()) {
									PreparedStatement statement = conn1.prepareStatement(
											"INSERT INTO mq_finishedquest (FQID,PDID,QuestID,FinishedTimes,LastFinishTime,RewardTaken) VALUES (?,?,?,?,?,?)");
									statement.setInt(1, pfqr.getInt("FQID"));
									statement.setInt(2, pfqr.getInt("PDID"));
									statement.setString(3, pfqr.getString("QuestID"));
									statement.setInt(4, pfqr.getInt("FinishedTimes"));
									statement.setInt(5, pfqr.getInt("LastFinishTime"));
									statement.setInt(6, pfqr.getInt("RewardTaken"));
									statement.execute();
									statement.close();
								}
								pfqr.close();
								pfqq.close();

								PreparedStatement fpq = conn.prepareStatement("SELECT * FROM mq_friendpoint");
								ResultSet fpqr = fpq.executeQuery();
								while (fpqr.next()) {
									PreparedStatement statement = conn1.prepareStatement(
											"INSERT INTO mq_friendpoint (FPID,PDID,NPC,FriendPoint) VALUES (?,?,?,?)");
									statement.setInt(1, fpqr.getInt("FPID"));
									statement.setInt(2, fpqr.getInt("PDID"));
									statement.setInt(3, fpqr.getInt("NPC"));
									statement.setInt(4, fpqr.getInt("FriendPoint"));
									statement.execute();
									statement.close();
								}
								fpqr.close();
								fpq.close();

								PreparedStatement fcq = conn.prepareStatement("SELECT * FROM mq_finishedconv");
								ResultSet fcqr = fcq.executeQuery();
								while (fcqr.next()) {
									PreparedStatement statement = conn1.prepareStatement(
											"INSERT INTO mq_finishedconv (FCID,PDID,ConvID) VALUES (?,?,?)");
									statement.setInt(1, fcqr.getInt("FCID"));
									statement.setInt(2, fcqr.getInt("PDID"));
									statement.setInt(3, fcqr.getInt("ConvID"));
									statement.execute();
									statement.close();
								}
							} catch (SQLException e) {
								// CommandInfo.GeneralErrorWhileLoadingBackup
								if (sender != null) {
									QuestChatManager.info(sender,
											I18n.locMsg(null, "CommandInfo.GeneralErrorWhileLoadingBackup"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.GeneralErrorWhileLoadingBackup"));
								e.printStackTrace();
								return false;
							}
							//Main.lockDown.set(false);

							// TODO: tell the admin that the dirty work has been done
						} else if (ConfigSettings.SAVE_TYPE == SaveType.YML) {
							Bukkit.getLogger().info("" + dbFile.isFile() + " " + ymlFile.isDirectory());
							if (dbFile.isFile() && !ymlFile.isDirectory()) {
								if (sender != null) {
									QuestChatManager.info(sender,
											I18n.locMsg(null, "CommandInfo.BackupConversion"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.BackupConversion"));
								try {
									SQLiteConfig config = new SQLiteConfig();
									config.setEncoding(SQLiteConfig.Encoding.UTF8);
									List<YamlConfiguration> ymls = MigrationManager
											.convertSQLToYml(
													DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath(),config.toProperties()))
											.get();
									ymlFile.mkdir();
									for (YamlConfiguration yml : ymls) {
										File file = new File(ymlFile,
												yml.getString("UUID") + ".yml");
										file.createNewFile();
										yml.save(file);

									}
								} catch (InterruptedException | ExecutionException | SQLException | IOException e) {
									if (sender != null) {
										QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.MigrationError"));
									}
									QuestChatManager.info(Bukkit.getConsoleSender(),
											I18n.locMsg(null, "CommandInfo.MigrationError"));
									e.printStackTrace();
									return false;
								}
							} else if (!dbFile.isFile() && !ymlFile.isDirectory()) {
								if (sender != null) {
									QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.BackupDoesNotExist"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.BackupDoesNotExist"));
								return false;
							}
							//Main.lockDown = true;
							try {
								FileUtils.copyDirectory(ymlFile,
										new File(Main.getInstance().getDataFolder() + "/data/"));
								
							} catch (IOException e) {
								// CommandInfo.ErrorWhileRecovering
								if (sender != null) {
									QuestChatManager.info(sender,
											I18n.locMsg(null, "CommandInfo.ErrorWhileRecovering"));
								}
								QuestChatManager.info(Bukkit.getConsoleSender(),
										I18n.locMsg(null, "CommandInfo.ErrorWhileRecovering"));
								//Main.lockDown.set(false);
								e.printStackTrace();
								return false;
							}
							//Main.lockDown.set(false);
							// tell the admin that the dirty work has been done
						}else {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupTypeUnsupported"));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupTypeUnsupported"));
						}
						return true;
					} catch (Exception e) {

						if (sender != null) {
							QuestChatManager.info(sender,
									I18n.locMsg(null, "CommandInfo.UnknownError", ymlFile.getName()));
						}
						QuestChatManager.info(Bukkit.getConsoleSender(),
								I18n.locMsg(null, "CommandInfo.UnknownError", ymlFile.getName()));
						e.printStackTrace();
					}
					return false;
				}).thenAcceptAsync((bool) -> {
					// ConsoleCommandSender console = Bukkit.getConsoleSender();
					// TODO: fix the damn message
					if(bool) {
						//Bukkit.getLogger().info("test");
						if (ConfigSettings.SAVE_TYPE == SaveType.YML) {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupLoadSuccessful", ymlFile.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupLoadSuccessful", ymlFile.getName()));
						} else {
							if (sender != null) {
								QuestChatManager.info(sender,
										I18n.locMsg(null, "CommandInfo.BackupLoadSuccessful", dbFile.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(),
									I18n.locMsg(null, "CommandInfo.BackupLoadSuccessful", dbFile.getName()));
						}
					}else {
						if (sender != null) {
							QuestChatManager.info(sender,
									I18n.locMsg(null, "CommandInfo.BackupLoadFailed"));
						}
						QuestChatManager.info(Bukkit.getConsoleSender(),
								I18n.locMsg(null, "CommandInfo.BackupLoadFailed"));
					}

					Main.lockDown.set(false);
				});
				return true;
			}else if(args[0].equalsIgnoreCase("convert")) {
				if(Main.lockDown.get()) {
					if (Main.lockDown.get()) {
						QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.OngoingBackupLoad"));
						return false;
					}
				}
				if(args.length != 3) {
					QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.InvalidArgument"));
					return false;
				}
				String name = args[1];
				String format = args[2];
				File ymlFolder = new File(Main.getInstance().configManager.getBackups()+"/"+name);
				File sqlFile = new File(Main.getInstance().configManager.getBackups(),name+".db");
				if(format.equalsIgnoreCase("yml")) {
					if(ymlFolder.isDirectory()) {
						//CommandInfo.ConversionAlreadyExists
						QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.ConversionAlreadyExists"));
						return false;
					}
					if(!sqlFile.isFile()) {
						//CommandInfo.NoSourceFile
						QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.NoSourceFile"));
						return false;
					}
					CompletableFuture.runAsync(()->{
						try {
							SQLiteConfig config = new SQLiteConfig();
							config.setEncoding(SQLiteConfig.Encoding.UTF8);
							Connection conn = DriverManager.getConnection("jdbc:sqlite:"+sqlFile,config.toProperties());
							List<YamlConfiguration> ymls = MigrationManager.convertSQLToYml(conn).get();
							if(ymls==null) {
								if(sender != null) {
									QuestChatManager.error(sender, I18n.locMsg(null, "ComamndInfo.ErrorWhileConverting",ymlFolder.getName(),sqlFile.getName()));
								}
								QuestChatManager.error(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.UnknownError",ymlFolder.getName(),sqlFile.getName()));
								return;
							}
							ymlFolder.mkdir();
							for(YamlConfiguration yml:ymls) {
								File file = new File(ymlFolder,yml.getString("UUID")+".yml");
								file.createNewFile();
								yml.save(file);
							}
							if(sender != null) {
								QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.ConversionSuccesful",sqlFile.getName(),ymlFolder.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.ConversionSuccesful",sqlFile.getName(),ymlFolder.getName()));
						} catch (SQLException | InterruptedException | ExecutionException | IOException e) {
							if(sender != null) {
								QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.UnknownError"));
							}
							QuestChatManager.error(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.UnknownError"));
							e.printStackTrace();
						}
					});
				}else if(format.equalsIgnoreCase("sql")) {
					if(sqlFile.isFile()) {
						//CommandInfo.ConversionAlreadyExists
						QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.ConversionAlreadyExists"));
						return false;
					}
					if(!ymlFolder.isDirectory()) {
						//CommandInfo.NoSourceFile
						QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.NoSourceFile"));
						return false;
					}
					CompletableFuture.runAsync(()->{
						try {
							SQLiteConfig config = new SQLiteConfig();
							config.setEncoding(SQLiteConfig.Encoding.UTF8);
							Connection conn = DriverManager.getConnection("jdbc:sqlite:"+sqlFile,config.toProperties());
							Map<String,YamlConfiguration> map = new HashMap<>(); 
							for(File f:ymlFolder.listFiles()) {
								if(f.isFile()&&f.getName().endsWith(".yml")) {
									try {
										UUID.fromString(QuestUtil.getFileNameWithoutExtension(f));
										map.put(QuestUtil.getFileNameWithoutExtension(f), YamlConfiguration.loadConfiguration(f));
									}catch(IllegalArgumentException e) {
										if(sender != null) {
											QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.YmlFileWrongFormat",f.getName()));
										}
										QuestChatManager.error(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.YmlFileWrongFormat",f.getName()));
									}
								}
							}
							if(!MigrationManager.convertYmltoSQL(map,conn).get()) {
								if(sender != null) {
									QuestChatManager.error(sender, I18n.locMsg(null, "ComamndInfo.ErrorWhileConverting",sqlFile.getName(),ymlFolder.getName()));
								}
								QuestChatManager.error(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.UnknownError",sqlFile.getName(),ymlFolder.getName()));
								return;
							}
							if(sender != null) {
								QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.ConversionSuccesful",ymlFolder.getName(),sqlFile.getName()));
							}
							QuestChatManager.info(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.ConversionSuccesful"));
						} catch (SQLException | InterruptedException | ExecutionException e) {
							if(sender != null) {
								QuestChatManager.error(sender, I18n.locMsg(null, "CommandInfo.UnknownError"));
							}
							QuestChatManager.error(Bukkit.getConsoleSender(), I18n.locMsg(null, "CommandInfo.UnknownError",ymlFolder.getName(),sqlFile.getName()));
							e.printStackTrace();
						}
					});
				}else {
					QuestChatManager.info(sender, I18n.locMsg(null, "CommandInfo.BackupTypeUnsupported"));
					return false;
				}
				return true;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
				return false;
			}
			if (Main.lockDown.get()) {
			// : add lockdown message,cannot alter data while locking down
			return false;
			 }
			QuestPlayerData pd = QuestUtil.getData(target);
			switch (args[0]) {
			// /mqa nextstage [ID] [quest]
			// /mqa forcetake [ID] [quest]
			// /mqa forcefinish [ID] [quest]
			//java8 compatability
			case "nextstage":
				
				Optional<Quest> quest = getQuestArg(args, 2);
				if(quest.isPresent()) {
					 pd.forceNextStage(quest.get(), true);
				}else {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
				}
				//quest.ifPresentOrElse(q -> pd.forceNextStage(q, true),
						//() -> QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument")));
				break;
			case "forcetake":
				quest = getQuestArg(args, 2);
				if(quest.isPresent()) {
					 pd.forceTake(quest.get(), true);
				}else {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
				}
				//quest.ifPresentOrElse(q -> pd.forceTake(q, true),
						//() -> QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument")));
				break;
			case "forcefinish":
				quest = getQuestArg(args, 2);
				if(quest.isPresent()) {
					 pd.forceFinish(quest.get(), true);
				}else {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
				}
				//quest.ifPresentOrElse(q -> pd.forceFinish(q, true),
						//() -> QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument")));
				break;
			case "forcequit":
				quest = getQuestArg(args, 2);
				if(quest.isPresent()) {
					 pd.forceQuit(quest.get(), true);
				}else {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
				}
				//quest.ifPresentOrElse(q -> pd.forceQuit(q, true),
					//	() -> QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument")));
				break;
			// /mqa finishobject [ID] [quest] [objindex]
			case "finishobject":
				if (args.length < 4)
					return false;
				quest = getQuestArg(args, 2);
				int obj = Integer.parseInt(args[3]);
				if (!quest.isPresent()) {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
					return false;
				}
				pd.forceFinishObj(quest.get(), obj, true);
				break;
			// /mqa removedata [ID]
			case "removedata":

				target.kickPlayer(I18n.locMsg(target, "CommandInfo.KickForDataClearing"));
				QuestConfigManager.getSaver().clearPlayerData(target);
				QuestChatManager.info(sender, I18n.locMsg(target, "CommandInfo.PlayerDataRemoved"));
				break;
			// /mqa friendpoint [ID] add/set [NPC] [amount]
			case "friendpoint":
				if (args.length < 5)
					return false;
				if (!QuestValidater.validateNPC(args[3])) {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
					return false;
				}
				if (!QuestValidater.validateInteger(args[4])) {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
					return false;
				}
				NPC npc = Main.getHooker().getNPC(args[3]);
				int amount = Integer.parseInt(args[4]);
				switch (args[2]) {
				case "add":
					pd.addNPCfp(npc.getId(), amount);
					QuestChatManager.info(target, I18n.locMsg(target, "CommandInfo.FriendPointAdded", target.getName(),
							me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), Integer.toString(amount)));
					return false;
				case "set":
					pd.setNPCfp(npc.getId(), amount);
					QuestChatManager.info(target, I18n.locMsg(target, "CommandInfo.FriendPointSet", target.getName(),
							me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), Integer.toString(amount)));
					return false;
				}
				break;
			// /mqa opennpc [ID] [NPCID] [trade]
			case "npcinfo":
				if (args.length < 4)
					return false;
				if (!QuestValidater.validateNPC(args[2])) {
					QuestChatManager.error(sender, I18n.locMsg(target, "CommandInfo.InvalidArgument"));
					return false;
				}
				npc = Main.getHooker().getNPC(args[2]);
				boolean trade = Boolean.parseBoolean(args[3]);
				QuestBookGUIManager.openNPCInfo(target, npc, trade);
				break;
			// /mqa stats [PLAYERNAME] -> open book gui and check player stats

			case "stats":
				// unfinished
				QuestChatManager.info(sender, QuestChatManager.translateColor("[MangoQuestReloaded] To be continued"));
				break;
			}
			QuestChatManager.info(sender, I18n.locMsg(target, "CommandInfo.CommandExecuted"));
			return true;
		}
		sendAdminHelp(sender);
		return true;
	}

	private Optional<Quest> getQuestArg(String[] args, int index) {
		return index < args.length ? Optional.ofNullable(QuestUtil.getQuest(args[index])) : Optional.empty();
	}

	public void sendAdminHelp(CommandSender p) {
		if (p instanceof Player) {
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.Title"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.Reload"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.Placeholders"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.NextStage"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.FinishObject"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.TakeQuest"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.FinishQuest"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.RemovePlayerData"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.FriendPoint"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.npcinfo"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.quitquest"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.stats"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.debuglevel"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.backup"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.loadBackup"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.convert"));
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.ToggleOverrideMode"));
		} else {
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.Title"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.Reload"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.Placeholders"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.NextStage"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.FinishObject"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.TakeQuest"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.FinishQuest"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.RemovePlayerData"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.FriendPoint"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.npcinfo"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.quitquest"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.stats"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.debuglevel"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.backup"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.loadBackup"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.convert"));
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.ToggleOverrideMode"));
		}

	}

	public void sendPlaceHolder(CommandSender p) {
		if (p instanceof Player) {
			QuestChatManager.info(p, I18n.locMsg((Player) p, "AdminCommandHelp.PlaceholdersTitle"));
			QuestChatManager.info(p,
					"%mq_player_currentquest_names%\n" + " %mq_player_currentquest_amount%\n"
							+ " %mq_player_currentquest_names\n" + " %mq_player_currentquest_progress%\n"
							+ " %mq_player_completedquests_names%\n" + " %mq_player_completedquests_amount%\n");
		} else {
			QuestChatManager.info(p, I18n.locMsg(null, "AdminCommandHelp.PlaceholdersTitle"));
			QuestChatManager.info(p,
					"%mq_player_currentquest_names%\n" + " %mq_player_currentquest_amount%\n"
							+ " %mq_player_currentquest_names\n" + " %mq_player_currentquest_progress%\n"
							+ " %mq_player_completedquests_names%\n" + " %mq_player_completedquests_amount%\n");
		}

	}

}
