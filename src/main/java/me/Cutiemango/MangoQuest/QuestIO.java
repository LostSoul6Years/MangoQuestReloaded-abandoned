package me.Cutiemango.MangoQuest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import me.Cutiemango.MangoQuest.manager.QuestChatManager;

public class QuestIO
{
	private final File file;
	private String[] aliases;
	private final FileConfiguration config = new YamlConfiguration();

	public QuestIO(File f) {
		file = f;
		initWithFile();
		loadFrom(file);
	}
	

	public QuestIO(String name, boolean warn, boolean fromJar, boolean replace, String... aliases) {
		this.aliases = aliases;
		file = new File(Main.getInstance().getDataFolder(), name);
		if (!file.exists() && warn)
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.FileNotFound", name));

		if (fromJar) {
			for(String alias:aliases) {
				File aliasFile;
				if((aliasFile = new File(Main.getInstance().getDataFolder(),alias)).exists()) {
					try {
						Files.move(aliasFile, file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
			
			Main.getInstance().saveResource(name, replace);
		}
		else
			initWithFileAndAliases();

		loadFrom(file);
	}
	
	public QuestIO(String folder,String name, boolean warn, boolean fromJar, boolean replace, String... aliases) {
		this.aliases = aliases;
		file = new File(Main.getInstance().getDataFolder() + File.separator + folder + File.separator, name);
		if (!file.exists() && warn)
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.FileNotFound", name));

		if (fromJar) {
			for(String alias:aliases) {
				File aliasFile;
				if((aliasFile = new File(Main.getInstance().getDataFolder(),alias)).exists()) {
					try {
						Files.move(aliasFile, file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
			
			Main.getInstance().saveResource(name, replace);
		}
		else
			initWithFileAndAliases();
		
		if(!file.exists()){
			try {
				FileUtils.moveFile(new File(Main.getInstance().getDataFolder(),name), file);
			} catch (IOException e) {
				DebugHandler.debug("Cannot save config to "+file.getPath()+"! Error as follows");
				e.printStackTrace();
				return;
			}
		}
		loadFrom(file);
	}


	public QuestIO(String folder, String name, String... aliases) {
		this.aliases = aliases;
		file = new File(Main.getInstance().getDataFolder() + File.separator + folder + File.separator, name);
		initWithFileAndAliases();
		loadFrom(file);
	}
	
	public QuestIO(String name, boolean warn, boolean fromJar, boolean replace) {
		
		file = new File(Main.getInstance().getDataFolder(), name);
		if (!file.exists() && warn)
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.FileNotFound", name));

		if (fromJar)
			Main.getInstance().saveResource(name, replace);
		else
			initWithFile();
		loadFrom(file);
	}
	public QuestIO(String folder,String name, boolean warn, boolean fromJar, boolean replace) {
		file = new File(Main.getInstance().getDataFolder() + File.separator + folder + File.separator, name);
		if (!file.exists() && warn)
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.FileNotFound", name));

		if (fromJar)
			Main.getInstance().saveResource(name, replace);
		else
			initWithFile();
		
		if(!file.exists()){
			try {
				FileUtils.moveFile(new File(Main.getInstance().getDataFolder(),name), file);
			} catch (IOException e) {
				DebugHandler.debug("Cannot save config to "+file.getPath()+"! Error as follows");
				e.printStackTrace();
				return;
			}
		}
		loadFrom(file);
	}


	public QuestIO(String folder, String name) {
		file = new File(Main.getInstance().getDataFolder() + File.separator + folder + File.separator, name);
		initWithFile();
		loadFrom(file);
	}

	public QuestIO(Player p) {
		file = new File(Main.getInstance().getDataFolder() + "/data/", p.getUniqueId() + ".yml");
		if (!file.exists())
			QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.NewRegister", p.getName()));
		initWithFile();
		loadFrom(file);
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void initWithFile() {
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				config.save(file);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadFrom(file);
	}
	
	public void initWithFileAndAliases() {
		if (!file.exists()) {
			try {
				for(String alias:aliases) {
					File aliasFile;
					if((aliasFile = new File(Main.getInstance().getDataFolder(),alias)).exists()) {
						Files.move(aliasFile, file);
						return;
					}
				}
				file.getParentFile().mkdirs();
				file.createNewFile();
				config.save(file);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadFrom(file);
	}

	public void save() {
		try {
			config.save(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadFrom(File f) {
		try {
			config.load(new InputStreamReader(new FileInputStream(f), Charsets.UTF_8));
		}
		catch (IOException | InvalidConfigurationException e) {
			QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"Cmdlog.IOException"));
			e.printStackTrace();
		}
	}

	public Set<String> getSection(String path) {
		return config.getConfigurationSection(path).getKeys(false);
	}

	public Set<Integer> getIntegerSection(String path) {
		Set<Integer> set = new HashSet<>();
		for (String key : config.getConfigurationSection(path).getKeys(false)) {
			set.add(Integer.parseInt(key));
		}
		return set;
	}
	public Set<Long> getLongSection(String path) {
		Set<Long> set = new HashSet<>();
		for (String key : config.getConfigurationSection(path).getKeys(false)) {
			set.add(Long.parseLong(key));
		}
		return set;
	}

	public void set(String path, Object value) {
		config.set(path, value);
	}

	public int getInt(String path) {
		return config.getInt(path);
	}

	public int getIntOrDefault(String path, int defaultValue) {
		int i = getInt(path);
		return i == 0 ? defaultValue : i;
	}

	public double getDouble(String path) {
		return config.getDouble(path);
	}

	public long getLong(String path) {
		return config.getLong(path);
	}

	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}

	public String getString(String path) {
		return config.getString(path);
	}

	public String getStringOrDefault(String path, String defaultValue) {
		return Optional.ofNullable(getString(path)).orElse(defaultValue);
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	public ItemStack getItemStack(String path) {
		return config.getItemStack(path);
	}

	public boolean contains(String path) {
		return config.contains(path);
	}

	public boolean isSection(String path) {
		return config.isConfigurationSection(path);
	}

	public void removeSection(String path) {
		config.set(path, null);
	}

}
