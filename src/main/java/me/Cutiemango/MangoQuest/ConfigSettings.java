package me.Cutiemango.MangoQuest;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import me.Cutiemango.MangoQuest.I18n.SupportedLanguage;

public class ConfigSettings
{
	public static boolean MC_1_8 = false;
	public static boolean LEGACY_MC = false;
	public static boolean DISCORDSRV_ENABLED = false;
	public static boolean MYTHICMOBNEWSUPPORT = false;
	public static Locale LOCALE_USING;
	public static Locale DEFAULT_LOCALE = new Locale("zh", "TW");
	public static Map<I18n.SupportedLanguage,Locale> PLAYER_LOCALE_CHOICES = new EnumMap<>(SupportedLanguage.class);
	public static boolean USE_RIGHT_CLICK_MENU = true;
	public static int MAXIMUM_QUEST_AMOUNT = 4;
	public static int MAXIMUM_DISPLAY_QUEST_AMOUNT = 3;
	public static int PLAYER_DATA_SAVE_INTERVAL = 600;
	// SQL Garbage Collector in ticks
	public static int SQL_CLEAR_INTERVAL_IN_TICKS = 24000;
	public static int CONVERSATION_ACTION_INTERVAL_IN_TICKS = 25;

	public static boolean POP_LOGIN_MESSAGE = true;
	public static boolean ENABLE_SCOREBOARD = true;
	public static boolean USE_PARTICLE_EFFECT = true;
	public static boolean ENABLE_SKIP = false;
	public static boolean ENABLE_BUNGEECORD_SUPPORT = true;
	

	public static boolean USE_WEAK_ITEM_CHECK = false;
	public static boolean useTranslation = true;
    public static int titlefadein = 5;
    public static int titlestay = 5;
    public static int titlefadeout = 5;
    public static int subtitlefadein = 5;
    public static int subtitlestay = 5;
    public static int subtitlefadeout = 5;
	public static SaveType SAVE_TYPE = SaveType.YML;
	public static String DATABASE_ADDRESS = "localhost";
	public static int DATABASE_PORT = 3306;
	public static String DATABASE_NAME = "sample";
	public static String DATABASE_USER = "admin";
	public static String DATABASE_PASSWORD = "1234";
	public static String BUNGEECORD_PERMISSION = "mangoquestreloaded.bungeecord";
	public static String DISCORDSRV_CHANNEL_NAME = "omegalul";
	public static boolean differentialquestsserver = true;

	public enum SaveType
	{
		YML,
		SQL,
		MONGODB;
	}
}
