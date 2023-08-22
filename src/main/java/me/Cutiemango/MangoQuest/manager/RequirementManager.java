package me.Cutiemango.MangoQuest.manager;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.sucy.skill.SkillAPI;

import joptsimple.internal.Strings;
import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;

public class RequirementManager {
	@SuppressWarnings("unchecked")
	public static Optional<String> meetRequirementWith(Player p, EnumMap<RequirementType, Object> requirements,
			boolean sendMsg) {
		QuestPlayerData pd = QuestUtil.getData(p);

		List<String> failMsg = new ArrayList<>();

		for (RequirementType t : requirements.keySet()) {
			Object value = requirements.get(t);
			switch (t) {
			case PERMISSION:
				if (!(value instanceof List)) {
					if (sendMsg)
						DebugHandler.log(5,
								"[Requirements] Requirement type is PERMISSION, but the value is not a list.");
					break;
				}
				List<String> permissions = (List<String>) value;
				for (String perm : permissions) {
					if (!p.hasPermission(perm)) {
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Permission"));
						break;
					}
				}
				break;
			case QUEST:
				for (String s : (List<String>) value) {
					Quest q = QuestUtil.getQuest(s);
					if (!pd.hasFinished(q))
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Quest", q.getQuestName()));
				}
				break;
			case LEVEL:
				if(value != null) {

					int level = (Integer) value;
					if (p.getLevel() < level)
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Level", Integer.toString(level)));
				}
				break;
			case MONEY:
				if (Main.getHooker().hasEconomyEnabled()) {
					if (value != null) {
						double money = (Double) value;
						double possession = Main.getHooker().getEconomy().hasAccount(p)?Main.getHooker().getEconomy().getBalance(p):0.0;						
						if (possession < money)
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Money",
									Double.toString(QuestUtil.cut(money))));
					}
				}
				break;
			case ITEM:
				if (!(value instanceof List)) {
					if (sendMsg)
						DebugHandler.log(5, "[Requirements] Requirement type is ITEM, but the value is not a list.");
					break;
				}
				if (ConfigSettings.USE_WEAK_ITEM_CHECK) {
					for (ItemStack reqItem : (List<ItemStack>) value) {
						int need = reqItem.getAmount();
						for (ItemStack owned : p.getInventory().getContents()) {
							if (owned == null || owned.getType() == Material.AIR)
								continue;
							if (QuestValidater.weakItemCheck(reqItem, owned, false)) {
								need -= Math.min(need, reqItem.getAmount());
								if (need == 0)
									break;
							}
						}
						if (need > 0) {
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Item", QuestUtil.getItemName(reqItem),
									Integer.toString(reqItem.getAmount())));
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
								DebugHandler.log(5,
										"[Requirements] Did not found enough (or any) %s in user's inventory.",
										QuestUtil.getItemName(reqItem));
							}
						}
					}
				} else {
					for (ItemStack reqItem : (List<ItemStack>) value) {
						if (!p.getInventory().containsAtLeast(reqItem, reqItem.getAmount())) {
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.Item", QuestUtil.getItemName(reqItem),
									Integer.toString(reqItem.getAmount())));
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
								DebugHandler.log(5,
										"[Requirements] Did not found enough (or any) %s in user's inventory.",
										QuestUtil.getItemName(reqItem));
							}
						}
					}
				}
				break;
			case SKILLAPI_CLASS:
				String classID = (String) value;
				if (classID.equalsIgnoreCase("none"))
					break;

				if (Main.getHooker().hasSkillAPIEnabled() && SkillAPI.hasPlayerData(p)
						&& SkillAPI.getPlayerData(p).getMainClass() != null) {
					com.sucy.skill.api.classes.RPGClass userClass = SkillAPI.getPlayerData(p).getMainClass().getData(),
							reqClass = SkillAPI.getClass(classID);

					if (!SkillAPI.isClassRegistered(classID))
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.NoClass") + classID);
					else if (!SkillAPI.getPlayerData(p).isExactClass(reqClass)) {
						boolean found = false;
						if ((Boolean) requirements.get(RequirementType.ALLOW_DESCENDANT)) {
							// We recursively search if user's ascendant class matches the required class.
							com.sucy.skill.api.classes.RPGClass it = userClass;
							while (it.getParent() != null) {
								if (it.getParent().getName().equalsIgnoreCase(classID)) {
									found = true;
									if (sendMsg)
										DebugHandler.log(5, "[Requirements] User has descendant class: %s", classID);
									break;
								}
								it = it.getParent();
							}
						}
						if (!found) {
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.RPGClass", reqClass.getPrefix()));
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
								DebugHandler.log(5,
										"[Requirements] User has the following class: %s, while the required class is %s.",
										userClass, classID);
							}
						}
					}
				}
				break;
			case SKILLAPI_LEVEL:
				int lvl = (Integer) value;
				if (lvl == 0)
					break;

				else if (Main.getHooker().hasSkillAPIEnabled() && SkillAPI.hasPlayerData(p))
					if (SkillAPI.getPlayerData(p).getMainClass() == null
							|| SkillAPI.getPlayerData(p).getMainClass().getLevel() < lvl)
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.RPGLevel", Integer.toString(lvl)));
				break;
			case QRPG_CLASS:
				classID = (String) value;
				if (classID.equalsIgnoreCase("none"))
					break;

				if (Main.getHooker().hasQuantumRPGEnabled()) {
					UserProfile user = Main.getHooker().getQuantumRPG().getUserManager().getOrLoadUser(p)
							.getActiveProfile();
					String userClassID = user.getClassData().getClassId();

					if (Main.getHooker().getQuantumRPG().getModuleCache().getClassManager()
							.getClassById(classID) == null)
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.NoClass") + classID);
					else if (!classID.equalsIgnoreCase(userClassID)) {
						RPGClass reqClass = Main.getHooker().getQuantumRPG().getModuleCache().getClassManager()
								.getClassById(classID),
								userClass = Main.getHooker().getQuantumRPG().getModuleCache().getClassManager()
										.getClassById(userClassID);

						boolean found = false;
						if ((Boolean) requirements.get(RequirementType.ALLOW_DESCENDANT)) {
							RPGClass it = userClass;
							while (it.getParent() != null) {
								if (it.getParent().getId().equalsIgnoreCase(classID)) {
									found = true;
									if (sendMsg)
										DebugHandler.log(5, "[Requirements] User has descendant class: %s", classID);
									break;
								}
								it = it.getParent();
							}
						}
						if (!found) {
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.RPGClass", reqClass.getName()));
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
								DebugHandler.log(5,
										"[Requirements] User has the following class: %s, while the required class is %s.",
										user.getClassData().getClassId(), classID);
							}
						}
					}
				}
				break;
			case QRPG_LEVEL:
				lvl = (Integer) value;
				if (lvl == 0)
					break;
				else if (Main.getHooker().hasQuantumRPGEnabled()) {
					UserProfile user = Main.getHooker().getQuantumRPG().getUserManager().getOrLoadUser(p)
							.getActiveProfile();
					if (user.getClassData() == null || user.getClassData().getLevel() < lvl) {
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.RPGLevel", Integer.toString(lvl)));
						if (sendMsg) {
							DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
							DebugHandler.log(5, "[Requirements] User has level %d, while the required level is %d.",
									user.getClassData().getLevel(), lvl);
						}
					}
				}
				break;
			case FRIEND_POINT:
				HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) value;

				for (Integer id : map.keySet()) {
					if (pd.getNPCfp(id) < map.get(id))
						failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.FriendPoint"));
				}
				break;
			case WORLD_TIME: {
				// all world times requirements
				List<Long> worldTicks = (List<Long>) value;
				Long ticksNow = p.getWorld().getTime();
				if (worldTicks.isEmpty()) {
					break;
				}
				if (ticksNow < worldTicks.get(0) || ticksNow > worldTicks.get(1)) {
					failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.WorldTime", String.valueOf(ticksNow),
							worldTicks.get(0) + "", worldTicks.get(1) + ""));
					if (sendMsg) {
						DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
						DebugHandler.log(5, "[Requirements] World Time now is " + ticksNow);
					}
				}

				break;
			}
			case SERVER_TIME: {
				List<Long> irlTicks = (List<Long>) value;
				if (irlTicks.isEmpty()) {
					break;
				}
				long seconds = Instant.now().getEpochSecond() * 1000;
				Date date = new Date(seconds);
				SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String java_date = jdf.format(date);
				if (seconds < irlTicks.get(0) || seconds > irlTicks.get(1)) {
					failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.ServerTime", java_date,
							jdf.format(new Date(irlTicks.get(0))), jdf.format(new Date(irlTicks.get(1)))));
					if (sendMsg) {
						DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
						DebugHandler.log(5,
								"[Requirements] Got the start date " + irlTicks.get(0) + " end date" + irlTicks.get(1));
					}
				}
				break;
			}
			case PLACEHOLDER_API: {
				if (Main.getHooker().hasPlaceholderAPIEnabled()) {
					Map<String, String> papiMap = (Map<String, String>) value;
					for (String placeholder : papiMap.keySet()) {
						String retrievedValue;
						retrievedValue = placeholder.replaceAll("%", "");
						retrievedValue = "%" + retrievedValue + "%";
						if (!retrievedValue.equals(papiMap.get(placeholder))) {
							failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.PlaceholderAPI", retrievedValue,
									papiMap.get(placeholder)));
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
							}
						}
					}
				}
				break;
			}
			case MCMMO_LEVEL: {
				if (Main.getHooker().hasmcMMOEnabled()) {
					Map<PrimarySkillType, Integer> skillMap = (Map<PrimarySkillType, Integer>) requirements.get(t);
					for (PrimarySkillType pst : skillMap.keySet()) {

						if (ExperienceAPI.getLevel(p, pst) < skillMap.get(pst)) {
							if (sendMsg) {
								DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
								DebugHandler.log(5, "[Requirements] Required Class %s is lower than minimum level %s",
										pst.name(), skillMap.get(pst) + "");
							}
							failMsg.add(
									I18n.locMsg(p, "Requirements.NotMeet.mcMMO", pst.name(), skillMap.get(pst) + ""));
						} else {
							DebugHandler.log(5, "[Requirements] User succeeded requirement: %s %s retrieved:%s",
									pst.name(), skillMap.get(pst), ExperienceAPI.getLevel(p, pst));
						}

					}
				}
				break;
			}

			}
		}

		if (failMsg.isEmpty())
			return Optional.empty();
		return Optional.of(MinecraftCompatability.join(failMsg, "\n"));
	}

	public static Optional<String> checkServerTimeFail(Player p, World w, EnumMap<RequirementType, Object> requirements,
			boolean sendMsg) {
		// QuestPlayerData pd = QuestUtil.getData(p);

		List<String> failMsg = new ArrayList<>();

		for (RequirementType t : requirements.keySet()) {
			Object value = requirements.get(t);
			switch (t) {
			case SERVER_TIME: {
				List<Long> irlTicks = (List<Long>) value;
				if (irlTicks.isEmpty()) {
					break;
				}
				long seconds = Instant.now().getEpochSecond() * 1000;
				Date date = new Date(seconds);
				SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String java_date = jdf.format(date);
				if (seconds < irlTicks.get(0) || seconds > irlTicks.get(1)) {
					failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.ServerTime", java_date,
							jdf.format(new Date(irlTicks.get(0))), jdf.format(new Date(irlTicks.get(1)))));
					if (sendMsg) {
						DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
						DebugHandler.log(5,
								"[Requirements] Got the start date " + irlTicks.get(0) + " end date" + irlTicks.get(1));
					}
				}
				break;
			}
			case WORLD_TIME: {
				List<Long> worldTicks = (List<Long>) value;
				if (worldTicks.isEmpty()) {
					break;
				}
				if (w.getTime() < worldTicks.get(0) || w.getTime() > worldTicks.get(1)) {
					if (sendMsg) {
						DebugHandler.log(5, "[Requirements] User has failed requirement: " + t.toString());
						DebugHandler.log(5, "[Requirements] Got the start date " + worldTicks.get(0) + " end date"
								+ worldTicks.get(1));
					}
					failMsg.add(I18n.locMsg(p, "Requirements.NotMeet.WorldTime", worldTicks.get(0) + "",
							worldTicks.get(1) + "", w.getTime() + ""));
				}
				break;
			}
			}
		}

		if (failMsg.isEmpty())
			return Optional.empty();
		return Optional.of(MinecraftCompatability.join(failMsg, "\n"));
	}
}
