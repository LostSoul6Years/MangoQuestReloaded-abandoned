package me.Cutiemango.MangoQuest.manager;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;

import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.compatutils.MythicMob.MythicMobCompatability;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import su.nightexpress.quantumrpg.QuantumRPG;

public class PluginHooker
{

	public PluginHooker(Main main) {
		plugin = main;
		
	}



	private final Main plugin;

	private Economy economy;
	private boolean citizens;
	private boolean shopkeepers;
	private boolean mythicMobs;
	private boolean skillAPI;
	private boolean mcMMO = false;
	private QuantumRPG quantumRPG;
	private boolean placeholderAPI = false;
	private boolean discordSRV = false;
	private boolean mmIsLegacy = true;
	private MythicMobCompatability mmCompat;
	

	public boolean hasDiscordSRVEnabled() {
		return discordSRV;
	}

	public void setHasDiscordSRVEnabled(boolean discordSRV) {
		this.discordSRV = discordSRV;
	}

	public void hookPlugins() {
		try {
			if (plugin.getServer().getPluginManager().isPluginEnabled("Citizens")) {
				citizens = true;
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.CitizensHooked"));
			} else {
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.PluginNotHooked"));
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.CitizensNotHooked1"));
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.CitizensNotHooked2"));
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.PleaseInstall"));
			}

			if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.VaultHooked"));
			} else {
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.PluginNotHooked"));
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.VaultNotHooked"));
				QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.PleaseInstall"));
			}

			if (plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
				//no more stupid version decider
				//even if i am to include the version checker
				//should make use of regex but i dont time for that rn
				try {
					Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
					mmIsLegacy = true;
				}catch(ClassNotFoundException e) {
					mmIsLegacy = false;
				}
				//mmIsLegacy = !Main.getInstance().configManager.getConfig().getBoolean("enablemmnewsupport");
				mmCompat = new MythicMobCompatability();
				mythicMobs = true;
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.MythicMobsHooked"));
			} else {
				DebugHandler.log(1, "MythicMobs not hooked.");
			}

			if (plugin.getServer().getPluginManager().isPluginEnabled("Shopkeepers")) {
				shopkeepers = true;
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.ShopkeepersHooked"));
			} else {
				DebugHandler.log(1, "Shopkeepers not hooked.");
			}

			if (plugin.getServer().getPluginManager().isPluginEnabled("SkillAPI")) {
				skillAPI = true;
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.SkillAPIHooked"));
			} else {
				DebugHandler.log(1, "SkillAPI not hooked.");
			}

			if (plugin.getServer().getPluginManager().isPluginEnabled("QuantumRPG")) {
				quantumRPG = (QuantumRPG) plugin.getServer().getPluginManager().getPlugin("QuantumRPG");
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.QuantumRPGHooked"));
			} else {
				DebugHandler.log(1, "QuantumRPG not hooked.");
			}
			if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				placeholderAPI = true;
				hookPlaceholderAPI();
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.PlaceholderAPIHooked"));
			}else {
				DebugHandler.log(1,"PlaceholderAPI not hooked.");
			}
			if(plugin.getServer().getPluginManager().isPluginEnabled("mcMMO")) {
				mcMMO = true;
			}else {
				mcMMO = false;
				DebugHandler.log(1, "mcMMO not hooked.");
			}
			if(plugin.getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
				discordSRV = true;
			}else {
				discordSRV=false;
				DebugHandler.log(1, "discordSRV not hooked.");
			}
		}
		catch (Exception ignored) {
		}

		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
			QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"PluginHooker.EconomyHooked"));
		} else {
			QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"PluginHooker.EconomyNotHooked"));
		}
		
	}

	public boolean hasEconomyEnabled() {
		return economy != null;
	}

	public boolean hasMythicMobEnabled() {
		return mythicMobs;
	}

	public boolean hasCitizensEnabled() {
		return citizens;
	}

	public boolean hasShopkeepersEnabled() {
		return shopkeepers;
	}

	public boolean hasSkillAPIEnabled() {
		return skillAPI;
	}

	public boolean hasQuantumRPGEnabled() {
		return quantumRPG != null;
	}
	public boolean hasPlaceholderAPIEnabled() {
		return placeholderAPI;
	}
	public boolean hasmcMMOEnabled() {
		return mcMMO;
	}


	public Economy getEconomy() {
		return economy;
	}

	public QuantumRPG getQuantumRPG() {
		return quantumRPG;
	}

	public MythicMobCompatability getMythicMobsAPI() {
		return mmCompat;
	}

	//public MythicMob getMythicMob(String id) {
	//	if (!hasMythicMobEnabled())
	//		return null;
	//	return getMythicMobsAPI().getMythicMob(id);
	//}

	public NPC getNPC(String id) {
		if (!hasCitizensEnabled() || !QuestValidater.validateInteger(id)) {
			if(!QuestValidater.validateInteger(id))
				DebugHandler.log(0, "Rejected NPC %s because integer validation failed", id+"");
			else
				DebugHandler.log(0, "Rejected NPC %s because citizens not enabled", id+"");
			return null;
		}
		return CitizensAPI.getNPCRegistry().getById(Integer.parseInt(id));
	}

	
	public NPC getNPC(int id) {
		if (!hasCitizensEnabled())
			return null;
		return CitizensAPI.getNPCRegistry().getById(id);
	}
	private void hookPlaceholderAPI() {
		new MQPlaceholder().register();
	}
	

	public boolean isMmIsLegacy() {
		return mmIsLegacy;
	}
}
