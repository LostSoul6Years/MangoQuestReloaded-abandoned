package me.Cutiemango.MangoQuest.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import me.Cutiemango.MangoQuest.I18n.SupportedLanguage;

public class TranslatableService {
	
	private static boolean isFetched = false;
	
	private static volatile ConcurrentMap<SupportedLanguage, JsonElement> internal = new ConcurrentHashMap<>();
	
	private static String sauce = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/%version%/assets/minecraft/lang/%lang%.json";

	public static boolean isFetched() {
		return isFetched;
		
	}
	
	public static JsonElement fetch(SupportedLanguage lang){
		
		if(internal.get(lang) == null) {
			StringBuilder builder = new StringBuilder();
			try(InputStream stream = new URL(sauce.replace("%version%", Bukkit.getBukkitVersion().split("-")[0]).replace("%lang%", lang.name().toLowerCase())).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
				String input;
				while((input = reader.readLine()) != null) {
					builder.append(input);
				}
				////Type mapType = new TypeToken<Map<String, Map>>(){}.getType();  
				//Map<String,Map> map = new Gson().fromJson(builder.toString(), mapType);
				JsonElement root = new JsonParser().parse(builder.toString());
				//Bukkit.getLogger().info(builder.toString());
				isFetched = true;
				//Bukkit.getLogger().info("is null?"+(root == null));
				internal.put(lang,  root);
				
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}
		
		return internal.get(lang);
		//return internal;
	}
	
public static JsonElement fetch(SupportedLanguage lang,boolean isDefault){
		
		if(internal.get(lang) == null) {
			StringBuilder builder = new StringBuilder();
			try(InputStream stream = new URL(sauce.replace("%version%", Bukkit.getBukkitVersion().split("-")[0]).replace("%lang%", lang.name().toLowerCase())).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
				String input;
				while((input = reader.readLine()) != null) {
					builder.append(input);
				}
				////Type mapType = new TypeToken<Map<String, Map>>(){}.getType();  
				//Map<String,Map> map = new Gson().fromJson(builder.toString(), mapType);
				JsonElement root = new JsonParser().parse(builder.toString());
				//Bukkit.getLogger().info(builder.toString());
				isFetched = true;
				//Bukkit.getLogger().info("is null?"+(root == null));
				internal.put(lang,  root);
				
				if(isDefault) {
					internal.put(SupportedLanguage.DEFAULT,root);
				}
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}
		
		return internal.get(lang);
		//return internal;
	}
	
	public static String getTranslation(String key,SupportedLanguage lang) {
		//Bukkit.getLogger().info(key);
		//Bukkit.getLogger().info("getting translation of "+lang.name());
		return internal.get(lang).getAsJsonObject().get(key) != null ? internal.get(lang).getAsJsonObject().get(key).getAsString() : null;
	}
	

	
	
	
}
