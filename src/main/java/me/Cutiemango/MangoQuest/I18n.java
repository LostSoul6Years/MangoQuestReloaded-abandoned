package me.Cutiemango.MangoQuest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.clip.placeholderapi.PlaceholderAPI;

public class I18n {
	public static enum SupportedLanguage {
		zh_TW, zh_CN, en_US,DEFAULT;

		public static SupportedLanguage valueOfSave(String locale) {
			for (SupportedLanguage sl : values()) {
				if (sl.name().equalsIgnoreCase(locale)) {
					return sl;
				}
			}
			return DEFAULT;
		}
	};

	private static ResourceBundle bundle;
	private static HashMap<UUID, SupportedLanguage> playerLangMap = new HashMap<>();
	private static Map<SupportedLanguage, ResourceBundle> langMap = new EnumMap<>(SupportedLanguage.class);

	public static void initLangMap(boolean useCustom) {
		for (SupportedLanguage local : ConfigSettings.PLAYER_LOCALE_CHOICES.keySet()) {
			try {
				if (bundle != null)
					ResourceBundle.clearCache();

				String properties = "messages_" + ConfigSettings.LOCALE_USING.toString() + ".properties";
				String langPath = "lang" + File.separator + "original_" + ConfigSettings.LOCALE_USING.toString()
						+ ".yml";

				if (useCustom) {
					QuestChatManager.logCmd(Level.INFO, "Using custom locale.");
					if (!new File(Main.getInstance().getDataFolder() + File.separator + langPath).exists())
						Main.getInstance().saveResource(langPath, true);
					if (!new File(Main.getInstance().getDataFolder() + File.separator + properties).exists())
						Main.getInstance().saveResource(properties, true);
				} else {
					QuestChatManager.logCmd(Level.INFO, "Using default locale.");
					Main.getInstance().saveResource(properties, true);
					Main.getInstance().saveResource(langPath, true);
				}
				InputStream in = new FileInputStream(new File(Main.getInstance().getDataFolder(),"messages_" + ConfigSettings.PLAYER_LOCALE_CHOICES.get(local) + ".properties"));;
				BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
				langMap.put(SupportedLanguage.valueOf(local.toString()),
						new PropertyResourceBundle(reader));

			} catch (IOException e) {
				try (InputStream in = I18n.class.getResourceAsStream(
						"/messages_" + ConfigSettings.PLAYER_LOCALE_CHOICES.get(local).toString() + ".properties");
						BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))) {
					// FileConfiguration idk = YamlConfiguration.loadConfiguration(reader);
					ResourceBundle tempBundle = new PropertyResourceBundle(reader);
					langMap.put(SupportedLanguage.valueOf(local.toString()), tempBundle);
					// Use resource
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public static void init(Locale local, boolean useCustom) {
		try {
			if (bundle != null)
				ResourceBundle.clearCache();

			String properties = "messages_" + ConfigSettings.LOCALE_USING.toString() + ".properties";
			String langPath = "lang" + File.separator + "original_" + ConfigSettings.LOCALE_USING.toString() + ".yml";

			if (useCustom) {
				QuestChatManager.logCmd(Level.INFO, "Using custom locale.");
				if (!new File(Main.getInstance().getDataFolder() + File.separator + langPath).exists())
					Main.getInstance().saveResource(langPath, true);
				if (!new File(Main.getInstance().getDataFolder() + File.separator + properties).exists())
					Main.getInstance().saveResource(properties, true);
			} else {
				QuestChatManager.logCmd(Level.INFO, "Using default locale.");
				Main.getInstance().saveResource(properties, true);
				Main.getInstance().saveResource(langPath, true);
			}
			//bundle = ResourceBundle.getBundle("messages", local,
				//	new FileResClassLoader(I18n.class.getClassLoader(), Main.getInstance()));
			InputStream in = new FileInputStream(new File(Main.getInstance().getDataFolder(),"messages_" + local.toString() + ".properties"));;
			BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			// FileConfiguration idk = YamlConfiguration.loadConfiguration(reader);
			bundle = new PropertyResourceBundle(reader);
			
		} catch ( IOException e) {
			QuestChatManager.logCmd(Level.WARNING,
					"The plugin encountered an error during initializing the i18n file.");
			e.printStackTrace();
			try (InputStream in = I18n.class.getResourceAsStream(
					"/messages_" + local.toString() + ".properties");
					BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))) {
				// FileConfiguration idk = YamlConfiguration.loadConfiguration(reader);
				bundle = new PropertyResourceBundle(reader);
				// Use resource
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static String locMsg(OfflinePlayer p, String path) {
		String format = "";
		SupportedLanguage lang = SupportedLanguage.valueOfSave(ConfigSettings.DEFAULT_LOCALE.toString());
		try {
			if (p != null) {

				if (p instanceof Player) {
					if (I18n.getPlayerLang((Player) p) == null
							|| I18n.getPlayerLang((Player) p).equals(I18n.SupportedLanguage.DEFAULT)) {
						format = bundle.getString(path);
						format = QuestChatManager.translateColor(format);
					} else {
						lang = I18n.getPlayerLang((Player) p);
						format = langMap.get(I18n.getPlayerLang(p.getPlayer())).getString(path);
						format = QuestChatManager.translateColor(format);
					}
				} else {
					format = bundle.getString(path);
					format = QuestChatManager.translateColor(format);
				}
			} else {
				format = bundle.getString(path);
				format = QuestChatManager.translateColor(format);
			}
		} catch (MissingResourceException e) {
			// return "error";
			// missing resource,getting default message instead.'
			try {
				if (bundle.getString("Bundle.MissingResource") != null) {
					QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "Bundle.MissingResource", path));
				} else {
					QuestChatManager.logCmd(Level.WARNING, "Missing Message in message.properties:" + path);
					// dostuff
				}
			} catch (MissingResourceException fuckingShit) {
				QuestChatManager.logCmd(Level.WARNING, "Missing Message in message.properties:" + path);
				QuestChatManager.logCmd(Level.WARNING,
						"Missing Message in message.properties:" + "Bundle.MissingResource");
			}
			try (InputStream in = I18n.class.getResourceAsStream("/messages_" + lang.name() + ".properties");
					BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))) {
				// FileConfiguration idk = YamlConfiguration.loadConfiguration(reader);
				ResourceBundle tempBundle = new PropertyResourceBundle(reader);
				Properties idk = new Properties();
				try (InputStream is = new FileInputStream(
						Main.getInstance().getDataFolder() + "/messages_" + lang.name() + ".properties");
						InputStreamReader iss = new InputStreamReader(is, StandardCharsets.UTF_8);
						OutputStream ip = new FileOutputStream(
								Main.getInstance().getDataFolder() + "/messages_" + lang.name() + ".properties", true);
						OutputStreamWriter ips = new OutputStreamWriter(ip, StandardCharsets.UTF_8);) {
					if (is != null && ip != null) {
						// idk.load(iss);
						idk.setProperty(path, tempBundle.getString(path));
						idk.store(ips, "");
					}
					I18n.init(ConfigSettings.DEFAULT_LOCALE, true);
					I18n.initLangMap(true);
				} catch (IOException ioe) {

				}
				format = QuestChatManager.translateColor(tempBundle.getString(path));
				// Use resource
				
			} catch (IOException e1) {
				e.printStackTrace();
				return "error";
			}
		}
		if (p != null && Main.getHooker().hasPlaceholderAPIEnabled()) {
			format = PlaceholderAPI.setPlaceholders(p, format);
		}
		return format;
		// return "error";
	}

	public static String locMsg(OfflinePlayer p, String path, String... args) {
		String format = "";
		SupportedLanguage lang = SupportedLanguage.valueOfSave(ConfigSettings.DEFAULT_LOCALE.toString());
		try {
			if (p != null) {

				if (p instanceof Player) {
					if (I18n.getPlayerLang((Player) p) == null
							|| I18n.getPlayerLang((Player) p).equals(I18n.SupportedLanguage.DEFAULT)) {
						format = bundle.getString(path);
						format = QuestChatManager.translateColor(format);
					} else {
						lang = I18n.getPlayerLang((Player) p);
						format = langMap.get(I18n.getPlayerLang((Player) p)).getString(path);
						format = QuestChatManager.translateColor(format);
					}
				} else {
					format = bundle.getString(path);
					format = QuestChatManager.translateColor(format);
				}
			} else {
				format = bundle.getString(path);
				format = QuestChatManager.translateColor(format);
			}
		} catch (Exception e) {
			if (e instanceof MissingResourceException) {
				try {
					if (bundle.getString("Bundle.MissingResource") != null) {
						QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null, "Bundle.MissingResource", path));
					} else {
						QuestChatManager.logCmd(Level.WARNING, "Missing Message in message.properties:" + path);
						// dostuff
					}
				} catch (MissingResourceException fuckingShit) {
					QuestChatManager.logCmd(Level.WARNING, "Missing Message in message.properties:" + path);
					QuestChatManager.logCmd(Level.WARNING,
							"Missing Message in message.properties:" + "Bundle.MissingResource");
				}
				try (InputStream in = I18n.class.getResourceAsStream("/messages_" + lang.name() + ".properties");
						BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))) {
					// FileConfiguration idk = YamlConfiguration.loadConfiguration(reader);
					// format = QuestChatManager.translateColor(idk.getString(path));
					ResourceBundle tempBundle = new PropertyResourceBundle(reader);
					Properties idk = new Properties();
					try (InputStream is = new FileInputStream(
							Main.getInstance().getDataFolder() + "/messages_" + lang.name() + ".properties");
							InputStreamReader iss = new InputStreamReader(is, StandardCharsets.UTF_8);
							OutputStream ip = new FileOutputStream(
									Main.getInstance().getDataFolder() + "/messages_" + lang.name() + ".properties",
									true);
							OutputStreamWriter ips = new OutputStreamWriter(ip, StandardCharsets.UTF_8);) {
						if (is != null && ip != null) {
							// idk.load(iss);
							idk.setProperty(path, tempBundle.getString(path));
							idk.store(ips, "");
						}

						I18n.init(ConfigSettings.DEFAULT_LOCALE, true);
						I18n.initLangMap(true);
					} catch (IOException ioe) {

					}

					format = QuestChatManager.translateColor(tempBundle.getString(path));

					// Use resource
					
				} catch (IOException e1) {
					e1.printStackTrace();
					return "error";
				}
			}

		}

		if (format.contains("%")) {
			for (int arg = 0; arg < args.length; arg++) {
				format = format.replace("[%" + arg + "]", args[arg]);
			}

		}

		if (p != null && Main.getHooker().hasPlaceholderAPIEnabled()) {
			format = PlaceholderAPI.setPlaceholders(p, format);
		}
		return format;
	}

	private static class FileResClassLoader extends ClassLoader {
		private final File dataFolder;

		FileResClassLoader(final ClassLoader classLoader, final Main plugin) {
			super(classLoader);
			this.dataFolder = plugin.getDataFolder();
		}

		@Override
		public URL getResource(final String string) {
			final File file = new File(dataFolder, string);
			if (file.exists()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException ex) {
				}
			}
			return null;
		}

		@Override
		public InputStream getResourceAsStream(final String string) {
			final File file = new File(dataFolder, string);
			if (file.exists()) {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException ex) {
				}
			}
			return null;
		}
	}

	public static HashMap<UUID, SupportedLanguage> getLangMap() {
		return playerLangMap;
	}

	public static void appendLangData(UUID uuid, SupportedLanguage sl) {
		playerLangMap.put(uuid, sl);
	}

	public static SupportedLanguage getPlayerLang(Player p) {
		if (playerLangMap.containsKey(p.getUniqueId())) {
			return playerLangMap.get(p.getUniqueId());
		} else {
			return SupportedLanguage.DEFAULT;
		}
	}

	// trash function i found on internet because I did not even bother to make it
	// lmao
	public static int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}

}
