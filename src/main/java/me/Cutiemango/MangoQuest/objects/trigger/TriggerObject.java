package me.Cutiemango.MangoQuest.objects.trigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed.Field;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;

public class TriggerObject
{
	TriggerObjectType type;
	String obj;
	int stage;

	public TriggerObject(TriggerObjectType t, String o, int i) {
		type = t;
		obj = o;
		stage = i;
	}

	public enum TriggerObjectType
	{
		COMMAND("TriggerObject.Command"),
		COMMAND_PLAYER("EnumAction.CommandPlayer"),
		COMMAND_PLAYER_OP("EnumAction.CommandPlayerOP"),
		SEND_TITLE("TriggerObject.SendTitle"),
		SEND_SUBTITLE("TriggerObject.SendSubtitle"),
		SEND_TITLE_AND_SUBTITLE("TriggerObject.SendTitleAndSubtitle"),
		SEND_MESSAGE("TriggerObject.SendMessage"),
		OPEN_CONVERSATION("TriggerObject.OpenConversation"),
		TELEPORT("TriggerObject.Teleport"),
		WAIT("TriggerObject.Wait"),
		DISCORD_SRV("TriggerObject.DiscordSRV");

		private String name;

		TriggerObjectType(String s) {
			name = s;
		}

		public String toCustomString(Player p) {
			return I18n.locMsg(p, name);
		}
	}

	public TriggerObjectType getObjType() {
		return type;
	}

	public String getObject() {
		return obj;
	}

	public int getStage() {
		return stage;
	}

	@SuppressWarnings("deprecation")
	public void trigger(Player p, TriggerTask task) {
		
		String object = obj.replace("<player>", p.getName());
		switch (type) {
			case WAIT:
				new BukkitRunnable()
				{
					@Override
					public void run() {
						task.next();
					}
				}.runTaskLater(Main.getInstance(), Long.parseLong(object) * 20);
				return;
			case COMMAND:
				QuestUtil.executeSyncConsoleCommand(object);
				break;
			case COMMAND_PLAYER:
				QuestUtil.executeSyncCommand(p, object);
				break;
			case COMMAND_PLAYER_OP:
				QuestUtil.executeSyncOPCommand(p, object);
				break;
			case SEND_MESSAGE:
				p.sendMessage(QuestChatManager.translateColor(object));
				break;
			case SEND_SUBTITLE:
				QuestUtil.sendTitle(p, ConfigSettings.titlefadein, ConfigSettings.titlestay, ConfigSettings.titlefadeout, "", object);
				break;
			case OPEN_CONVERSATION:
				
				if (ConversationManager.getConversation(object) != null) {
					//QuestUtil.getData(p).talkToNPC(ConversationManager.getConversation(object).getNPC());
					ConversationManager.startConversation(p, ConversationManager.getConversation(object));
				
				}
				break;
			case SEND_TITLE:
				QuestUtil.sendTitle(p, ConfigSettings.titlefadein, ConfigSettings.titlestay, ConfigSettings.titlefadeout, object, "");
				break;
			case SEND_TITLE_AND_SUBTITLE:
				String title = object.split("%")[0];
				String subtitle = "";
				if (object.split("%")[0].length() > 1)
					subtitle = object.split("%")[1];
				QuestUtil.sendTitle(p, ConfigSettings.titlefadein, ConfigSettings.titlestay, ConfigSettings.titlefadeout, title, subtitle);
				break;
			case TELEPORT:
				String[] splited = obj.split(":");
				Location loc = new Location(Bukkit.getWorld(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]),
						Double.parseDouble(splited[3]));
				p.teleport(loc);
				break;
			case DISCORD_SRV:
				String message = obj;
				if(Main.getHooker().hasDiscordSRVEnabled() && ConfigSettings.DISCORDSRV_ENABLED) {
					QuestIO dcsrv = Main.getInstance().configManager.getDiscordSRVMessages();
					FileConfiguration config = dcsrv.getConfig();
					if(ConfigSettings.DISCORDSRV_CHANNEL_NAME == null) {
						break;
					}
					TextChannel idk  =QuestUtil.findChannel(ConfigSettings.DISCORDSRV_CHANNEL_NAME);
					
					//Bukkit.getLogger().info(idk.getName()+"///// "+idk.getId());
					for(String msg:dcsrv.getConfig().getConfigurationSection("messages").getKeys(false)) {
						if(msg.equals(message)) {
							String msgPath = "messages."+msg+".";
							
							if(!dcsrv.getBoolean(msgPath+"isembed")) {
								for(String content:dcsrv.getConfig().getStringList(msgPath+"content")) {
									content = content.replace("<player>", ChatColor.stripColor(p.getDisplayName()));
									DiscordUtil.sendMessage(QuestUtil.findChannel(ConfigSettings.DISCORDSRV_CHANNEL_NAME), content);
									//Bukkit.getLogger().info("sent message "+content);
								}
							}else {
								String sPath = msgPath+"settings.";
								String cPath = msgPath+"content";
								Date date = null;
								OffsetDateTime date1 = null;
								List<Field> fields = new ArrayList<>();
								for(String field:dcsrv.getConfig().getConfigurationSection(cPath).getKeys(false)) {
									String fieldPath = cPath+"."+field+".";									
									fields.add(new Field(dcsrv.getStringOrDefault(fieldPath+"key", ""),dcsrv.getStringOrDefault(fieldPath+"value", ""),dcsrv.getBoolean(fieldPath+"inline")));
								}
								if(config.get(sPath+"time")!=null) {
									date = new Date();
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									try {
										date = format.parse(config.getString(sPath+"time"));
									} catch (ParseException e) {
										//e.printStackTrace();
										QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "Cmdlog.DiscordSRVWrongTimestamp"));
									}
									date1 = date.toInstant().atOffset(ZoneOffset.UTC);
								}
								
								
								EmbedBuilder eb = new EmbedBuilder();
								for(Field f:fields) {
									if(f.getName()==null||f.getValue()==null) {
										eb.addBlankField(f.isInline());
										continue;
									}
									eb.addField(f.getName(), f.getValue(), f.isInline());
								}
								if(config.getString(sPath+"title")!=null)
								eb.setTitle(config.getString(sPath+"title").replace("<player>", ChatColor.stripColor(p.getDisplayName())),dcsrv.getStringOrDefault(sPath+"titleurl","").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								
								if(dcsrv.getString(sPath+"color")!=null)
								eb.setColor(Integer.parseInt(dcsrv.getStringOrDefault(sPath+"color", "000000"),16));
								
								if(config.getString(sPath+"description")!=null)
								eb.setDescription(config.getString(sPath+"description").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								String name = config.getString(sPath+".authorinfo.name");
								if(name!=null)
								eb.setAuthor(name.replace("<player>", ChatColor.stripColor(p.getDisplayName())),dcsrv.getStringOrDefault(sPath+".authorinfo.url","").replace("<player>", ChatColor.stripColor(p.getDisplayName())),												
												dcsrv.getStringOrDefault(sPath+".authorinfo.iconUrl","").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								
								if(config.getString(sPath+".thumbnail.url")!=null)
								eb.setThumbnail(config.getString(sPath+".thumbnail.url").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								
								if(config.getString(sPath+".footer.text")!=null)
								eb.setFooter(config.getString(sPath+".footer.text").replace("<player>", ChatColor.stripColor(p.getDisplayName())),dcsrv.getStringOrDefault(sPath+".footer.iconUrl","").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								
								if(config.getString(sPath+".image.url")!=null)
								eb.setImage(config.getString(sPath+".image.url").replace("<player>", ChatColor.stripColor(p.getDisplayName())));
								
								if(date1!=null)
								eb.setTimestamp(date1);
								//eb.copyFrom(me);
								//eb.setTitle("hi");
								//Bukkit.getLogger().info("sent "+msg +" blocking");
								idk.sendMessage(eb.build()).queue();

								
							}
							break;
						}
					}
				}
				break;
			default:
				break;
		}
		task.next();
	}
}
